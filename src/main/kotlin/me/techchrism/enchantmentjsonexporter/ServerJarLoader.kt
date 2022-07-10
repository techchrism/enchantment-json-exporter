package me.techchrism.enchantmentjsonexporter

import com.google.gson.Gson
import net.md_5.specialsource.JarMapping
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.jar.JarInputStream


class ServerJarLoader {
    companion object {
        fun loadVersionManifest(): VersionManifestV2 {
            val jsonText = URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").readText()
            val gson = Gson()
            return gson.fromJson(jsonText, VersionManifestV2::class.java)
        }
        
        fun remapJar(bundledJarStream: JarInputStream, mappings: InputStream) {
            val mapping = JarMapping()
            BufferedReader(InputStreamReader(mappings)).use { 
                mapping.loadMappings(it, null, null, false)
            }

            val remappedLoader = RemappedClassLoader(Thread.currentThread().contextClassLoader, mapping, bundledJarStream)
            val executorClass = remappedLoader.reloadClass(Executor::class.java)
            val executor = executorClass.constructors.iterator().next().newInstance() as Runnable
            executor.run()
        }
    }
}