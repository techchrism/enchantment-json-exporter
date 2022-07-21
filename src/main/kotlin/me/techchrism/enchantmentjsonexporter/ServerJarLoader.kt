package me.techchrism.enchantmentjsonexporter

import com.google.gson.Gson
import net.md_5.specialsource.JarMapping
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.function.Supplier
import java.util.jar.JarInputStream


class ServerJarLoader {
    companion object {
        const val VERSION = "1.2.0"
        
        fun loadVersionManifest(): VersionManifestV2 {
            val jsonText = URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").readText()
            val gson = Gson()
            return gson.fromJson(jsonText, VersionManifestV2::class.java)
        }
        
        fun loadVersionJson(url: String): VersionJson {
            val jsonText = URL(url).readText()
            val gson = Gson()
            return gson.fromJson(jsonText, VersionJson::class.java)
        }
        
        fun getEnchantmentsFor(version: String? = null): String? {
            val manifest = loadVersionManifest()
            val targetVersion = version ?: manifest.latest.release
            val foundVersion = manifest.versions.find { it.id == targetVersion } ?: return null
            return getEnchantmentsForVersionJson(loadVersionJson(foundVersion.url))
        }
        
        fun getEnchantmentsForVersionJson(version: VersionJson): String {
            return remapJarForEnchantments(
                JarInputStream(URL(version.downloads.server.url).openStream()),
                URL(version.downloads.server_mappings.url).openStream()
            )
        }
        
        fun remapJarForEnchantments(bundledJarStream: JarInputStream, mappings: InputStream): String {
            val mapping = JarMapping()
            BufferedReader(InputStreamReader(mappings)).use { 
                mapping.loadMappings(it, null, null, false)
            }

            val remappedLoader = RemappedClassLoader(Thread.currentThread().contextClassLoader, mapping, bundledJarStream)
            val executorClass = remappedLoader.reloadClass(Executor::class.java)
            val executor = executorClass.constructors.iterator().next().newInstance() as Supplier<String>
            return executor.get()
        }
    }
}