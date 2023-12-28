# Coprolite (WORK IN PROGRESS)

### Minecraft ~~core~~ server launcher

## Features
- Mixin [(Fabric Wiki)](https://fabricmc.net/wiki/tutorial:mixin_registration) [(Mixin Wiki)](https://github.com/SpongePowered/Mixin/wiki)

## How to run with PaperMC

```shell
java -Xmx4G -Dcoprolite.jar=./paper-1.20.4-340.jar -jar coprolite-launcher-0.0.1.jar nogui
```

## Plugin structure

`coprolite.plugin.json`

```json
{
  "schemaVersion": 0,
  "id": "mod-id",
  "name": "Example Mod",
  "version": "${version}",
  "authors": [
    "Your Name"
  ],
  "mixins": [
    "mod-id.mixins.json"
  ]
}
```

`mod-id.mixins.json`

```json
{
  "required": true,
  "minVersion": "0.8.5",
  "package": "your.plugin.mixin",
  "target": "@env(DEFAULT)",
  "compatibilityLevel": "JAVA_17",
  "mixins": [
    "MixinCraftServer"
  ]
}
```

## Project layout

- `coprolite-api` contains Сoprolite loader api
- `coprolite-loader` contains platform-independent implementation of Сoprolite loader _(if desired, it can be used at least as the client mod loader)_
- `coprolite-launcher` contains launcher for Сoprolite loader, usually for PaperMC and its forks
- `coprolite-paper` contains a couple of Mixins for PaperMC that fix several conflicts

## Credit

Special thanks to [FabricMC](https://fabricmc.net/), a considerable part of the code is taken from fabric-loader

Licensed with Apache-2.0 License
