package me.techchrism.enchantmentjsonexporter

import net.md_5.specialsource.repo.CachingRepo
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

class ExtractedClassRepo(private val resources: Map<String, ByteArray>) : CachingRepo() {
    override fun findClass0(internalName: String?): ClassNode? {
        val bytes = resources[internalName] ?: return null
        val cr = ClassReader(bytes)
        val node = ClassNode()
        cr.accept(node, 0)
        return node
    }
}