# Shared Fortune

有福同享 (Shared Fortune) is a NeoForge mod for Minecraft 1.21.1. Linked players share damage and healing.

## Development

- Java: 21
- Minecraft: 1.21.1
- NeoForge: 21.1.248
- Version: `0.5.0`
- Mod ID: `sharedfortune`
- Item ID: `shared_fortune`
- Package: `com.tanrunn.sharedfortune`

Link data is stored as `SoulLink` records in `SharedFortuneSavedData` and persisted to the server world.

Operators with permission level 2 can inspect the current link with:

```text
/sharedfortune debug
```

Players can use a `contract_certificate` on their linked partner to increase the link level.

Players can remove their own link with `/sharedfortune unlink`. Operators can remove another player's link with `/sharedfortune admin unlink <player>`.

Build the mod with:

```sh
./gradlew build
```

The resulting JAR is written to `build/libs/`.
