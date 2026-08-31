# Changelog

## 1.1.0 - 2026-08-31

### Added

- English and Russian localization for commands and every GUI screen, with English as the default.
- Recipe permission, minimum-level, world allow-list, and automation conditions.
- Item tags, item flags, API tag queries, direct item/recipe commands, target grants, and tab completion.
- Configurable storage folders constrained to the plugin data directory.

### Fixed

- Eliminated an item duplication vulnerability in the recipe editor by using virtual slot copies.
- Revalidated recipes during the actual craft event and corrected normalized shaped matching, including 2x2 grids.
- Prevented item loss when a target inventory is full.
- Preserved durability and repair cost while upgrading old item versions.
- Safely skipped unsafe IDs, malformed definitions, and unsupported language values.

## 1.0.0 - 2026-08-26

### Added

- Stable PDC-backed custom items with versions and `item_model` support.
- Exact custom ingredients for shaped and shapeless recipes.
- Indexed recipe matching and optional priority over conflicting vanilla recipes.
- In-game item browser, recipe browser, and recipe editor.
- Bukkit Services API for item lookup, giving, and upgrades.
