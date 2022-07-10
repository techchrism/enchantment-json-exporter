@file:JvmName("CommandLine")
package me.techchrism.enchantmentjsonexporter

fun main(args: Array<String>) {
    print(ServerJarLoader.getEnchantmentsFor())
}
