# Traffic Control — Fabric 1.20.4 Port Notes

This file tracks the Fabric 1.20.4 rewrite of CSX8600's Traffic Control mod
(originally Minecraft 1.12.2 Forge). The 1.12.2 source at the repo root is
read-only reference. The Fabric port lives in `./port/`.

`TODO(ka):` marks deferred work. `BLOCKED:` marks work blocked by the environment.

## Environment

- Container has JDK 21; the port compiles/runs with `--release 17` (Java 17
  bytecode), keeping it 1.20.4-compatible.
- Network access to `maven.fabricmc.net`, `meta.fabricmc.net`,
  `libraries.minecraft.net`, `piston-meta.mojang.com`, `repo1.maven.org` and
  `services.gradle.org` works — local `./gradlew build` and `runClient` both run.
- `reference/fabric-example-mod` and `reference/fabric-docs` are git submodules
  (run `git submodule update --init --recursive` to populate them).

## Versions pinned

- `minecraft_version=1.20.4`, `yarn_mappings=1.20.4+build.3`,
  `loader_version=0.15.7`, `fabric_version=0.97.2+1.20.4`,
  `loom_version=1.6-SNAPSHOT`, Gradle 8.8.
- Mod id `trafficcontrol`, group `com.clussmanproductions.trafficcontrol`.

## CI

`.github/workflows/build.yml` builds `port/` on every push to
`claude/traffic-control-fabric-rewrite-rcf0S` (and on `workflow_dispatch`),
using JDK 17, and uploads `port/build/libs/*.jar` as an artifact stamped with
the short commit SHA.

## v1 status — works

- Fabric scaffold: `./gradlew build` produces `port/build/libs/trafficcontrol-1.0.0.jar`;
  `./gradlew runClient` launches Minecraft 1.20.4.
- All assets converted: 80 block models, 83 item models, textures, sounds,
  `sounds.json`, `en_us.json` (from `en_us.lang`), `pack.mcmeta` (`pack_format: 18`).
- 52 blocks registered (`ModBlocks`) as plain static blocks with their converted
  models, each with a `BlockItem`.
- 11 items registered (`ModItems`) as plain items.
- Creative tab `Traffic Control` (`ModItemGroups`) holds all blocks and items.

## TODO(ka) — deferred behavior

- **Rotation.** v1 registers every block with no rotation property. The plan
  calls for vanilla `HorizontalFacingBlock` (4-way) where placement orientation
  matters, and 16-way rotation inside a `BlockEntityRenderer` for traffic
  lights / signs / crossing gates / wig wags. Not yet implemented.
- **VoxelShape hitboxes.** Decorative blocks (cone, drum, etc.) currently use a
  full-cube selection/collision box. The original supplied custom shapes.
- **Block entities + renderers.** ~32 TileEntities and their renderers
  (signs, street lights, traffic lights, crossing gates, wig wags, bells) are
  not ported; their blocks are registered as static blocks for now.
- **Automation.** Traffic-light control-box state machine, its `ScreenHandler`
  /`Screen` GUI, traffic sensors, pedestrian buttons, crossing-gate animation
  and bells are not ported. Crossing/relay automation is to be redstone-driven.
- **concrete_barrier** is a single block in v1 (white). The original had 16
  dye colors — port as 16 blocks or a color property.
- **traffic_light_bulb** is a single item in v1; the original had 16 bulb
  variants used in traffic-light assembly.
- **Recipes.** A representative subset is converted to 1.20.4 data recipes
  under `port/src/main/resources/data/trafficcontrol/recipes/`. The remaining
  1.12-format recipes (metadata items, `forge:ore_dict`) are not yet converted.

## Dropped (per user — no third-party mod integration)

- ImmersiveRailroading scanner / train detection. Crossing/relay automation is
  to be redstone-driven instead.
- OpenComputers card driver and the traffic-light-card item.
- Shunt blocks (`shunt_island`, `shunt_border`) were IR-only; they remain as
  inert decorative blocks.

## Final Status

- **Works:** Fabric scaffold, CI workflow, full asset conversion, 52 blocks +
  11 items registered and placeable, creative tab.
- **TODO:** rotation, VoxelShapes, block entities/renderers, automation + GUI,
  concrete-barrier colors, bulb variants, full recipe conversion.
- **Blocked:** none — the build environment has the network access it needs.
