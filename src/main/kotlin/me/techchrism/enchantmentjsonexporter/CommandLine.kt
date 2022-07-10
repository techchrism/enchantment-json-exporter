@file:JvmName("CommandLine")
package me.techchrism.enchantmentjsonexporter

import java.io.FileInputStream
import java.util.jar.JarInputStream

fun main(args: Array<String>) {
    if(args.size < 2) {
        println("Need bundled server jar and mappings file")
        return
    }
    
    ServerJarLoader.remapJar(JarInputStream(FileInputStream(args[0])), FileInputStream(args[1]))
}
