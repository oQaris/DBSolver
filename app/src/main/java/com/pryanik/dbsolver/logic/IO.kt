package com.pryanik.dbsolver.logic

val delimiter = "[\\p{P}|\\s]+".toRegex()

fun parseRelations(src: List<Pair<String, String>>): Relations {
    val out = Relations()
    src.forEach { line ->
        if (line.first == "" || line.second == "")
            throw IllegalArgumentException("В функциональной зависимости должно быть только 2 части!")

        val det = line.first.extractChars()
        det.forEach { out.allAttr.add(it) }
        if (out[det] == null)
            out[det] = mutableSetOf()

        val dep = line.second.extractChars()
        dep.forEach {
            out[det]?.add(it)
            out.allAttr.add(it)
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

fun parseDcmpStr(src: String): List<String> {
    return src.split("\n").map { it.extractChars().joinToString(" ") }
}

fun String.extractChars(): MutableSet<String> {
    val set = this.split(delimiter).toMutableSet()
    set.remove("")
    return set
}