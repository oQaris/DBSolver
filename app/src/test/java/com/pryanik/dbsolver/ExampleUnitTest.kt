package com.pryanik.dbsolver

import com.pryanik.dbsolver.logic.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AlgorithmsTest {
    private lateinit var rel4: Relations
    private lateinit var rel5: Relations

    @Before
    fun setUp() {
        rel4 = parseRelations(
            listOf(
                "A B" to "C",
                "C" to "D",
                "D" to "A"
            )
        )
        rel5 = parseRelations(
            listOf(
                "A" to "A C B",
                "C" to "D E",
                "F" to "I",
                "A F" to "G I H",
                "C B A" to "B D"
            )
        )
    }

    @Test
    fun minCoverTest() {
        assertEquals(
            parseRelations(
                listOf(
                    "A" to "C B",
                    "C" to "D E",
                    "F" to "I",
                    "A F" to "G H"
                )
            ), minCover(rel5)
        )
    }

    @Test
    fun minKeysTest() {
        assertEquals(setOf(setOf("A", "F")), minKeys(rel5))
    }

    @Test
    fun nonTrivialFDsTest() {
        assertEquals(
            parseRelations(
                listOf(
                    "C" to "D",
                    "C" to "A",
                    "D" to "A",
                    "A B" to "C",
                    "A B" to "D",
                    "A C" to "D",
                    "B C" to "A",
                    "B C" to "D",
                    "B D" to "A",
                    "B D" to "C",
                    "C D" to "A",
                    "A B C" to "D",
                    "A B D" to "C",
                    "B C D" to "A",
                )
            ), nonTrivialFDs(rel4)
        )
    }

    @Test
    fun decompositionTest() {
        assertEquals(
            setOf(
                setOf("A", "B", "C"),
                setOf("C", "D", "E"),
                setOf("F", "I"),
                setOf("A", "F", "G", "H")
            ), decomposition(rel5)
        )
    }

    @Test
    fun isLosslessConnectionTest() {
        val rel = parseRelations(
            listOf(
                "A" to "B",
                "C" to "D",
            )
        )
        assertFalse(isLosslessJoin(rel, decomposition(rel), true))
    }

    @Test
    fun isFuncDepPersistenceTest() {
        assertTrue(isFuncDepPersistence(rel5, decomposition(rel5)))
    }
}