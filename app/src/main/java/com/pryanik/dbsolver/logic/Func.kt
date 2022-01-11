package com.pryanik.dbsolver.logic

import com.pryanik.dbsolver.Log
import com.pryanik.dbsolver.logic.algorithms.Relation

// Для андроида
const val br = "<br/>"
const val tagI = "i"
const val tagB = "b"
const val tagU = "u"
const val arrow = '→'
const val impl = "⇒"
const val clPrefix = "<sup><small>+</small></sup>"
const val clPrefixIt = "$clPrefix<sub><small>s-*</small></sub>"
const val clPrefixH = "$clPrefix<sub><small>s-н</small></sub>"

// Для консоли
/*const val br = "\n"
const val tagI = "i"
const val tagB = "b"
const val tagU = "u"
const val arrow = "->"
const val impl = "=>"
const val clPrefix = "+"
const val clPrefixIt = "${clPrefix}S-*"
const val clPrefixH = "${clPrefix}S-H"*/

fun String.sup() = "<sup><small>$this</small></sup>"
fun String.inf() = "<sub><small>$this</small></sub>"

fun closure(attributeSet: Collection<String>, funcDeps: FuncDeps): Set<String> {
    val closure = attributeSet.toMutableSet()
    var isChanged = true
    while (isChanged) {
        isChanged = false
        for ((det, dep) in funcDeps)
            if (closure.containsAll(det))
                isChanged = if (!isChanged) closure.addAll(dep) else true
    }
    return closure
}

fun toStr(set: Collection<String>, anyInBrackets: Boolean = false): String {
    if (set.size == 1 && !anyInBrackets)
        return set.first()
    return "{${set.joinToString(",")}}"
}

fun toStr(set: Relation) = "(${set.joinToString(",") { it.lit + it.type.str }})"

fun hasInput(rel: FuncDeps, isLog: Boolean = true) {
    Log.setLogging(isLog)
    Log.ln("Вы ввели:", "b")
    Log.ln(rel.toStr("<br/>"))
    Log.ln()
    Log.restoreLogging()
}

fun showMatrixAsHTML(matrix: Array<Array<String>>, title: List<String>, column: List<Relation>) {
    Log.l("<table border=\"1\" width=\"100%\" cellpadding=\"5\">")
    Log.l("<tr><th></th>")
    title.forEach {
        Log.l(it, "th")
    }
    Log.l("</tr>")
    matrix.forEachIndexed { i, arr ->
        Log.l("<tr>")
        Log.l(toStr(column[i].toSetLit()), "td")
        arr.forEach { Log.l(it, "td") }
        Log.l("</tr>")
    }
    Log.l("</table>")
}