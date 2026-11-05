# Materialism Core

Auxiliary NeoForge mod for the [Materialism](https://github.com/hyperbaton) modpack (Minecraft 1.21.1).

It holds what the pack can't do with scripts alone: new items and blocks, the full society datapack for Care For Them, extensions to TFMG's chemical vat, and fixes that make the pack's mods work together.

## Requirements

| Mod | Needed | Used for |
|---|---|---|
| NeoForge 21.1.216+ | Required | |
| Care For Them | Required | Society datapack |
| CFT-TFC | Required | Society datapack (climate needs) |
| TerraFirmaCraft | Required (through CFT-TFC) | Heat, knapping, chests, fireboxes |
| Create | Optional | Mechanical arm, contraption and boiler integrations |
| TFMG Community Edition 1.3.3+ | Optional | Vat extensions, blast furnace and casting basin changes |
| Firmalife | Optional | Oven fix |
| Vintage Improvements | Optional | Helve hammer fix |

Integrations for an optional mod only apply when that mod is installed. Older TFMG versions, including the original TFMG, are not supported.

## Items and blocks

### Galvanic Cell

A single-use battery. It holds 12,000 FE and can't be recharged. It pushes up to 256 FE per tick into adjacent energy receivers, and other blocks can also pull from it. When broken, it keeps its remaining charge as an item.

### Fluid Scanner

Right-click a tank, a fluid block or anything else holding a fluid to record which fluid it is. If the target has more than one tank, sneak to read the second one. Other mods see the scanner as holding that fluid, but it can never be filled or drained.

### Bone knapping

Adds a TFC knapping type that uses two items from the `materialism:bone_knapping` tag, with recipes for a cleaver, a hacksaw, a hammer and a skinning knife.

## Society

A complete replacement for the default content of Care For Them. The mod's `pack.mcmeta` filters out the classes, needs, structures and jobs from CFT and CFT-TFC, so only these remain:

- **48 social classes:** 8 for each of the 6 cultures (subarctic, temperate, steppe, tropical, desert, mountain), with one humanoid skin per class
- **579 needs:** food, drink, clothing, housing, heating, lighting, rituals, climate and more
- **148 jobs:** including farmers, guards, healers, merchants, builders and ritual officiants
- **140 structures:** 48 class houses plus workplaces, gathering places and production sites

Names for all of them are included in the language file.

## Chemical vat extensions (TFMG)

`tfmg:vat` recipes accept four extra optional fields. They all default to 0, which leaves the recipe working as TFMG defines it.

| Field | Meaning |
|---|---|
| `electrode_wear` | Durability each electrode used by the recipe loses per finished craft |
| `min_current` | Amps each of those electrodes needs, otherwise the recipe pauses |
| `energy` | Total FE those electrodes must draw before the recipe can finish |
| `temperature` | Minimum vat temperature in °C, otherwise the recipe pauses |

```json
{
  "type": "tfmg:vat",
  "machines": ["tfmg:graphite_electrode", "tfmg:graphite_electrode"],
  "processing_time": 300,
  "electrode_wear": 5,
  "min_current": 5,
  "energy": 9000000,
  "temperature": 800
}
```

The requirements appear in JEI, and the vat's goggle tooltip shows its temperature and energy progress.

### Electrodes

Electrodes have durability, set per type in `config/materialismcore-startup.toml` (1000 by default). Changing it needs a restart, and the value should match on the server and clients. An electrode that runs out breaks, and the recipe can't continue until it's replaced.

### Electricity

`min_current` and `energy` only apply to recipes that use electrodes.

- **Current** is voltage ÷ resistance: copper and zinc electrodes are 10 Ω, graphite is 300 Ω. A recipe can't go below TFMG's global `electrolysisMinimumCurrent`.
- **Power** per electrode is voltage² ÷ resistance, in FE per tick. Electrodes draw this constantly, even when the vat is idle.
- **Craft time** is the longer of `processing_time` and `energy` ÷ power. For `energy` to have any effect, it must be higher than the power at `min_current` × `processing_time`.

### Temperature

The vat accepts heat the same way a TFC crucible does, so fireboxes, firepits and charcoal forges placed underneath heat it directly. It heats towards the average across its whole footprint, so a wide vat needs a heater under every column. Temperature changes gradually, at TFC's device heating rate (1 °C per tick by default).

Create-style heaters have no real temperature: heated counts as 600 °C and superheated as 1200 °C. `heat_level` and freezers keep working as in TFMG.

## Mod integrations and fixes

### TFMG

- **Blast furnace:** items thrown into the opening no longer spill into the ore slot. Anything that doesn't fit in its own slot stays in the opening.
- **Blast furnace retrieval:** right-click the output block with an empty hand to take back the items in its ore and flux slots.
- **Casting basin:** holds 100 mB instead of 90 mB, matching TFC's ingot size.

### TerraFirmaCraft and Create

- **TFC fireboxes** work as boiler heaters for Create, based on their real temperature.
- **Mechanical arms** can deposit fuel into TFC fireboxes.
- **TFC chests** work as contraption storage, including addon chests built on them. They keep TFC's item size limits and menu while moving.

### Other mods

- **Vintage Improvements:** items processed by the helve hammer keep the temperature of their inputs.
- **Firmalife:** the oven now declares TFC's `IHeatable`, matching how it already heats items.
- **Create JEI:** sequenced assembly pages with more than 6 steps now fit in the recipe frame, and every step gets a numeral.

### Crash fixes

- **Create-based mods:** reading a config value before its file has loaded now falls back to the default value instead of crashing the server.
- **TFC Eratosthenes:** the map projection is now calculated from the mod's own config when it hasn't been set yet, instead of crashing the world on the first player tick.

## Building

```
./gradlew build
```

The built jar will be in `build/libs/`.

## License

MIT
