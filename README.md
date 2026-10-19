# Eclipse Domain MOD (Testing repository)

NeoForge 1.21.1 mod adding the craftable **Eclipse Seal** attack spell. The entire domain VFX is highly inspired in Bloxfruit Roblox Game Transformed Dragon West "C" moveset

## Features

- 20x20 eclipse domain with a rotating flame circle.
- Repeating 20-block fire rays that deal 10 damage per strike.
- Three-block-radius falling meteors with knockback.
- Three-second rain phase followed by a descending final blast.
- Owner immunity to the spell's damage and knockback.
- Vanilla particle and sound fallbacks, so no external visual-effects mod is required.

## Crafting

Craft the Eclipse Seal in a crafting table:

```text
Blaze Powder | Blaze Powder    | Blaze Powder
Dragon Breath| Nether Star     | Dragon Breath
Dragon Breath| Dragon Breath   | Dragon Breath
```

Right-click the item to cast it. The item is consumed unless the player is in Creative mode.

## Building

Requires Java 21. Use the included Gradle wrapper or Gradle 8.13:

```text
gradlew build
```

The compiled mod is written to `build/libs/`.
