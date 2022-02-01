package com.pryanik.dbsolver

import com.pryanik.dbsolver.logic.algorithms.*
import com.pryanik.dbsolver.logic.parseRelations
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DecompositionTest {
    private val rel = parseRelations(
        listOf(
            "A" to "C B",
            "C" to "D E",
            "F" to "I",
            "A F" to "G H"
        )
    )

    @Test
    fun to2NF_Test() {
        Assertions.assertEquals(
            listOf(
                listOf("A", "B", "C", "D", "E").toRelation(),
                listOf("F", "I").toRelation(),
                listOf("A", "F", "G", "H").toRelation()
            ).toDecomposition(),
            to2NF(rel)
        )
    }

    @Test
    fun to3NF_Test() {
        Assertions.assertEquals(
            listOf(
                listOf("A", "B", "C").toRelation(),
                listOf("C", "D", "E").toRelation(),
                listOf("F", "I").toRelation(),
                listOf("A", "F", "G", "H").toRelation()
            ).toDecomposition(),
            to3NF(rel)
        )
    }

    @Test
    fun toBCNF_Test() {
        Assertions.assertEquals(
            to3NF(rel),
            toBCNF(rel, rel.toUniversalDcmp())
        )

        val fds = parseRelations(
            listOf(
                "k1" to "k3",
                "k3" to "k1",
                "k1 k2" to "a1",
                "k2 k3" to "a1"
            )
        )
        Assertions.assertEquals(
            listOf(
                listOf("k1", "k3").toRelation(),
                listOf("k1", "k2", "a1").toRelation()
            ).toDecomposition(),
            toBCNF(fds, fds.toUniversalDcmp())
        )
    }
}