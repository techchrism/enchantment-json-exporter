package me.techchrism.enchantmentjsonexporter

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.SharedConstants
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentCategory
import java.lang.Integer.max

class EnchantmentJSONExporter {
    companion object {
        fun generate(): JsonObject {
            val typeField = Enchantment::class.java.getDeclaredField("category")
            typeField.isAccessible = true

            val rootObj = JsonObject()
            rootObj.addProperty("version", SharedConstants.getCurrentVersion().releaseTarget)
            rootObj.addProperty("exporter_version", "1.0.0")
            
            // Rarities
            val rarities = JsonArray()
            for (rarity in Enchantment.Rarity.values()) {
                val rarityObject = JsonObject()
                rarityObject.addProperty("name", rarity.name)
                rarityObject.addProperty("weight", rarity.weight)
                val itemCost = run {
                    when (rarity) {
                        Enchantment.Rarity.COMMON -> 1
                        Enchantment.Rarity.UNCOMMON -> 2
                        Enchantment.Rarity.RARE -> 4
                        Enchantment.Rarity.VERY_RARE -> 8
                    }
                }
                rarityObject.addProperty("item_cost", itemCost)
                rarityObject.addProperty("book_cost", max(1, itemCost / 2))
                
                rarities.add(rarityObject)
            }
            rootObj.add("rarities", rarities);

            // Categories
            val categories = JsonArray()
            for (category in EnchantmentCategory.values()) {
                val categoryObject = JsonObject()
                categoryObject.addProperty("name", category.name)
                val itemsArray = JsonArray()
                for ((key, item) in Registry.ITEM.entrySet()) {
                    if (category.canEnchant(item)) {
                        val itemObject = JsonObject()
                        itemObject.addProperty("id", key.location().toString())
                        itemObject.addProperty("name", item.description.string)
                        itemsArray.add(itemObject)
                    }
                }
                categoryObject.add("items", itemsArray)
                categories.add(categoryObject)
            }
            rootObj.add("categories", categories)
            
            // Enchantments
            val enchantsArray = JsonArray()
            for ((key, e) in Registry.ENCHANTMENT.entrySet()) {
                val enchantObject = JsonObject()
                enchantObject.addProperty("id", key.location().toString())
                enchantObject.addProperty("name", Component.translatable(e.descriptionId).string)
                enchantObject.addProperty("category", (typeField[e] as EnchantmentCategory).name)
                enchantObject.addProperty("min_level", e.minLevel)
                enchantObject.addProperty("max_level", e.maxLevel)
                enchantObject.addProperty("rarity", e.rarity.name)
                enchantObject.addProperty("is_curse", e.isCurse)
                enchantObject.addProperty("is_discoverable", e.isDiscoverable)
                enchantObject.addProperty("is_tradeable", e.isTradeable)
                enchantObject.addProperty("is_treasure_only", e.isTreasureOnly)
                
                // Go through enchantments to determine what's incompatible
                val incompatibles = JsonArray()
                for ((checkKey, value) in Registry.ENCHANTMENT.entrySet()) {
                    if (checkKey == key) continue
                    if (!e.isCompatibleWith(value)) {
                        incompatibles.add(checkKey.location().toString())
                    }
                }
                enchantObject.add("incompatible", incompatibles)
                enchantsArray.add(enchantObject)
            }
            rootObj.add("enchantments", enchantsArray);
            
            return rootObj
        }
    }
}