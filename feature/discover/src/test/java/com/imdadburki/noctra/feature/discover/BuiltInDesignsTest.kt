package com.imdadburki.noctra.feature.discover

import com.imdadburki.noctra.core.common.Collections
import com.imdadburki.noctra.core.common.builtInDesigns
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInDesignsTest {

    @Test
    fun `collections total 70 wallpapers`() {
        val total = Collections.ALL.sumOf { it.second.size }
        assertEquals(70, total)
        assertEquals(70, builtInDesigns().size)
    }

    @Test
    fun `every design has a unique id`() {
        val ids = builtInDesigns().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every design title belongs to its collection`() {
        for (design in builtInDesigns()) {
            val names = Collections.ALL.first { it.first == design.collection }.second
            assertTrue(names.contains(design.title))
        }
    }

    @Test
    fun `seven collections exist`() {
        assertEquals(7, Collections.ALL.size)
    }
}
