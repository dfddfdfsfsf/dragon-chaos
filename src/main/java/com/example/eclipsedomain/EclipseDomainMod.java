package com.example.eclipsedomain;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.NeoForge;

@Mod(EclipseDomainMod.MOD_ID)
public final class EclipseDomainMod {
    public static final String MOD_ID = "eclipse_domain";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredItem<Item> ECLIPSE_SEAL = ITEMS.register("eclipse_seal",
            () -> new EclipseSealItem(new Item.Properties().stacksTo(1)));

    public EclipseDomainMod(IEventBus modBus) {
        ITEMS.register(modBus);
        NeoForge.EVENT_BUS.addListener(EclipseDomain::tickServer);
    }
}
