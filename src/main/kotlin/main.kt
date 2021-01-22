import com.google.common.collect.HashMultiset
import java.io.File

/*data class Relation(val attrs: MutableSet<String> = mutableSetOf()) {
    private val _PK = mutableSetOf<String>()
    private val _FK = mutableSetOf<String>()

    fun addPK(atr: String) = _PK.add(atr)
    fun addFK(atr: String) = _FK.add(atr)

    fun print(idx: Int, key: Set<String>) {
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

    fun print(key: Set<String>) {
        relations.forEachIndexed { i, rel ->
            rel.print(i, key)
        }
    }
}*/

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

fun closure(attributeSet: Set<String>, funcDeps: MutableMap<MutableSet<String>, MutableSet<String>>): Set<String> {
    val closure = setOf<String>().union(attributeSet).toMutableSet()
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

fun main() {
    var map = mutableMapOf<MutableSet<String>, MutableSet<String>>()
    val allAtrSet = mutableSetOf<String>()

    val file = File("FZ.txt")
    file.forEachLine { line ->
        val fz = line.split("->", "–>", "⇒")
        assert(fz.size == 2)
        val det = fz[0].split("\\s+".toRegex()).toMutableSet()
        det.remove("")
        det.forEach { allAtrSet.add(it) }

        map.putIfAbsent(det.toMutableSet(), mutableSetOf())
        val dep = fz[1].split("\\s+".toRegex()).toSet().toMutableSet()
        dep.remove("")
        dep.forEach {
            allAtrSet.add(it)
            map[det]?.add(it)
        }
    }

    println("Исходные ФЗ:")
    println(FDToString(map))

    println("Минимальное Покрытие:")
    println("1)")
    map.forEach { (k, v) ->
        v.forEach {
            val newMap = mutableMapOf<MutableSet<String>, MutableSet<String>>()
            map.forEach { (t, u) ->
                newMap[t.toMutableSet()] = u.toMutableSet()
            }
            newMap[k]?.remove(it)
            if (newMap[k]?.size == 0)
                newMap.remove(k)
            val cl = closure(k, newMap)
            if (cl.contains(it)) {
                println("${FDToString(mutableMapOf(k to it))}:  $k+-* = $cl есть $it => S=S-{${FDToString(mutableMapOf(k to it))}}")
                map = newMap
            } else
                println("${FDToString(mutableMapOf(k to it))}:  $k+-* = $cl нет $it => S не меняется")
        }
    }
    println("2)")
    map.forEach { (k, v) ->
        if (k.size > 1)
            k.forEach {
                val set = k.minus(setOf(it))
                val cl = closure(set, map)
                if (cl.containsAll(v)) {
                    val newMap = mutableMapOf<MutableSet<String>, MutableSet<String>>()
                    map.forEach { (t, u) ->
                        if (t == k)
                            newMap.getOrPut(set.toMutableSet()) { u.toMutableSet() }
                                .addAll(u.toMutableSet())
                        else newMap[t.toMutableSet()] = u.toMutableSet()
                    }
                    map = newMap
                    println(
                        "${FDToString(mutableMapOf(set.toMutableSet() to v))}:  $set+ = $cl есть $v => $it удаляем из дет. ${
                            FDToString(
                                mutableMapOf(k to v)
                            )
                        }"
                    )
                } else
                    println(
                        "${FDToString(mutableMapOf(set.toMutableSet() to v))}:  $set+ = $cl нет $v => $it оставляем в дет. ${
                            FDToString(
                                mutableMapOf(k to v)
                            )
                        }"
                    )
            }
    }
    println("Мин. Покрытие:")
    map.forEach { (t, u) ->
        u.forEach {
            println(FDToString(mutableMapOf(t to it)))
        }
    }
    println()

    println("Замыкания всех наборов атрибутов:")
    val keys = mutableListOf<Set<String>>()
    val fz = mutableMapOf<Set<String>, Set<String>>()
    var minLenKey = Int.MAX_VALUE
    //val closures = mutableMapOf<Set<String>, MutableSet<String>>()
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
    fz.forEach { (t, u) ->
        u.forEach {
            println(FDToString(mutableMapOf(t.toMutableSet() to it)))
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
    //val sheme = mutableSetOf<MutableSet<String>>()
    val addedAtr = mutableSetOf<String>()
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
    val newSheme = mutableSetOf<MutableSet<String>>()
    sheme.forEach {
        newSheme.add(it.toMutableSet())
    }
    newSheme.forEach { R ->
        R.minus()
    }
    /*sheme.forEach { set ->
        val curPK = mutableSetOf<String>()
        set.forEach {
            if (keys[0].contains(it))
                curPK.add(it)
        }
        set.forEach {
            if (!curPK.contains(it)) {
                val newDec = mutableSetOf<String>()
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

    println()
    println("Проверка декомпозиции на свойство соединения без потерь:")
    val decomp = mutableSetOf<MutableSet<String>>()
    val file2 = File("DCMP.txt")
    file2.forEachLine { line ->
        val set = line.split("\\s+".toRegex()).toMutableSet()
        set.remove("")
        //map.getOrPut(fz[0].trim()) { mutableSetOf() }.add(fz[1].trim())
        //val set = mutableSetOf<String>()
        /*fz.forEach {
            assert(it.toStringArray().size == 1)
            set.add(it.toStringArray()[0])
            allAtrSet.add(it.toStringArray()[0])
        }*/
        decomp.add(set.toMutableSet())
        allAtrSet.addAll(set)
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
        println("${FDToString(mutableMapOf(k to v))}:")
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
    println()

    println("Проверка декомпозиции на свойство сохранения ФЗ:")
    val crnc = mutableMapOf<MutableSet<String>, MutableSet<String>>()
    map.forEach { (t, _) ->
        crnc[t.toMutableSet()] = closure(t.toMutableSet(), map).minus(t).toMutableSet()
    }
    println("1. УНП = ${FDToString(crnc)}  H = {}")
    print("2. G = ")
    val listOut = mutableListOf<String>()
    crnc.forEach { (t, u) ->
        u.forEach { attr ->
            listOut.add(FDToString(mutableMapOf(t to attr)))
        }
    }
    print("{${listOut.joinToString("; ")}}")
    println()
    println("3.")
    val H = mutableMapOf<MutableSet<String>, MutableSet<String>>()
    crnc.forEach { (t, u) ->
        u.forEach { attr ->
            print("${FDToString(mutableMapOf(t to attr))}:\t")
            var flag = false
            for (i in dcmpIdx.indices) {
                val un = t.union(mutableSetOf(attr))
                print("$un ")
                if (dcmpIdx[i].containsAll(un)) {
                    print("⊆ R${i + 1} ")
                    flag = true
                    break
                } else print("не ⊆ R${i + 1}, ")
            }
            if (!flag) {
                H.getOrPut(t) { mutableSetOf() }.add(attr)
                print("=>\tH += {${FDToString(mutableMapOf(t to attr))}}")
            }
            println()
        }
    }
    print("4. Множество H ")
    if (H.isEmpty())
        println("пустое => сво-во сохранения ФЗ выполняется")
    else {
        println("не пустое => переход к шагу 5")
        val G = mutableMapOf<MutableSet<String>, MutableSet<String>>()
        map.forEach { (t, u) ->
            G[t.toMutableSet()] = u.toMutableSet()
        }
        H.forEach { (t, u) ->
            G[t] = G[t]?.minus(u)?.toMutableSet()!!
            if (G[t]?.size == 0)
                G.remove(t)
        }
        println("5.")
        var flag = true
        for ((t, u) in H.entries) {
            print("$t+G-H = ")
            val cl = closure(t, G)
            print(cl)
            if (!cl.containsAll(u)) {
                flag = false
                print(" не ⊇ $u")
                break
            }
            println(" ⊇ $u")
        }
        if (flag)
            println("=> Декомпозиция обладает св-вом сохранения ФЗ!")
        else println("=> Декомпозиция НЕ обладает св-вом сохранения ФЗ")
    }
}

fun FDToString(map: MutableMap<MutableSet<String>, MutableSet<String>>): String {
    var out = ""
    map.forEach { (t, u) ->
        out += "${t.joinToString(",")}–>${u.joinToString(",")}; "
    }
    return out.dropLast(2)
}

@JvmName("FDToString1")
fun FDToString(map: MutableMap<MutableSet<String>, String>): String {
    var out = ""
    map.forEach { (t, u) ->
        out += "${t.joinToString(",")}–>$u; "
    }
    return out.dropLast(2)
}

fun printMtx(matrix: Array<Array<String>>, title: List<String>, column: List<Set<String>>) {
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
        arr.forEachIndexed { j, it ->
            print(it.padEnd(title[j].length + 2))
        }
        println()
    }
    println()
}