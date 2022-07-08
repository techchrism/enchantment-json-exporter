@file:JvmName("CommandLine")
package me.techchrism.enchantmentjsonexporter

import com.google.gson.GsonBuilder
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap

fun main(args: Array<String>) {
    // Set up Minecraft constants and bootstrap
    val stdout = System.out
    SharedConstants.tryDetectVersion()
    Bootstrap.bootStrap()
    System.setOut(stdout)

    // Print generated json
    val gson = GsonBuilder().setPrettyPrinting().create()
    println(gson.toJson(EnchantmentJSONExporter.generate()))
}