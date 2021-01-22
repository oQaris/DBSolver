import com.google.common.collect.HashMultiset
import java.io.File

/*data class Relation(val attrs: MutableSet<Char> = mutableSetOf()) {
    private val _PK = mutableSetOf<Char>()
    private val _FK = mutableSetOf<Char>()

    fun addPK(atr: Char) = _PK.add(atr)
    fun addFK(atr: Char) = _FK.add(atr)

    fun print(idx: Int, key: Set<Char>) {
        var out = "R$idx("
        attrs.forEach {
            out += "$it"
            if (_PK.contains(it))
                out += "(PK)"
            if (_FK.contains(it))
                out += "(FK)"
            out += ", "
        }
        println(out.substring(0..out.length - 3) + ")")
    }
}

data class Decomposition(val relations: MutableSet<Relation> = mutableSetOf()) {
    *//*fun add(rel: Relation) = relations.add(rel)
    fun get(rel: Relation) = relations.add(rel)*//*

    fun print(key: Set<Char>) {
        relations.forEachIndexed { i, rel ->
            rel.print(i, key)
        }
    }
}*/

fun combinations(
    arr: List<Char>,
    len: Int,
    startPosition: Int = 0,
    result: Array<Char> = Array(len) { ' ' },
    otv: MutableList<Set<Char>> = mutableListOf()
): MutableList<Set<Char>> {
    if (len == 0) {
        val set = mutableSetOf<Char>()
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

fun closure(attributeSet: Set<Char>, funcDeps: MutableMap<MutableSet<Char>, MutableSet<Char>>): Set<Char> {
    val closure = setOf<Char>().union(attributeSet).toMutableSet()
    var j = 1
    while (j <= closure.size) {
        var comb = combinations(closure.toList(), j)
        var g = 0
        while (g < comb.size) {
            if (funcDeps[comb[g]] != null && closure.addAll(funcDeps[comb[g]]!!)) {
                comb = combinations(closure.toList(), j)
                j = 0
                g = -1
            }
            g++
        }
        j++
    }
    return closure
}

fun main(args: Array<String>) {
    var map = mutableMapOf<MutableSet<Char>, MutableSet<Char>>()
    val allAtrSet = mutableSetOf<Char>()

    val file = File("FZ.txt")
    file.forEachLine { line ->
        val fz = line.split("->")
        assert(fz.size == 2)
        //map.getOrPut(fz[0].trim()) { mutableSetOf() }.add(fz[1].trim())
        val det = fz[0].trim().toSet()
        det.forEach { allAtrSet.add(it) }

        map.putIfAbsent(det.toMutableSet(), mutableSetOf())
        fz[1].trim().forEach {
            allAtrSet.add(it)
            map[det]?.add(it)
        }
    }

    println("Исходные ФЗ:")
    map.forEach { (t, u) ->
        println("$t -> $u")
    }
    println()

    println("Минимальное Покрытие:")
    println("1)")
    map.forEach { (k, v) ->
        v.forEach {
            val newMap = mutableMapOf<MutableSet<Char>, MutableSet<Char>>()
            map.forEach { (t, u) ->
                newMap[t.toMutableSet()] = u.toMutableSet()
            }
            newMap[k]?.remove(it)
            if (newMap[k]?.size == 0)
                newMap.remove(k)
            val cl = closure(k, newMap)
            if (cl.contains(it)) {
                println("$k->$it:  $k+-* = $cl есть $it => S=S-{$k->$it}")
                map = newMap
            } else
                println("$k->$it:  $k+-* = $cl нет $it => S не меняется")
        }
    }
    println("2)")
    map.forEach { (k, v) ->
        if (k.size > 1)
            k.forEach {
                val set = k.minus(setOf(it))
                val cl = closure(set, map)
                if (cl.containsAll(v)) {
                    val newMap = mutableMapOf<MutableSet<Char>, MutableSet<Char>>()
                    map.forEach { (t, u) ->
                        if (t == k)
                            newMap.getOrPut(set.toMutableSet()) { u.toMutableSet() }
                                .addAll(u.toMutableSet())
                        else newMap[t.toMutableSet()] = u.toMutableSet()
                    }
                    map = newMap
                    println("$set->$v:  $set+ = $cl есть $v => $it удаляем из дет. $k->$v")
                } else
                    println("$set->$v:  $set+ = $cl нет $v => $it оставляем в дет. $k->$v")
            }
    }
    println("Мин. Покрытие:")
    map.forEach { (t, u) ->
        u.forEach {
            println("$t -> $it")
        }
    }
    println()

    println("Замыкания всех наборов атрибутов:")
    val keys = mutableListOf<Set<Char>>()
    val fz = mutableMapOf<Set<Char>, Set<Char>>()
    var minLenKey = Int.MAX_VALUE
    //val closures = mutableMapOf<Set<Char>, MutableSet<Char>>()
    for (i in 1..allAtrSet.size)
        combinations(
            allAtrSet.toList(), i
        ).forEach { det ->
            val closure = closure(det, map)
            if (closure == allAtrSet && minLenKey >= det.size) {
                keys.add(det)
                minLenKey = det.size
            }
            if (closure.minus(det).isNotEmpty())
                fz[det] = closure.minus(det)
            println("$det+ = $closure")
        }
    println()

    println("Минимальные Ключи:")
    keys.forEach {
        println(it)
    }
    println()

    println("Нетривиальные ФЗ с 1 атрибутом в зависимой части:")
    fz.forEach { itFz ->
        itFz.value.forEach {
            println("${itFz.key} -> $it")
        }
    }
    println()

    println("Декомпозиция до БКНФ:\nВ разработке!\n")
    /*val decomp = Decomposition()
    println("1НФ  (Все атрибуты имеют атомарное значение):")
    val univRel = Relation(allAtrSet)
    decomp.relations.add(univRel)
    var out = "R("
    univRel.attrs.forEach {
        out += "$it"
        if (keys[0].contains(it))
            out += "(PK)"
        out += ", "
    }
    println(out.substring(0..out.length - 3) + ")")

    println("\n2НФ  (Каждый неключевой атрибут функц. полно зависят от ключа):")
    //val sheme = mutableSetOf<MutableSet<Char>>()
    val addedAtr = mutableSetOf<Char>()
    for (i in 1..keys[0].size) {
        combinations(
            keys[0].toList(), i
        ).forEach { key ->
            val closure = closure(key, map)
            if (closure.isNotEmpty()) {
                // Создание нового R
                sheme.add(mutableSetOf())
                key.forEach {
                    sheme.last().add(it)
                    if (!addedAtr.contains(it))
                        addedAtr.add(it)
                }
                closure.forEach {
                    if (!addedAtr.contains(it)) {
                        sheme.last().add(it)
                        addedAtr.add(it)
                    }
                }
            }
        }
        if (addedAtr == allAtrSet)
            break
    }
    printDecomp(sheme, keys[0])

    println("\n3НФ  (Каждый атирбут нетранзитивно зависит от ключа):")
    val newSheme = mutableSetOf<MutableSet<Char>>()
    sheme.forEach {
        newSheme.add(it.toMutableSet())
    }
    newSheme.forEach { R ->
        R.minus()
    }
    /*sheme.forEach { set ->
        val curPK = mutableSetOf<Char>()
        set.forEach {
            if (keys[0].contains(it))
                curPK.add(it)
        }
        set.forEach {
            if (!curPK.contains(it)) {
                val newDec = mutableSetOf<Char>()
                map.forEach { (t, u) ->
                    if (u.contains(it) && curPK != t) {
                        newDec.addAll(t)
                        newDec.addAll(u)
                        //set = set.minus(t).minus(u).toMutableSet()
                    }
                }
                if (newDec.isNotEmpty())
                    newSheme.add(newDec)
            }
        }
    }*//*
    //printDecomp(newSheme, keys[0])
    newSheme.forEach {
        println(it)
    }
    //printDecomp(newSheme, keys[0])
    println()*/*/


    println("Проверка декомпозиции на свойство соединения без потерь:")
    val decomp = mutableSetOf<MutableSet<Char>>()
    val file2 = File("DCMP.txt")
    file2.forEachLine { line ->
        val fz = line.split(" ")
        //map.getOrPut(fz[0].trim()) { mutableSetOf() }.add(fz[1].trim())
        val set = mutableSetOf<Char>()
        fz.forEach {
            assert(it.toCharArray().size == 1)
            set.add(it.toCharArray()[0])
            allAtrSet.add(it.toCharArray()[0])
        }
        decomp.add(set)
    }
    val attrIdx = allAtrSet.toSortedSet().toList()
    val dcmpIdx = decomp.toList()
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
        println("$k -> $v:")
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
    if(!isLossConnectProperty)
        println("Т.к. были перебраны все ФЗ, а строка, полностью состоящая из A так и не появилась, то св-во соединения без потерь не выполняется!")
}

fun printMtx(matrix: Array<Array<String>>, title: List<Char>, column: List<Set<Char>>) {
    var maxLen = 0
    column.forEach {
        if (it.toString().length > maxLen)
            maxLen = it.toString().length
    }
    maxLen++
    print(" ".padStart(maxLen))
    title.forEach {
        print("$it  ")
    }
    println()
    matrix.forEachIndexed { i, arr ->
        print("${column[i]}".padEnd(maxLen))
        arr.forEach {
            if (it.length == 2)
                print("$it ")
            else print("$it  ")
        }
        println()
    }
    println()
}