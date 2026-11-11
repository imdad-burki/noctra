package com.imdadburki.noctra.core.clock

import com.imdadburki.noctra.core.common.ClockKind
import com.imdadburki.noctra.core.common.DigitalLayout
import com.imdadburki.noctra.core.common.DigitalTreatment
import com.imdadburki.noctra.core.common.allClockStyles
import com.imdadburki.noctra.core.common.clockFaceByName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The v2 catalogue contract: exactly 150 distinct, named, valid clocks. */
class ClockCatalogTest {

    @Test
    fun catalogueHas150Styles() {
        assertEquals(150, allClockStyles().size)
    }

    @Test
    fun allNamesUniqueAndNonBlank() {
        val names = allClockStyles().map { it.displayName }
        assertTrue(names.all { it.isNotBlank() })
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun digitalCountIs120AndAnalogIs30() {
        val all = allClockStyles()
        assertEquals(120, all.count { it.kind == ClockKind.DIGITAL })
        assertEquals(30, all.count { it.kind == ClockKind.ANALOG })
    }

    @Test
    fun everyFaceParsesToValidLayoutAndTreatment() {
        for (face in allClockStyles()) {
            if (face.kind == ClockKind.DIGITAL) {
                // fromName must round-trip (no silent STANDARD fallback).
                assertEquals(face.digitalLayout, DigitalLayout.fromName(face.digitalLayout).name)
                assertEquals(face.digitalTreatment, DigitalTreatment.fromName(face.digitalTreatment).name)
            }
        }
    }

    @Test
    fun lookupByNameFindsEveryFace() {
        for (face in allClockStyles()) {
            assertEquals(face, clockFaceByName(face.displayName))
        }
    }

    @Test
    fun digitalCoversEveryLayoutAndTreatment() {
        val digital = allClockStyles().filter { it.kind == ClockKind.DIGITAL }
        val layouts = digital.map { it.digitalLayout }.toSet()
        val treatments = digital.map { it.digitalTreatment }.toSet()
        assertEquals(DigitalLayout.entries.map { it.name }.toSet(), layouts)
        assertEquals(DigitalTreatment.entries.map { it.name }.toSet(), treatments)
    }
}
