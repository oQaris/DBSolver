package com.pryanik.dbsolver.logic

import com.pryanik.dbsolver.logic.algorithms.Attr

data class FD(val det: Set<String>, val dep: Set<String>) {
    override fun toString() = "${toStr(det)}$arrow${toStr(dep)}"
}

infix fun String.f(attr: String) = FD(setOf(this), setOf(attr))
infix fun <A : Set<String>> A.f(attr: String) = FD(this, setOf(attr))
infix fun <B : Set<String>> String.f(attr: B) = FD(setOf(this), attr)
infix fun <A : Set<String>, B : Set<String>> A.f(attr: B) = FD(this, attr)

data class FuncDeps(private val data: MutableMap<MutableSet<String>, MutableSet<String>> = linkedMapOf()) :
    MutableMap<MutableSet<String>, MutableSet<String>> by data {

    val allAttr: Set<String>
        get() = data.keys.flatten() union data.values.flatten()

    val singlePairs: MutableSet<FD>
        get() = buildSet {
            data.forEach { (det, dep) ->
                dep.forEach { add(det f it) }
            }
        }.toMutableSet()

    fun remove(fd: FD) {
        data[fd.det]?.removeAll(fd.dep)
        if (data[fd.det]?.size == 0)
            data.remove(fd.det)
    }

    fun copy() =
        FuncDeps().also {
            data.forEach { (det, dep) ->
                it[det.toMutableSet()] = dep.toMutableSet()
            }
        }

    fun toStr(separator: String, isSinglePairs: Boolean = false) =
        buildList {
            if (isSinglePairs)
                for ((t, attr) in singlePairs)
                    add("${t f attr}")
            else for ((t, u) in data)
                add("${t f u}")
        }.joinToString(separator)
}

// TODO
data class SetAttr(private val set: MutableSet<Attr>) : MutableSet<Attr> by set {

}