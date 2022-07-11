# Enchantment JSON Exporter
Automatically export Minecraft enchantments as JSON

Enchantments for version 1.19+ can be viewed at <https://techchrism.github.io/enchantment-json-exporter/versions.json>

 - Downloads bundled server jar and mappings file from Mojang
 - Dynamically deobfuscates classes on the fly with [SpecialSource](https://github.com/md-5/SpecialSource)
 - Bootstraps the server jar to load the registry and iterates over enchantments

All in memory - without writing to disk