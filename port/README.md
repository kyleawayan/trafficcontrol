# Traffic Control (Fabric 1.20.1)

A clean-room Fabric rewrite of **Traffic Control**, originally created by **CSX8600**
for Minecraft 1.12.2 Forge.

Traffic Control adds traffic signals, signs, crossing gates, wig wags, road cones,
barriers and other road infrastructure to Minecraft.

This port re-implements the mod's behavior on Fabric for Minecraft 1.20.1 and reuses
the original art assets. It is not affiliated with the original author.

## Credits

- **CSX8600** — original Traffic Control mod
  ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/traffic-control)).
- Fabric 1.20.1 rewrite by Kyle Awayan.

## Building

```sh
cd port
./gradlew build
```

The built jar is written to `port/build/libs/`.

## Running in dev

```sh
cd port
./gradlew runClient
```

## License

MIT — see [LICENSE](LICENSE).
