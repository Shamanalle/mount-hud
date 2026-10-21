<div align="center">

<img src="icon.png" width="128" alt="Mount HUD icon: a horseshoe over a heart and a drumstick, a jump bar below">

# Mount HUD

**Hunger and your horse's health at the same time, and the experience bar that turns into the jump bar only while you jump.**
A client-side riding HUD for Forge, NeoForge and Fabric.

[![Build](https://github.com/Shamanalle/mount-hud/actions/workflows/build.yml/badge.svg)](https://github.com/Shamanalle/mount-hud/actions/workflows/build.yml)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.x%20%7C%2026.x-62b47a)
![Loaders](https://img.shields.io/badge/loaders-Forge%20%7C%20NeoForge%20%7C%20Fabric%20%7C%20Quilt-d08a3e)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

**English** · [Русский](README.ru.md)

</div>

> **In development.** The first release is being prepared; there are no downloads yet. This page describes what the
> mod does and will be filled in with screenshots, files and settings as they land.

## What it does

When you ride a horse, pig, camel, strider or any other living mount, vanilla Minecraft hides two things:

- your **hunger bar** (the mount's hearts take its place), and
- your **experience bar** (the jump bar takes its place, even when you are not jumping).

Mount HUD keeps both:

- **Hunger and the mount's hearts at once.** The mount's hearts go one row above hunger (the air bubbles move up above them), or to the left above your armor, as you choose.
- **Experience while riding.** The experience bar and level stay visible and turn into the jump bar only while you charge a jump (and while a camel's dash recharges). You can also keep the jump bar all the time, as in vanilla, or never show it.

The mod only changes your HUD: it is not needed on the server, and works on any server.

## Planned

- Mount stats: speed in blocks per second, jump height in blocks and health, in the horse's inventory screen and, if you like, on the HUD while riding.
- Compatibility with AppleSkin, Raised, Overflowing Bars, Colorful Hearts, Mantle / Tinkers' Construct, Jade and WTHIT, described here once checked.
- Every Minecraft version from 1.16.5 to 26.3 on Forge, NeoForge and Fabric, in as few files as possible.

## Settings

`config/mounthud.properties`, also changed on the settings screen: through Mod Menu on Fabric, the Config button in the Mods list on Forge and NeoForge.

| Setting | Values | Default | What it does |
|---|---|---|---|
| `enabled` | `true` / `false` | `true` | Everything the mod changes; `false` gives the vanilla HUD |
| `hunger_while_riding` | `true` / `false` | `true` | Keep the hunger bar while riding a mount with health |
| `hearts_position` | `above_hunger` / `left` | `above_hunger` | Where the mount's hearts go while hunger is shown |
| `jump_bar` | `auto` / `always_jump` / `always_experience` | `auto` | `auto`: experience, the jump bar while charging; `always_jump`: as in vanilla; `always_experience`: never the jump bar |

## Languages

English, Russian, Ukrainian, German, Spanish, Portuguese (Brazil), Chinese (Simplified), French, Italian, Polish, Japanese, Korean, Turkish.

## License

[MIT](LICENSE).
