package com.example.dbsolver.logic

data class FD(val det: Set<String>, val dep: Set<String>) {
    override fun toString() = "${toStr(det)}–>${toStr(dep)}"
}

infix fun String.f(attr: String) = FD(setOf(this), setOf(attr))
infix fun <A : Set<String>> A.f(attr: String) = FD(this, setOf(attr))
infix fun <B : Set<String>> String.f(attr: B) = FD(setOf(this), attr)
infix fun <A : Set<String>, B : Set<String>> A.f(attr: B) = FD(this, attr)

class Relations(private val funcDep: MutableMap<MutableSet<String>, MutableSet<String>> = linkedMapOf()) :
    MutableMap<MutableSet<String>, MutableSet<String>> by funcDep {
    val allAttr = mutableSetOf<String>()

    /*val unionPairs: MutableSet<FD>
        get() {
            val entries = mutableSetOf<FD>()
            funcDep.forEach { (det, dep) ->
                entries.add(det f dep)
            }
            return entries
        }*/
    val singlePairs: MutableSet<FD>
        get() {
            val entries = mutableSetOf<FD>()
            funcDep.forEach { (det, dep) ->
                dep.forEach {
                    entries.add(det f it)
                }
            }
            return entries
        }

    fun remove(fd: FD) {
        funcDep[fd.det]?.removeAll(fd.dep)
        if (funcDep[fd.det]?.size == 0)
            funcDep.remove(fd.det)
    }

    fun copy(): Relations {
        val out = Relations()
        funcDep.forEach { (t, u) ->
            out[t.toMutableSet()] = u.toMutableSet()
        }
        return out
    }

    fun toString(separator: String): String {
        var out = ""
        funcDep.forEach { (t, u) ->
            out += "${setToString(t)}–>${setToString(u)}$separator"
        }
        return out.dropLast(separator.length)
    }

    private fun setToString(set: MutableSet<String>): String {
        if (set.size == 1)
            return set.first()
        return "{${set.joinToString(",")}}"
    }
}