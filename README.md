<div align="center">
  <h1>mCraft</h1>
  <p>A lightweight custom-item framework and exact crafting engine.</p>
  <p>
    <a href="https://papermc.io/software/paper"><img alt="Paper" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/paper_vector.svg"></a>
    <a href="https://purpurmc.org"><img alt="Purpur" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/purpur_vector.svg"></a>
    <a href="https://papermc.io/software/folia"><img alt="Folia" height="56" src="https://raw.githubusercontent.com/miklires/mCommand/main/docs/assets/folia-available.png"></a>
  </p>
  <p>
    <a href="https://github.com/miklires/mCraft"><img alt="GitHub" src="https://tr7zw.github.io/uikit/social_buttons_icon/Github-Button-64.png"></a>
    <a href="https://modrinth.com/project/mcraft"><img alt="Modrinth" src="https://tr7zw.github.io/uikit/social_buttons_icon/Modrinth-Button-64.png"></a>
    <a href="https://discord.gg/pes25cnWKy"><img alt="Discord" src="https://tr7zw.github.io/uikit/social_buttons_icon/Discord-Button-64.png"></a>
  </p>
  <p>
    <a href="https://bstats.org/plugin/bukkit/mCraft/33357"><img alt="bStats" src="https://img.shields.io/badge/bStats-33357-2F9BE6?style=for-the-badge"></a>
    <a href="https://github.com/miklires/mCraft/releases"><img alt="Release" src="https://img.shields.io/github/v/release/miklires/mCraft?style=for-the-badge"></a>
    <img alt="Java 25" src="https://img.shields.io/badge/Java-25-5382A1?style=for-the-badge">
  </p>
</div>

## What it does

- Defines custom items with stable PDC IDs, versions, MiniMessage names/lore, enchantments, unbreakable state, and modern `item_model` components.
- Creates shaped and shapeless recipes with exact custom-item ingredients.
- Indexes recipe candidates by material instead of scanning every recipe on each grid update.
- Provides an in-game browser and recipe editor; edited recipes are stored as individual YAML files.
- Exposes `MCraftAPI` through Bukkit Services Manager.

Resource-pack generation, blocks, furniture, brewing, machines, and RPG stats are intentionally outside 1.0.0.

## Requirements

- Java 25
- Paper, Purpur, or Folia 26.2

## Install

1. Put `mCraft-1.0.0.jar` in `plugins`.
2. Start the server.
3. Add item definitions under `plugins/mCraft/items` or use the GUI recipe editor.

Items use a stable `mcraft:item_id` PDC tag. Increment an item's `version` after changing its definition; old copies upgrade when selected in a hotbar.

## Commands and permissions

- `/mcraft` — GUI, `mcraft.command.mcraft`
- `/mcraft give <id> [amount]` — `mcraft.command.give`
- `/mcraft reload` — `mcraft.command.reload`
- `mcraft.admin` grants all permissions.

## API

Obtain `MCraftAPI` from Bukkit's Services Manager. It supports item lookup, ID lookup, safe giving, and upgrading older item versions without direct storage access.

## Telemetry and updates

mCraft uses anonymous [bStats metrics](https://bstats.org/plugin/bukkit/mCraft/33357). Disable them with `metrics.enabled: false`. No item contents or player identifiers are collected. Disable public version checks separately with `updates.enabled: false`.

## Build

```bash
./gradlew clean build
```

Licensed under the MIT License.
