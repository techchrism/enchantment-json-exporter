package me.techchrism.enchantmentjsonexporter

import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import net.md_5.specialsource.repo.ClassRepo
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarInputStream


class RemappedClassLoader(
        parentLoader: ClassLoader,
        mapping: JarMapping,
        bundledJarStream: JarInputStream
    ) : ClassLoader(parentLoader) {
    
    private val inverseMappedClasses = HashMap<String, String>()
    private val jarContents = HashMap<String, ByteArray>()
    private var remapper: JarRemapper
    private var repo: ClassRepo
    private val manuallyRemapPackages = HashSet<String>()
    
    init {
        println("Loading...")
        val start = System.currentTimeMillis()
        
        // Inverse mapping for quick class calculation
        for((key, value) in mapping.classes) {
            if(inverseMappedClasses.containsKey(value)) {
                println("Warning: Duplicate for $value -> $key")
            }
            inverseMappedClasses[value] = key
        }
        
        // Load library and server jars from bundle
        lateinit var entry: JarEntry
        while(bundledJarStream.nextJarEntry?.also { entry = it } != null) {
            if (entry.isDirectory || !entry.name.endsWith(".jar")) continue

            if (entry.name.startsWith("META-INF/versions/") || entry.name.startsWith("META-INF/libraries/")) {
                val jis = JarInputStream(ByteArrayInputStream(bundledJarStream.readAllBytes()))
                lateinit var subJarEntry: JarEntry
                while(jis.nextJarEntry?.also { subJarEntry = it } != null) {
                    jarContents[subJarEntry.name] = jis.readAllBytes()
                }
            }
        }

        mapping.setFallbackInheritanceProvider(ProcessedInheritanceProvider(jarContents))
        
        repo = ExtractedClassRepo(jarContents)
        remapper = JarRemapper(mapping)

        val end = System.currentTimeMillis()
        println("Done! Took ${end - start}ms")
    }
    
    fun reloadClass(clazz: Class<*>): Class<*> {
        val name = clazz.name
        val classPath = name.replace('.', '/') + ".class"
        val classStream = this.javaClass.classLoader.getResourceAsStream(classPath)
        val bytes = classStream!!.readAllBytes()
        val newClass = defineClass(name, bytes, 0, bytes.size)
        resolveClass(newClass)
        
        manuallyRemapPackages.removeIf { it.startsWith(name) }
        manuallyRemapPackages.add(name)
        
        return newClass
    }
    
    override fun getResourceAsStream(name: String?): InputStream? {
        val contents = jarContents[name] ?: return super.getResourceAsStream(name)
        return ByteArrayInputStream(contents)
    }
    
    override fun loadClass(name: String): Class<*> {
        
        val classPath = name.replace('.', '/')
        
        val obfuscatedName = inverseMappedClasses[classPath]
        if(obfuscatedName != null) {
            val fileName = "$obfuscatedName.class"
            val bytes = jarContents[fileName]
            if(bytes != null) {
                val remappedBytes = remapper.remapClassFile(bytes, repo)
                //println("Dynamically remapped ${obfuscatedName} to ${name}")
                val newClass = defineClass(name, remappedBytes, 0, remappedBytes!!.size)
                resolveClass(newClass)
                return newClass
            }
            
            return super.loadClass(name)
        } else {
            // Search for class in other jars
            val fileName = "$classPath.class"
            val bytes = jarContents[fileName]
            if(bytes != null) {
                val newClass = defineClass(name, bytes, 0, bytes.size)
                resolveClass(newClass)
                return newClass
            }
            
            if(manuallyRemapPackages.any { name.startsWith(it) }) {
                val classByteStream = super.getResourceAsStream("$classPath.class") ?: throw ClassNotFoundException("Custom could not find $name")
                val classBytes = classByteStream.readAllBytes()
                classByteStream.close()

                val newClass = defineClass(name, classBytes, 0, classBytes!!.size)
                resolveClass(newClass)
                return newClass
            }
            return super.loadClass(name)
        }
    }
}