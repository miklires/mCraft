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
- Provides a localized in-game browser and virtual recipe editor. Editor previews never consume or duplicate player-owned items.
- Supports recipe permission, minimum-level, world allow-list, and automation conditions.
- Adds item tags and item flags, plus automatic version upgrades that preserve stack amount and durability.
- Exposes `MCraftAPI` through Bukkit Services Manager, including tag queries and overflow-safe item grants.

mCraft uses modern `item_model` components but does not generate or distribute resource packs.

## Requirements

- Java 25
- Paper, Purpur, or Folia 26.2

## Install

1. Put `mCraft-1.1.0.jar` in `plugins`.
2. Start the server.
3. Add item definitions under `plugins/mCraft/items` or use the GUI recipe editor.

Items use stable `mcraft:item_id` and `mcraft:item_version` PDC tags. Increment an item's `version` after changing its definition; old copies upgrade on join, pickup, or hotbar selection without repairing their durability.

English is the default language. Set `language: ru_RU` in `config.yml` and run `/mcraft reload` to switch to Russian. Existing language files receive new default keys automatically.

## Item example

```yaml
id: ruby_sword
material: DIAMOND_SWORD
display-name: "<red>Ruby Sword"
lore:
  - "<gray>Forged from ruby"
version: 2
item-model: "my_pack:ruby_sword"
unbreakable: false
tags: [weapon, ruby]
item-flags: [hide_attributes]
enchantments:
  sharpness: 6
```

IDs must match `[a-z0-9][a-z0-9_-]{0,63}`. Files with invalid IDs or malformed values are skipped with a focused warning instead of disabling the plugin.

## Recipe conditions

The in-game editor creates shaped and shapeless recipes. Advanced conditions can be added to the generated recipe YAML:

```yaml
conditions:
  permission: "mcraft.craft.ruby_sword"
  minimum-level: 15
  worlds: [world, world_nether]
  allow-automation: false
```

Custom ingredients are matched by stable PDC ID, not display name or base material. Vanilla ingredients deliberately reject mCraft custom items made from the same material.

## Commands and permissions

- `/mcraft` — GUI, `mcraft.command.mcraft`
- `/mcraft item <id>` — open an item directly
- `/mcraft recipe <id>` — open a recipe directly
- `/mcraft give <id> [amount] [player]` — `mcraft.command.give`
- `/mcraft reload` — `mcraft.command.reload`
- `mcraft.admin` grants all permissions.

## API

Obtain `MCraftAPI` from Bukkit's Services Manager. It supports item lookup, tag lookup, ID lookup, overflow-safe giving, and upgrading older item versions without direct storage access.

## Telemetry and updates

mCraft uses anonymous [bStats metrics](https://bstats.org/plugin/bukkit/mCraft/33357). Disable them with `metrics.enabled: false`. No item contents or player identifiers are collected. Disable public version checks separately with `updates.enabled: false`.

## Build

```bash
./gradlew clean build
```

Licensed under the MIT License.
