package com.pryanik.dbsolver.logic.algorithms

import com.github.shiguruikai.combinatoricskt.Combinatorics
import com.github.shiguruikai.combinatoricskt.powerset
import com.pryanik.dbsolver.logic.FuncDeps
import com.pryanik.dbsolver.logic.closure
import com.pryanik.dbsolver.logic.minCover
import com.pryanik.dbsolver.logic.minKeys

typealias DCMP = Set<Set<String>>

fun to2NF(rel: FuncDeps): Decomposition {
    val dcmp = Decomposition()
    val addedAtr = mutableSetOf<String>()
    val key = minKeys(rel, false).first()

    for (i in 1..key.size) {
        // Перебираем части ключа
        Combinatorics.combinations(
            key.toList(), i
        ).forEach { k ->
            val clsAtr = closure(k, rel) - k.toSet()
            if (clsAtr.isNotEmpty()) {
                // Создание нового R
                val newRel = Relation()
                k.forEach {
                    // Ключи добавляем все, т.к. они могут повторяться (FK)
                    if (addedAtr.contains(it))
                        newRel.addForeignKey(it)
                    else {
                        newRel.addPrimaryKey(it)
                        addedAtr.add(it)
                    }
                }
                dcmp.add(newRel)
                clsAtr.forEach {
                    // А атрибуты проверяем на уникальность
                    if (!addedAtr.contains(it)) {
                        dcmp.last().addForeignKey(it)
                        addedAtr.add(it)
                    }
                }
            }
        }
        if (addedAtr == rel.allAttr)
            break
    }
    return dcmp
}

fun to3NF(rel: FuncDeps): Decomposition {
    val dcmp = to2NF(rel)
    dcmp.copy().forEach { curRel ->
        val pks = curRel.getPrimaryKeys()

        val dirDepAttrs = pks.powerset().fold(setOf<Attr>()) { acc, keySet ->
            // Перебираем все комбинации ключей и получаем нетранзитивные атрибуты (Set<Attr>)
            val attrsStr = rel[keySet.map { it.lit }.toSet()]
            val attrs = rel[attrsStr]?.toRelation() ?: setOf()
            acc.union(attrs)
        }
        val newRel = pks.toSet() + dirDepAttrs
        if (newRel != curRel) {
            // Если в отношении есть лишните атрибуты, то заменяем его новым
            dcmp.remove(curRel)
            dcmp.add(newRel)
            curRel.minus(newRel).forEach { transAttr ->
                // Определяем ключ для каждого транзитивного атрибута
                //TODO Реализовать без minCover
            }
        }
    }
    //return dcmp

    //TODO Убрать, временная мера
    return buildSet {
        minCover(rel).forEach { (det, dep) ->
            add(det.union(dep).toRelation())
        }
    }.toDecomposition()
}

fun toBCNF(rel: FuncDeps): Decomposition {
    val dcmp = to3NF(rel)
    val keys = minKeys(rel)
    //todo искать ключи для конкретного отношения
    val commonAtr = keys.reduce { acc, key -> acc intersect key }
    // Если >= 2 потенциальных ключа и они составные и имеют общие атрибуты
    if (keys.size > 1 && keys.any { it.size > 1 } && commonAtr.isNotEmpty()) {
        val allKeyAtr = keys.flatten().toRelation()
        // Декомпозируем отношения из 3НФ у которых ключевые атрибуты
        dcmp.filter { it.containsAll(allKeyAtr) }.forEach { relation ->
            dcmp.remove(relation)
            val r1 = allKeyAtr - commonAtr.toRelation()
            dcmp.add(r1)
            dcmp.add(relation - r1 + Relation(mutableSetOf(r1.first())))
        }
    }
    return dcmp
}