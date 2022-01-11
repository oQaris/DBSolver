package com.pryanik.dbsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.github.shiguruikai.combinatoricskt.Combinatorics.combinations as cmb1

class FuncTest {

    @Test
    fun combinationsTest() {
        val data3 = listOf("A", "B", "C")
        assertEquals(
            cmb1(data3, 0).toList().norm(), listOf(setOf<String>()).norm()
        )
        assertEquals(
            cmb1(data3, 5).toList().norm(), listOf<Set<String>>().norm()
        )
        assertEquals(
            cmb1(data3, 1).toList().norm(),
            listOf(setOf("A"), setOf("B"), setOf("C")).norm()
        )
        assertEquals(
            cmb1(data3, 2).toList().norm(),
            listOf(setOf("A", "B"), setOf("A", "C"), setOf("B", "C")).norm()
        )
        assertEquals(
            cmb1(data3, 3).toList().norm(),
            listOf(setOf("A", "B", "C")).norm()
        )

        assertEquals(
            cmb2(data3, 0).norm(), listOf(setOf<String>()).norm()
        )
        assertEquals(
            cmb2(data3, 5).norm(), listOf<Set<String>>().norm()
        )
        assertEquals(
            cmb2(data3, 1).norm(),
            listOf(setOf("A"), setOf("B"), setOf("C")).norm()
        )
        assertEquals(
            cmb2(data3, 2).norm(),
            listOf(setOf("A", "B"), setOf("A", "C"), setOf("B", "C")).norm()
        )
        assertEquals(
            cmb2(data3, 3).norm(),
            listOf(setOf("A", "B", "C")).norm()
        )
    }

    //TODO удалить после тестов (старая версия)
    private fun cmb2(
        arr: List<String>,
        len: Int,
        startPosition: Int = 0,
        result: Array<String> = Array(len) { " " },
        otv: MutableList<Set<String>> = mutableListOf()
    ): MutableList<Set<String>> {
        if (len == 0) {
            val set = mutableSetOf<String>()
            for (card in result) {
                set.add(card)
            }
            otv.add(set)
        } else for (i in startPosition..arr.size - len) {
            result[result.size - len] = arr[i]
            cmb2(arr, len - 1, i + 1, result, otv)
        }
        return otv
    }

    private fun Collection<Collection<String>>.norm(): MutableList<List<String>> {
        val out = mutableListOf<List<String>>()
        this.forEach { out.add(it.toList()) }
        return out
    }
}