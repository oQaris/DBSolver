package com.example.dbsolver.logic

import com.example.dbsolver.Log

const val arrow = "<sup>→</sup>"
const val impl = "<sup>⇒</sup>"
const val clPrefix = "<sup><small>+</small></sup>"
const val clPrefixIt = "$clPrefix<sub><small>s-*</small></sub>"
const val clPrefixH = "$clPrefix<sub><small>s-н</small></sub>"

fun String.sup() = "<sup><small>$this</small></sup>"
fun String.inf() = "<sub><small>$this</small></sub>"

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

fun toStr(set: Set<String>, anyInBrackets: Boolean = false): String {
    if (set.size == 1 && !anyInBrackets)
        return set.first()
    return "{${set.joinToString(",")}}"
}

fun hasInput(rel: Relations, isLog: Boolean = true) {
    Log.setLogging(isLog)
    Log.ln("Вы ввели:", "b")
    Log.ln(rel.toString("<br/>"))
    Log.ln()
    Log.restoreLogging()
}

fun printMtx(matrix: Array<Array<String>>, title: List<String>, column: List<Set<String>>) {
    Log.l("<table border=\"1\" width=\"100%\" cellpadding=\"5\">")
    Log.l("<tr><th></th>")
    title.forEach {
        Log.l(it, "th")
    }
    Log.l("</tr>")
    matrix.forEachIndexed { i, arr ->
        Log.l("<tr>")
        Log.l(toStr(column[i]), "td")
        arr.forEach { Log.l(it, "td") }
        Log.l("</tr>")
    }
    Log.l("</table>")
}