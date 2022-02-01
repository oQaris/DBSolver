package com.pryanik.dbsolver.logic.algorithms

import com.github.shiguruikai.combinatoricskt.powerset
import com.pryanik.dbsolver.logic.FuncDeps
import com.pryanik.dbsolver.logic.closure
import com.pryanik.dbsolver.logic.minCover
import com.pryanik.dbsolver.logic.minKeys

fun buildDcmpWithFDsForEachRel(
    fds: FuncDeps,
    dcmp: Decomposition,
    decompose: Decomposition.(FuncDeps, Relation) -> Unit
) = Decomposition().apply {
    dcmp.forEach {
        decompose(fds.extractFor(it), it)
    }
}

fun to2NF(fds: FuncDeps, originalDcmp: Decomposition = fds.toUniversalDcmp()) =
    buildDcmpWithFDsForEachRel(fds, originalDcmp) { specificFD, _ ->
        val addedAtr = mutableSetOf<String>()
        val key = minKeys(specificFD, false).first()

        for (k in key.toList().powerset()) {
            val closure = closure(k, specificFD)
            if (closure.minus(k).isNotEmpty()) {
                // Создание нового R
                val newRel = Relation()
                k.forEach {
                    newRel.add(it)
                    if (!addedAtr.contains(it))
                        addedAtr.add(it)
                }
                closure.forEach {
                    if (!addedAtr.contains(it)) {
                        newRel.add(it)
                        addedAtr.add(it)
                    }
                }
                add(newRel)
            }
            if (addedAtr == specificFD.allAttr)
                break
        }
    }

fun to3NF(fds: FuncDeps, originalDcmp: Decomposition = to2NF(fds)): Decomposition {
    //TODO Реализовать без minCover
    return buildSet {
        minCover(fds).forEach { (det, dep) ->
            add(det.union(dep).toRelation())
        }
    }.toDecomposition()
}

// todo НФБК не должна требовать 3НФ (по определению)
fun toBCNF(fds: FuncDeps, originalDcmp: Decomposition = to3NF(fds)) =
    buildDcmpWithFDsForEachRel(fds, originalDcmp) { specificFD, rel ->
        val keys = minKeys(specificFD)
        val commonAtr = keys.reduce { acc, key -> acc intersect key }
        // Если >= 2 потенциальных ключа и они составные и имеют общие атрибуты
        if (keys.size > 1 && keys.any { it.size > 1 } && commonAtr.isNotEmpty()) {
            val allKeyAtr = keys.flatten().toRelation()
            // Декомпозируем отношение
            if (rel.containsAll(allKeyAtr)) {
                val r1 = allKeyAtr - commonAtr.toRelation()
                add(r1)
                // todo Сделать не только для двух ПК
                add(rel - r1 + setOf(r1.first().lit).toRelation())
            }
        } else add(rel)
    }
