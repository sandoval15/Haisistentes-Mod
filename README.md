# Haisistente

A Minecraft **Forge 1.20.1** mod that adds *Haisistentes* — tameable companion
creatures with custom models and animations powered by GeckoLib. Originally
prototyped in MCreator and since migrated to a hand-written Forge codebase.

## Features

- **10 Haisistente variants**: Haisistente, Jenn, Lux, Isabella, Pixel, Tate,
  Flou, Lilac, Another and Zombie — each with its own model, texture and
  animation set.
- **Taming**: feed them bamboo (or **Golden Bamboo**, which always succeeds)
  to tame them. Tamed Haisistentes follow you, defend you, sit on command and
  sleep next to you when you go to bed.
- **Dancing**: play a music disc in a jukebox nearby and they will dance
  (6 different dances).
- **Lux**: a flying variant with an 18-slot personal inventory
  (sneak + right-click as its owner to open it).
- **Curable zombie**: the Haisistente Zombie can be cured like a zombie
  villager — throw a splash potion of Weakness at it and feed it bamboo
  (or Golden Bamboo); after a few minutes it converts into a normal
  Haisistente.
- **The Haisillage**: a custom village structure that generates in bamboo
  jungles (`/locate structure haisistente:aldea`), with houses, farms, paths
  and chest loot.
- **Plush Haise**: a decorative plush block.

## Requirements

| Tool | Version |
|---|---|
| Java (JDK) | 17 |
| Minecraft | 1.20.1 |
| Forge | 47.2.0+ |
| [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) | 4.4.x (**required** at runtime) |
| [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) | optional |
| [Kleiders Player Renderer](https://www.curseforge.com/minecraft/mc-mods/kleiders-custom-renderer) | optional |

## Development

```powershell
# Run the dev client (first run downloads and decompiles Minecraft — be patient)
.\gradlew.bat runClient

# Run a dev dedicated server
.\gradlew.bat runServer

# Build the distributable jar -> build\libs\haisistente-1.0.0.jar
.\gradlew.bat build
```

To play it in a regular launcher, install Forge 1.20.1 and drop the built jar
plus the GeckoLib jar into your `mods` folder.

## Project layout

```
src/main/java/net/anzhi/haisistente/
├── HaisistenteMod.java      # Mod entrypoint, network channel
├── block/                   # Plush Haise block
├── client/renderer/         # GeckoLib entity renderer
├── entity/                  # Entity classes (HaisistenteAbstract is the base)
│   ├── flag/                # Animation frame-flag sync (client -> server)
│   ├── layer/               # Render layers (held food item)
│   ├── lux/                 # Lux inventory menu/screen and hat
│   └── model/               # GeoModel (texture/model/animation lookup)
├── goal/                    # Custom AI goals (flying follow)
├── init/                    # Deferred registers (entities, items, blocks...)
└── procedures/              # Event handlers (jukebox dancing)

src/main/resources/
├── assets/haisistente/
│   ├── animations/          # GeckoLib animation JSONs (outfit_*.animation.json)
│   ├── geo/                 # GeckoLib model JSONs (outfit_*.geo.json)
│   ├── textures/            # entities/, item/, block/, screens/
│   └── lang/                # en_us.json
└── data/haisistente/
    ├── loot_tables/         # Block drops and village chest loot
    ├── structures/          # Haisillage building NBT pieces
    └── worldgen/            # Jigsaw structure, structure set, template pools
```

### Conventions

- Registry IDs (`haisistente:peluchehaise`, `haisistente:golden_bamboo`,
  `haisistente:aldea`, ...) are **frozen** — renaming them breaks existing
  worlds and the structure NBT files.
- Bone names inside `geo/` and `animations/` JSONs are referenced by the
  animation files and by code (`head`, `obj`) — do not rename them casually.
- Entity classes pick their assets via `getTexture()` / `getModel()` /
  `getGeoAnimation()` in their class under `entity/`.

## Credits

- **INTEL** — design, models, textures and animations.
- Built with [GeckoLib](https://github.com/bernie-g/geckolib).
