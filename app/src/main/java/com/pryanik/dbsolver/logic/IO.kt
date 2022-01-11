package com.pryanik.dbsolver.logic

import com.pryanik.dbsolver.logic.algorithms.Decomposition
import com.pryanik.dbsolver.logic.algorithms.toRelation

val delimiter = "[\\p{P}|\\s]+".toRegex()

fun parseRelations(src: List<Pair<String, String>>): FuncDeps {
    val out = FuncDeps()
    src.forEach { line ->
        require(line.first != "" && line.second != "")
        { "В функциональной зависимости должно быть только 2 части!" }

        val det = line.first.extractChars()
        if (out[det] == null)
            out[det] = mutableSetOf()

        val dep = line.second.extractChars()
        dep.forEach {
            out[det]?.add(it)
        }
    }
    return out
}

const val arrowSaves = '→'
fun parsePairs(src: String): List<Pair<String, String>> {
    val items = src.split("$arrowSaves|\\n".toRegex())
    require(items.size % 2 == 0) { "Не должно быть пустых пар!" }
    return items.zipWithNext { a, b ->
        a.extractChars().joinToString(" ") to
                b.extractChars().joinToString(" ")
    }.filterIndexed { index, _ -> index % 2 == 0 }
}

fun parseDecomposition(src: List<String>, rel: FuncDeps): Decomposition {
    val dcmp = Decomposition()
    src.forEach { line ->
        val set = line.split(delimiter).toMutableSet()
        set.remove("")
        dcmp.add(set.toRelation())
        if (!rel.allAttr.containsAll(set))
            throw IllegalArgumentException("В декомпозиции должны находиться только атрибуты из заданного отношения!")
    }
    return dcmp
}

fun parseDcmpStr(src: String): List<String> {
    return src.split("\n").map { it.extractChars().joinToString(" ") }
}

fun String.extractChars(): MutableSet<String> {
    val set = this.split(delimiter).toMutableSet()
    set.remove("")
    return set
}