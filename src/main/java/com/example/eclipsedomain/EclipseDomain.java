package com.example.eclipsedomain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EclipseDomain {
    private static final List<ActiveDomain> ACTIVE = new ArrayList<>();
    private static final int RADIUS = 10;
    private static final int DOMAIN_TICKS = 60;

    private EclipseDomain() {}

    public static void start(ServerPlayer owner) {
        ACTIVE.removeIf(domain -> domain.ownerId.equals(owner.getUUID()));
        Vec3 center = owner.position().add(0, 1, 0);
        ACTIVE.add(new ActiveDomain(owner.getUUID(), owner.serverLevel(), center));
        owner.serverLevel().playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE, 1.5F, 0.6F);
    }

    public static void tickServer(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;
        Iterator<ActiveDomain> domains = ACTIVE.iterator();
        while (domains.hasNext()) {
            ActiveDomain domain = domains.next();
            domain.age++;
            if (domain.age <= DOMAIN_TICKS) {
                renderAndStrike(domain);
            } else {
                finish(domain);
                domains.remove();
            }
        }
    }

    private static void renderAndStrike(ActiveDomain domain) {
        ServerLevel level = domain.level;
        Vec3 center = domain.center;
        double radius = RADIUS * Math.min(1.0, (domain.age + 5) / 20.0);
        for (int i = 0; i < 48; i++) {
            double angle = Math.PI * 2 * i / 48 + domain.age * 0.04;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.FLAME, x, center.y + .1, z, 2, .1, .02, .1, .01);
            level.sendParticles(ParticleTypes.SMOKE, x, center.y + .15, z, 1, .05, .01, .05, .01);
        }
        level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 24 - Math.min(domain.age, 56) * .12,
                center.z, 12, 1.2, 1.2, 1.2, .03);
        beam(level, center, domain.age);
        if (domain.age >= 20 && domain.age < 56) {
            if (domain.age % 2 == 0) fireRay(domain);
            if (domain.age % 6 == 0) meteor(domain);
        }
    }

    private static void beam(ServerLevel level, Vec3 center, int age) {
        double top = center.y + 20 - Math.max(0, age - 56) * .3;
        for (int y = 0; y < 20; y++) {
            level.sendParticles(ParticleTypes.END_ROD, center.x, top - y * .95, center.z,
                    2, .12, .12, .12, .01);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, top, center.z, 14, 1, .05, 1, .04);
    }

    private static void fireRay(ActiveDomain domain) {
        double x = domain.center.x + ThreadLocalRandom.current().nextInt(-10, 11);
        double z = domain.center.z + ThreadLocalRandom.current().nextInt(-10, 11);
        for (int y = 0; y < 20; y++) {
            domain.level.sendParticles(ParticleTypes.FLAME, x, domain.center.y + y, z,
                    10, .15, .15, .15, .02);
            domain.level.sendParticles(ParticleTypes.LAVA, x, domain.center.y + y, z, 2, .05, .05, .05, 0);
        }
        hurt(domain, new Vec3(x, domain.center.y + .5, z), 1, 10, false);
    }

    private static void meteor(ActiveDomain domain) {
        double x = domain.center.x + ThreadLocalRandom.current().nextInt(-9, 10);
        double z = domain.center.z + ThreadLocalRandom.current().nextInt(-9, 10);
        for (int y = 0; y < 20; y += 2) {
            domain.level.sendParticles(ParticleTypes.FLAME, x, domain.center.y + y, z, 8, .4, .4, .4, .02);
        }
        domain.level.sendParticles(ParticleTypes.EXPLOSION, x, domain.center.y + .5, z, 2, 0, 0, 0, 0);
        hurt(domain, new Vec3(x, domain.center.y + .5, z), 3, 6, true);
    }

    private static void hurt(ActiveDomain domain, Vec3 point, double radius, float damage, boolean knockback) {
        AABB box = new AABB(point, point).inflate(radius);
        for (LivingEntity entity : domain.level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (entity.getUUID().equals(domain.ownerId)) continue;
            entity.hurt(domain.level.damageSources().inFire(), damage);
            if (knockback) entity.knockback(.35, point.x - entity.getX(), point.z - entity.getZ());
        }
    }

    private static void finish(ActiveDomain domain) {
        Vec3 center = domain.center;
        domain.level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 3, .7F);
        domain.level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1, center.z, 1, 0, 0, 0, 0);
        domain.level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 1, center.z, 200, 4, 3, 4, .15);
        domain.level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 1, center.z, 200, 4, 2, 4, .12);
        for (LivingEntity entity : domain.level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(4), LivingEntity::isAlive)) {
            if (entity.getUUID().equals(domain.ownerId)) continue;
            entity.hurt(domain.level.damageSources().explosion(null), 24);
            entity.knockback(1.2, center.x - entity.getX(), center.z - entity.getZ());
        }
    }

    private static final class ActiveDomain {
        private final UUID ownerId;
        private final ServerLevel level;
        private final Vec3 center;
        private int age;

        private ActiveDomain(UUID ownerId, ServerLevel level, Vec3 center) {
            this.ownerId = ownerId;
            this.level = level;
            this.center = center;
        }
    }
}
