package me.techchrism.enchantmentjsonexporter

import net.md_5.specialsource.provider.InheritanceProvider
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode


class ProcessedInheritanceProvider(private val resources: Map<String, ByteArray>) : InheritanceProvider {
    override fun getParents(className: String): MutableCollection<String>? {
        val node = getNode(className) ?: return null

        val parents: MutableCollection<String> = ArrayList()
        for (iface in node.interfaces) {
            parents.add(iface)
        }
        if (node.superName != null) {
            parents.add(node.superName)
        }
        return parents
    }
    
    private fun getNode(name: String): ClassNode? {
        val bytes = resources["$name.class"] ?: return null
        val cr = ClassReader(bytes)
        val node = ClassNode()
        cr.accept(node, 0)
        return node
    }
}