package com.example.dbsolver.logic

import com.example.dbsolver.Log

fun combinations(
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
        combinations(arr, len - 1, i + 1, result, otv)
    }
    return otv
}

fun closure(attributeSet: Set<String>, funcDeps: Relations): Set<String> {
    val closure = attributeSet.toMutableSet()
    var isChanged = true
    while (isChanged) {
        isChanged = false
        for ((det, dep) in funcDeps)
            if (closure.containsAll(det))
                isChanged = closure.addAll(dep)
    }
    return closure
}

fun toStr(set: Set<String>): String {
    if (set.size == 1)
        return set.first()
    return "{${set.joinToString(",")}}"
}

fun hasInput(rel: Relations, isLog: Boolean = true) {
    Log.turnOn = isLog
    Log.ln("Вы ввели:")
    Log.ln(rel.toString("\n"))
    Log.ln()
}

fun printMtx(matrix: Array<Array<String>>, title: List<String>, column: List<Set<String>>) {
    var maxLen = 0
    column.forEach {
        if (it.toString().length > maxLen)
            maxLen = it.size * 2 + 2
    }
    maxLen++
    Log.l(" ".padStart(maxLen))
    title.forEach {
        Log.l("$it  ")
    }
    Log.ln()
    matrix.forEachIndexed { i, arr ->
        Log.l(toStr(column[i]).padEnd(maxLen))
        arr.forEachIndexed { j, it ->
            Log.l(it.padEnd(title[j].length + 2))
        }
        Log.ln()
    }
    Log.ln()
}