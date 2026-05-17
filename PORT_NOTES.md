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
- All blocks render on the cutout layer (`ModRenderers`) so transparent texture
  regions are not drawn opaque.
- **Street lights** (single + double) are ported: a `StreetLightBlock`
  (`BlockEntityProvider`, 16-way `rotation` property, invisible block model)
  drawn by `StreetLightBlockEntityRenderer` — the multi-block-tall post, arm(s)
  and lamp(s) are emitted box-for-box from the original TESR. Rotation is set
  from player yaw on placement. The block emits light directly (luminance 15)
  instead of the original's multi-block `light_source` placement.
- **Crossing gate** and **wig wag** (lower quadrant) are ported as animated
  block entities (`CrossingGateBlock`, `WigWagBlock` — invisible blocks with a
  16-way `rotation` property). Their renderers draw the static housing model
  plus the moving parts box-for-box from the original TESRs. The animation is
  **redstone-driven**: a powered crossing gate lowers its arm (and raises it
  when unpowered); a powered wig wag swings its banner. The moving angle is
  animated client-side; the powered flag is server-authoritative and synced.
- **Sounds** are wired up (`ModSounds` registers the eight sound events from
  `sounds.json`). The wig wag rings its bell each time the banner passes a
  side; the crossing gate loops a motor sound while the arm moves; and the
  four bell blocks (`wch_bell`, `wch_mechanical_bell`, `safetran_type_3`,
  `safetran_mechanical`) are `BellBlock`s that loop their ring while powered
  by redstone. Looping sounds use a client-side `LoopingSoundInstance`.

## TODO(ka) — deferred behavior

- **Rotation.** v1 registers every block with no rotation property. The plan
  calls for vanilla `HorizontalFacingBlock` (4-way) where placement orientation
  matters, and 16-way rotation inside a `BlockEntityRenderer` for traffic
  lights / signs / crossing gates / wig wags. Not yet implemented.
- **VoxelShape hitboxes.** Decorative blocks (cone, drum, etc.) currently use a
  full-cube selection/collision box. The original supplied custom shapes.
- **Block entities + renderers.** Street lights, the crossing gate and the
  lower-quadrant wig wag are ported (see above). Still not ported: the
  upper-quadrant wig wag (`vertical_wig_wag` — its arm is a separate baked
  model, needing a different render path), traffic lights, bells, and signs.
  The traffic lights have real 3D models and only lack rotation/animation.
  `street_sign` still renders as a flat icon — it needs the sign-pack data
  system + GUI. `TcBoxRenderer` is a reusable box-drawing helper for the rest.
  The bell blocks have sound but no swinging-bell animation, and the
  pedestrian button / screwdriver sounds are registered but not yet triggered.
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
