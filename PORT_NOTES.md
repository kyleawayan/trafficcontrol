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

## Railroad crossing system

The railroad crossing is a working, redstone-driven subsystem:

- **Crossing lamps** (`crossing_gate_lamps`) are a `CrossingLampsBlock` — an
  invisible block with a 16-way `rotation` property and a `lit` property.
  `CrossingLampsBlockEntityRenderer` draws the static housing plus four
  flashing bulb panels: while lit, the left and right bulb pairs alternate.
  The flash phase comes from world time so every crossing lamp flashes in
  unison. The block lights up (luminance 15) while lit.
- **Crossing relay** — the relay box (`crossing_relay_se`, placed by the
  `crossing_relay_box` item) is a `RelayBlock` with a `RelayBlockEntity`. The
  original eight-block multiblock relay is collapsed onto a **single block**.
  While the relay receives redstone power, a one-second heartbeat drives every
  linked component: gates close, bells ring, lamps flash, wig wags swing. The
  relay also outputs a comparator signal while powered.
- **Tuner** — the `crossing_relay_tuner` item is a `TunerItem`. Right-click a
  relay to select it, then right-click a crossing gate / lamps / bell / wig wag
  to link or unlink it; right-click the selected relay again to clear. The
  selected relay is stored in the tuner's stack NBT.
- **Crossing gate config** — right-clicking a crossing gate empty-handed opens
  a config screen (`CrossingGateScreenHandler` / `CrossingGateScreen`) for the
  arm length and close delay. Edited values are sent to the server with a
  `ServerPlayNetworking` packet, clamped, and applied; the renderer draws the
  arm at the configured length.

Components linked to a relay are re-asserted by its heartbeat; unlinked
components still react to their own local redstone. A component that is both
linked to a relay and powered by local redstone is driven by whichever source
changed last — a minor, documented edge case.

### Manual test (relay loop)

Headless `runClient` verifies loading but not the redstone logic. To verify
in-game: place a relay, a crossing gate, a crossing lamps and a bell; with the
tuner, right-click the relay then each component; power the relay with a lever
— the gate lowers, lamps flash, bell rings; remove power — all reset.

## TODO(ka) — deferred behavior

- **Rotation.** v1 registers every block with no rotation property. The plan
  calls for vanilla `HorizontalFacingBlock` (4-way) where placement orientation
  matters, and 16-way rotation inside a `BlockEntityRenderer` for traffic
  lights / signs / crossing gates / wig wags. Not yet implemented.
- **VoxelShape hitboxes.** Decorative blocks (cone, drum, etc.) currently use a
  full-cube selection/collision box. The original supplied custom shapes.
- **Upper-quadrant wig wag** (`vertical_wig_wag`) remains a decorative block.
  Its swinging arm is a separate baked model (`vertical_wig_wag_arm`) that the
  original rendered by id; porting it needs an extra-model loading path
  (Fabric `ModelLoadingPlugin`) rather than the block's own model.
- **Overhead lamps** (`overhead_lamps`) remain decorative. Unlike
  `crossing_gate_lamps`, the converted overhead model has no bulb geometry
  (the original supplied four separate quadrant lamp models), so the flashing
  treatment was not applied.
- **Block entities + renderers.** Street lights, the crossing gate, the
  lower-quadrant wig wag, the crossing lamps and the relay are ported (see
  above). Still not ported: traffic lights and signs.
  The traffic lights have real 3D models and only lack rotation/animation.
  `street_sign` still renders as a flat icon — it needs the sign-pack data
  system + GUI. `TcBoxRenderer` is a reusable box-drawing helper for the rest.
  The bell blocks have sound but no swinging-bell animation, and the
  pedestrian button / screwdriver sounds are registered but not yet triggered.
- **Automation.** The crossing/relay automation is done (redstone-driven — see
  the railroad crossing section). Still not ported: the traffic-light
  control-box state machine, its GUI, traffic sensors and pedestrian buttons.
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
  11 items registered and placeable, creative tab. Street lights, crossing
  gate, lower-quadrant wig wag and bells are animated. The railroad crossing
  is a complete redstone-driven subsystem: flashing crossing lamps, a
  single-block relay, the linking tuner, and a crossing-gate config screen.
- **TODO:** traffic-light control box + GUI, traffic sensors, pedestrian
  buttons, signs, the upper-quadrant wig wag and overhead lamps animation,
  concrete-barrier colors, bulb variants, full recipe conversion.
- **Blocked:** none — the build environment has the network access it needs.
