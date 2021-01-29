package com.example.dbsolver.logic

import com.example.dbsolver.Log

//todo Избавиться от .toMutableSet()

fun minCover(rel: Relations, isLog: Boolean = false): Relations {
    var out = rel.copy()
    Log.ln("Вычисление минимального покрытия:", isLog)
    Log.ln("1)", isLog)
    for ((det, v) in out.singlePairs) {
        val newMap = out.copy()
        newMap.remove(det f v)
        val cl = closure(det, newMap)
        if (cl.containsAll(v)) {
            Log.ln("${det f v}:\t${toStr(det)}+S-* = ${toStr(cl)} э $v => S=S-{${det f v}}", isLog)
            out = newMap
        } else
            Log.ln("${det f v}:\t${toStr(det)}+S-* = ${toStr(cl)} не э $v => S не меняется", isLog)
    }
    val multiDet = out.filter { (k, _) -> k.size > 1 }
    Log.ln("2)", isLog)
    if (multiDet.isEmpty())
        Log.ln("S не содержит ФЗ с детерминантом из нескольких атрибутов", isLog)
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
                Log.ln(
                    "${set f v}:\t${toStr(set)}+ = ${toStr(cl)} э ${toStr(v)} => $it удаляем из дет. ${k f v}",
                    isLog
                )
            } else
                Log.ln(
                    "${set f v}:\t${toStr(set)}+ = ${toStr(cl)} не э ${toStr(v)} => $it оставляем в дет. ${k f v}",
                    isLog
                )
        }
    }
    Log.ln("Минимальное Покрытие:", isLog)
    Log.ln(out.toString("\n"), isLog)
    Log.ln(isLog)
    return out
}

fun allClosure(rel: Relations, isLog: Boolean = false): Relations {
    Log.ln("Замыкания всех наборов атрибутов:", isLog)
    val out = Relations()
    for (i in 1..rel.allAttr.size)
        combinations(
            rel.allAttr.toList(), i
        ).forEach { det ->
            val closure = closure(det, rel)
            out[det.toMutableSet()] = closure.toMutableSet()
            Log.ln("${toStr(det)}+ = ${toStr(closure)}", isLog)
        }
    Log.ln(isLog)
    return out
}

fun minKeys(rel: Relations, isLog: Boolean = false): Set<Set<String>> {
    Log.ln("Минимальные Ключи:", isLog)
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
    keys.forEach { Log.ln(toStr(it), isLog) }
    Log.ln(isLog)
    return keys
}

fun nonTrivialFDs(rel: Relations, isLog: Boolean = false): Relations {
    Log.ln("Нетривиальные ФЗ с зависимой частью из 1 атрибута:", isLog)
    val out = Relations()
    for ((det, cl) in allClosure(rel)) {
        cl.minus(det).forEach {
            out[det.toMutableSet()] = mutableSetOf(it)
            Log.ln("${det f it}", isLog)
        }
    }
    Log.ln(isLog)
    return out
}

fun decomposition(rel: Relations, isLog: Boolean = false): Set<Set<String>> {
    Log.ln("Декомпозиция до БКНФ:\nВ разработке!", isLog)
    Log.ln(isLog)
    return setOf()
}

/*
fun isLosslessConnection(rel: Relations, dcmp: Set<Set<String>>): Boolean {
    println("Проверка декомпозиции на свойство соединения без потерь:")
    val attrIdx = allAtrSet.toSortedSet().toList()
    val dcmpIdx = dcmp.toList()
    val matrix = Array(dcmpIdx.size) { Array(attrIdx.size) { "" } }
    for (i in matrix.indices)
        for (j in matrix[i].indices)
            if (dcmpIdx[i].contains(attrIdx[j]))
                matrix[i][j] = "a"
            else matrix[i][j] = "${i + 1}"

    println("Исходная таблица:")
    printMtx(matrix, attrIdx, dcmpIdx)
    var isLossConnectProperty = true
    for ((k, v) in map.entries) {
        println("${mapToString(mutableMapOf(k to v))}:")
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
                println("Строка ${i + 1} полностью состоит из 'a' => декомпозиция обладает свойством соединения без потерь!")
                break
            }
        }
        if (isLossConnectProperty)
            break
    }
    if (!isLossConnectProperty)
        println("Т.к. были перебраны все ФЗ, а строка, полностью состоящая из A так и не появилась, то св-во соединения без потерь не выполняется!")
    println()
}

fun isFuncDepPersistence(rel: Relations, dcmp: Set<Set<String>>): Boolean {
    println("Проверка декомпозиции на свойство сохранения ФЗ:")
    println("1.")
    val crnc = Relations()
    for ((t, u) in map) {
        val clm = closure(t.toMutableSet(), map).minus(t)
        print("${toStr(t)}+S-* = ${toStr(clm.toMutableSet())} => ")
        if (u == clm)
            println("G не меняется")
        else println("Заменяем ${mapToString(mutableMapOf(t to u))} на ${mapToString(mutableMapOf(t to clm.toMutableSet()))}")
        crnc[t.toMutableSet()] = clm.toMutableSet()
    }
    println("УНП = {${mapToString(crnc)}}, H = {}")
    println("2.")
    print("G = ")
    val listOut = mutableListOf<String>()
    for ((t, attr) in crnc.singlePairs) {
        listOut.add(mapToString(mutableMapOf(t.toMutableSet() to attr)))
    }
    print("{${listOut.joinToString("; ")}}")
    println()
    println("3.")
    val h = Relations()
    for ((t, attr) in crnc.singlePairs) {
        print("${mapToString(mutableMapOf(t.toMutableSet() to attr))}:\t")
        var flag = false
        for (i in dcmpIdx.indices) {
            val un = t.union(mutableSetOf(attr))
            print("${toStr(un.toMutableSet())} ")
            if (dcmpIdx[i].containsAll(un)) {
                print("⊆ R${i + 1} ")
                flag = true
                break
            } else print("не ⊆ R${i + 1}, ")
        }
        if (!flag) {
            h.getOrPut(t.toMutableSet()) { mutableSetOf() }.add(attr)
            print("=>\tH += {${mapToString(mutableMapOf(t.toMutableSet() to attr))}}")
        }
        println()
    }
    println("4.")
    print("Множество H ")
    if (h.isEmpty())
        println("пустое => сво-во сохранения ФЗ выполняется")
    else {
        println("не пустое => переход к шагу 5")
        println("5.")
        val g = map.copy()
        for ((t, u) in h) {
            g[t] = g[t]?.minus(u)?.toMutableSet()!!
            if (g[t]?.size == 0)
                g.remove(t)
        }
        var flag = true
        for ((t, u) in h.entries) {
            print("${toStr(t)}+G-H = ")
            val cl = closure(t, g)
            print(toStr(cl.toMutableSet()))
            if (!cl.containsAll(u)) {
                flag = false
                print(" не ⊇ ${toStr(u)}")
                break
            }
            println(" ⊇ ${toStr(u)}")
        }
        if (flag)
            println("=> Декомпозиция обладает св-вом сохранения ФЗ!")
        else println(" => Декомпозиция НЕ обладает св-вом сохранения ФЗ")
        println()
    }
}*/
