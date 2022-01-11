package com.pryanik.dbsolver

import com.pryanik.dbsolver.logic.*
import com.pryanik.dbsolver.logic.algorithms.toBCNF
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AlgorithmsTest {
    private lateinit var rel4: FuncDeps
    private lateinit var rel5: FuncDeps
    private lateinit var cl: FuncDeps
    private lateinit var exam: FuncDeps

    @BeforeEach
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
        cl = parseRelations(
            listOf(
                "G" to "H",
                "D I" to "E G",
                "A B" to "C D",
                "B I" to "F"
            )
        )
        exam = parseRelations(
            listOf(
                "A" to "B C",
                "C" to "D E",
                "F" to "I",
                "A F" to "H G"
            )
        )
    }

    @Test
    fun examTest() {
        Log.setLogging(true)

        /*Log.ln("№4")
        nonTrivialFDs(exam, true)*/

        Log.ln("№5")
        minCover(exam, true)
        minKeys(exam, true)

        val d = toBCNF(exam)
        assertTrue(isFuncDepPersistence(exam, d, true))
        assertTrue(isLosslessJoin(exam, d, true))

        println(Log.toString())
        Log.restoreLogging()
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
    fun isLosslessJoinTest() {
        val rel = parseRelations(
            listOf(
                "A" to "B",
                "C" to "D",
            )
        )
        assertFalse(isLosslessJoin(rel, toBCNF(rel), true))
    }

    @Test
    fun isFuncDepPersistenceTest() {
        assertTrue(isFuncDepPersistence(rel5, toBCNF(rel5)))
    }

    @Test
    fun otherTest() {
        assertTrue(closure(setOf("I", "A", "B"), cl).size == 9)
    }
}