package com.example.dbsolver.logic

import com.example.dbsolver.Log
import com.google.common.collect.HashMultiset

//todo Избавиться от .toMutableSet()

fun minCover(rel: Relations, isLog: Boolean = false): Relations {
    Log.setLogging(isLog)
    var out = rel.copy()
    Log.ln("Вычисление минимального покрытия:", "b")
    Log.ln("1)")
    for ((det, v) in out.singlePairs) {
        val newMap = out.copy()
        newMap.remove(det f v)
        val cl = closure(det, newMap)
        if (cl.containsAll(v)) {
            Log.ln("${det f v}:\t${toStr(det)}$clPrefixIt = ${toStr(cl)} э ${toStr(v)} $impl S=S-{${det f v}}")
            out = newMap
        } else
            Log.ln("${det f v}:\t${toStr(det)}$clPrefixIt = ${toStr(cl)} не э ${toStr(v)} $impl S не меняется")
    }
    val multiDet = out.filter { (k, _) -> k.size > 1 }
    Log.ln("2)")
    if (multiDet.isEmpty())
        Log.ln("S не содержит ФЗ с детерминантом из нескольких атрибутов", "i")
    else for ((k, v) in multiDet) {
        k.forEach {
            val set = k.minus(setOf(it))
            val cl = closure(set, out)
            if (cl.containsAll(v)) {
                val newMap = Relations()
                for ((t, u) in out) {
                    if (t == k)
                        newMap.getOrPut(set.toMutableSet()) { u }.addAll(u)
                    else newMap[t] = u
                }
                out = newMap
                Log.ln("${set f v}:\t${toStr(set)}$clPrefix = ${toStr(cl)} э ${toStr(v)} $impl $it удаляем из дет. ${k f v}")
            } else
                Log.ln("${set f v}:\t${toStr(set)}$clPrefix = ${toStr(cl)} не э ${toStr(v)} $impl $it оставляем в дет. ${k f v}")
        }
    }
    Log.ln("Минимальное Покрытие:", "i")
    Log.ln(out.toString("<br/>"))
    Log.ln()
    Log.restoreLogging()
    return out
}

fun allClosure(rel: Relations, isLog: Boolean = false): Relations {
    Log.setLogging(isLog)
    Log.ln("Замыкания всех наборов атрибутов:", "b")
    val out = Relations()
    for (i in 1..rel.allAttr.size)
        combinations(
            rel.allAttr.toList(), i
        ).forEach { det ->
            val closure = closure(det, rel)
            out[det.toMutableSet()] = closure.toMutableSet()
            Log.ln("${toStr(det)}$clPrefix = ${toStr(closure)}")
        }
    Log.ln()
    Log.restoreLogging()
    return out
}

fun minKeys(rel: Relations, isLog: Boolean = false): Set<Set<String>> {
    Log.setLogging(isLog)
    Log.ln("Минимальные Ключи:", "b")
    val keys = mutableSetOf<Set<String>>()
    var minLenKey = Int.MAX_VALUE
    for ((det, cl) in allClosure(rel)) {
        if (cl == rel.allAttr && minLenKey >= det.size) {
            if (minLenKey > det.size)
                keys.clear()
            keys.add(det)
            minLenKey = det.size
        }
    }
    for (k in keys) {
        Log.ln(toStr(k))
    }
    //keys.forEach { Log.ln(toStr(it)) }
    Log.ln()
    Log.restoreLogging()
    return keys
}

fun nonTrivialFDs(rel: Relations, isLog: Boolean = false): Relations {
    Log.setLogging(isLog)
    Log.ln("Нетривиальные ФЗ с зависимой частью из 1 атрибута:", "b")
    val out = Relations()
    for ((det, cl) in allClosure(rel)) {
        cl.minus(det).forEach {
            out[det.toMutableSet()] = mutableSetOf(it)
            Log.ln("${det f it}")
        }
    }
    Log.ln()
    Log.restoreLogging()
    return out
}

fun decomposition(rel: Relations, isLog: Boolean = false): List<Set<String>> {
    Log.setLogging(isLog)
    Log.ln("Декомпозиция до БКНФ:", "b")
    Log.ln("• 1НФ\t(Все атрибуты имеют атомарное значение):", "i")
    Log.ln("R${toStr(rel.allAttr)}")
    Log.ln("• 2НФ\t(Каждый неключевой атрибут функц. полно зависит от ключа):", "i")
    val decomp = mutableListOf<MutableSet<String>>()
    val addedAtr = mutableSetOf<String>()
    val key = minKeys(rel, false).first()
    for (i in 1..key.size) {
        combinations(
            key.toList(), i
        ).forEach { k ->
            val closure = closure(k, rel)
            if (closure.isNotEmpty()) {
                // Создание нового R
                decomp.add(mutableSetOf())
                k.forEach {
                    decomp.last().add(it)
                    if (!addedAtr.contains(it))
                        addedAtr.add(it)
                }
                closure.forEach {
                    if (!addedAtr.contains(it)) {
                        decomp.last().add(it)
                        addedAtr.add(it)
                    }
                }
            }
        }
        if (addedAtr == rel.allAttr)
            break
    }
    decomp.forEachIndexed { idx, set ->
        Log.ln("R${idx + 1}${toStr(set)}")
    }
    Log.ln("• 3НФ\t(Каждый атирбут нетранзитивно зависит от ключа):", "i")
    decomp.clear()
    val minCov = minCover(rel)
    for ((k, v) in minCov)
        decomp.add(k.union(v).toMutableSet())
    decomp.forEachIndexed { idx, set ->
        Log.ln("R${idx + 1}${toStr(set)}")
    }
    Log.ln("• НФБК - В разработке!", "i")
    Log.ln()
    Log.restoreLogging()
    return decomp
}

fun isLosslessConnection(rel: Relations, dcmp: Set<Set<String>>, isLog: Boolean = false): Boolean {
    Log.setLogging(isLog)
    Log.ln("Проверка декомпозиции на свойство соединения без потерь:", "b")
    val attrIdx = rel.allAttr.toSortedSet().toList()
    val dcmpIdx = dcmp.toList()
    val matrix = Array(dcmpIdx.size) { Array(attrIdx.size) { "" } }
    for (i in matrix.indices)
        for (j in matrix[i].indices)
            if (dcmpIdx[i].contains(attrIdx[j]))
                matrix[i][j] = "a"
            else matrix[i][j] = "${i + 1}"

    Log.ln("Исходная таблица:", "i")
    printMtx(matrix, attrIdx, dcmpIdx)
    var isLossConnectProperty: Boolean
    for ((k, v) in rel.entries) {
        Log.ln("${k f v}:", "i")
        // Logic
        var setIdxRow = mutableSetOf<Int>()
        for (i in dcmpIdx.indices)
            setIdxRow.add(i)
        k.forEach { attr ->
            val habr = HashMultiset.create<String>()
            val curSetIdxRow = mutableSetOf<Int>()
            val wCol = attrIdx.indexOf(attr)
            for (i in matrix.indices)
                habr.add(matrix[i][wCol])
            var l = " "
            var max = 0
            habr.forEach {
                if (habr.count(it) > max) {
                    l = it
                    max = habr.count(it)
                }
            }
            for (i in matrix.indices)
                if (matrix[i][wCol] == l)
                    curSetIdxRow.add(i)
            setIdxRow = setIdxRow.intersect(curSetIdxRow).toMutableSet()
        }
        v.forEach { attr ->
            val wCol = attrIdx.indexOf(attr)
            var flag = false
            setIdxRow.forEach { i ->
                if (matrix[i][wCol] == "a")
                    setIdxRow.forEach { j ->
                        matrix[j][wCol] = "a"
                        flag = true
                    }
            }
            if (!flag)
                setIdxRow.forEach { i ->
                    matrix[i][wCol] = matrix[setIdxRow.first()][wCol]
                }
        }
        printMtx(matrix, attrIdx, dcmpIdx)
        for (i in matrix.indices) {
            isLossConnectProperty = true
            for (j in 1 until matrix[i].size)
                if (matrix[i][j - 1] != matrix[i][j])
                    isLossConnectProperty = false
            if (isLossConnectProperty && matrix[i][0] == "a") {
                Log.ln("Строка ${i + 1} полностью состоит из 'a' $impl декомпозиция обладает свойством соединения без потерь!")
                Log.ln()
                Log.restoreLogging()
                return true
            }
        }
    }
    Log.ln("Т.к. были перебраны все ФЗ, а строка, полностью состоящая из A так и не появилась, то св-во соединения без потерь НЕ выполняется!")
    Log.ln()
    Log.restoreLogging()
    return false
}

fun isFuncDepPersistence(rel: Relations, dcmp: Set<Set<String>>, isLog: Boolean = false): Boolean {
    Log.setLogging(isLog)
    Log.ln("Проверка декомпозиции на свойство сохранения ФЗ:", "b")
    Log.ln("1.")
    val dcmpIdx = dcmp.toList()
    val crnc = Relations()
    for ((t, u) in rel) {
        val clm = closure(t, rel).minus(t)
        Log.l("${toStr(t)}$clPrefixIt = ${toStr(clm)} $impl ")
        if (u == clm)
            Log.ln("G не меняется")
        else Log.ln("Заменяем ${t f u} на ${t f clm}")
        crnc[t] = clm.toMutableSet()
    }
    Log.ln("УНП = {${crnc.toString("; ")}}, H = {}")
    Log.ln("2.")
    Log.ln("G = {${crnc.toString("; ", isSinglePairs = true)}}")
    Log.ln("3.")
    val h = Relations()
    for ((t, attr) in crnc.singlePairs) {
        Log.l("${t f attr}:\t")
        var setIdx = -1
        val un = t.union(attr)
        for (i in dcmpIdx.indices)
            if (dcmpIdx[i].containsAll(un)) {
                setIdx = i + 1
                break
            }
        Log.l(toStr(un))
        if (setIdx == -1) {
            h.getOrPut(t.toMutableSet()) { mutableSetOf() }.add(attr.first())
            Log.l(" не ⊆ R1…R${dcmpIdx.size} $impl\tH += {${t f attr}}")
        } else Log.l(" ⊆ R${setIdx} ")
        Log.ln()
    }
    Log.ln("4.")
    Log.l("Множество H ", "i")
    var isPersistence = true
    if (h.isEmpty())
        Log.ln("пустое $impl св-во сохранения ФЗ выполняется", "i")
    else {
        Log.ln("не пустое $impl переход к шагу 5", "i")
        Log.ln("5.")
        val g = rel.copy()
        for ((t, u) in h) {
            g[t] = g[t]?.minus(u)?.toMutableSet()!!
            if (g[t]?.size == 0)
                g.remove(t)
        }
        for ((t, u) in h.entries) {
            Log.l("${toStr(t)}$clPrefixH = ")
            val cl = closure(t, g)
            Log.l(toStr(cl))
            if (!cl.containsAll(u)) {
                isPersistence = false
                Log.l(" не ⊇ ${toStr(u)}")
                break
            }
            Log.ln(" ⊇ ${toStr(u)}")
        }
        if (isPersistence)
            Log.ln("$impl Декомпозиция обладает св-вом сохранения ФЗ!", "i")
        else Log.ln(" $impl Декомпозиция НЕ обладает св-вом сохранения ФЗ", "i")
    }
    Log.ln()
    Log.restoreLogging()
    return isPersistence
}