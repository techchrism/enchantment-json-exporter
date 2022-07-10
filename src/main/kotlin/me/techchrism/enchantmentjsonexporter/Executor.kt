package me.techchrism.enchantmentjsonexporter

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.SharedConstants
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentCategory
import java.io.OutputStream
import java.io.PrintStream
import java.util.function.Supplier

class Executor : Supplier<String> {
    override fun get(): String {
        // Set up Minecraft constants and bootstrap
        val stdout = System.out
        val stderr = System.err
        System.setErr(PrintStream(object : OutputStream() {
            override fun write(b: Int) {}
        }))
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        System.setOut(stdout)
        System.setErr(stderr)

        // Return generated json
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(generate())
    }

    private fun generate(): JsonObject {
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
            val itemCost = when (rarity) {
                Enchantment.Rarity.COMMON -> 1
                Enchantment.Rarity.UNCOMMON -> 2
                Enchantment.Rarity.RARE -> 4
                Enchantment.Rarity.VERY_RARE -> 8
            }
            rarityObject.addProperty("item_cost", itemCost)
            rarityObject.addProperty("book_cost", Integer.max(1, itemCost / 2))

            rarities.add(rarityObject)
        }

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