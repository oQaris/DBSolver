package com.example.dbsolver.logic

import android.os.Build
import java.io.File

val delimiter = "[\\p{P}|\\s]+".toRegex()

fun readRelations(src: List<Pair<String, String>>): Relations {
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

fun readDecomposition(file: File = File("DCMP.txt"), rel: Relations): Set<Set<String>> {
    val out = mutableSetOf<Set<String>>()
    file.forEachLine { line ->
        val set = line.split(delimiter).toMutableSet()
        set.remove("")
        out.add(set)
        if (!rel.allAttr.containsAll(set))
            throw IllegalArgumentException("В декомпозиции должны находиться только атрибуты из заданного отношения!")
    }
    return out
}