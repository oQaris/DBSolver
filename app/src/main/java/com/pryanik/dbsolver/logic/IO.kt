package com.pryanik.dbsolver.logic

import android.os.Build

val delimiter = "[\\p{P}|\\s]+".toRegex()

fun parseRelations(src: List<Pair<String, String>>): Relations {
    val out = Relations()
    src.forEach { line ->
        if (line.first == "" || line.second == "")
            throw IllegalArgumentException("В функциональной зависимости должно быть только 2 части!")

        val det = line.first.split(delimiter).toMutableSet()
        det.remove("")
        det.forEach { out.allAttr.add(it) }
        //todo исправить
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            out.putIfAbsent(det, mutableSetOf())
        else
            if (out[det] == null)
                out[det] = mutableSetOf()

        val dep = line.second.split(delimiter).toMutableSet()
        dep.remove("")
        dep.forEach {
            out[det]?.add(it)
            out.allAttr.add(it)
        }
    }
    return out
}

fun parseDecomposition(src: List<String>, rel: Relations): Set<Set<String>> {
    val out = mutableSetOf<Set<String>>()
    src.forEach { line ->
        val set = line.split(delimiter).toMutableSet()
        set.remove("")
        out.add(set)
        if (!rel.allAttr.containsAll(set))
            throw IllegalArgumentException("В декомпозиции должны находиться только атрибуты из заданного отношения!")
    }
    return out
}