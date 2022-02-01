package com.pryanik.dbsolver.logic.algorithms

import com.pryanik.dbsolver.logic.FuncDeps

data class Attr(val lit: String, val type: TypeAttr)

enum class TypeAttr(val str: String) {
    PRIMARY_KEY("(PK)"), FOREIGN_KEY("(FK)"), NOT_KEY("")
}

class Relation(private val allAttr: MutableSet<Attr> = mutableSetOf()) :
    MutableSet<Attr> by allAttr {

    fun add(lit: String) = this.add(Attr(lit, TypeAttr.NOT_KEY))

    fun addPrimaryKey(lit: String) = this.add(Attr(lit, TypeAttr.PRIMARY_KEY))

    fun addForeignKey(lit: String) = this.add(Attr(lit, TypeAttr.FOREIGN_KEY))

    fun getPrimaryKeys() = this.filter { it.type == TypeAttr.PRIMARY_KEY }

    fun getForeignKeys() = this.filter { it.type == TypeAttr.FOREIGN_KEY }

    fun toSetLit() = this.map { it.lit }

    override fun toString() = "(${this.joinToString(",") { "${it.lit}${it.type.str}" }})"

    fun copy() = Relation(allAttr.map { it.copy() }.toMutableSet())

    override fun equals(other: Any?): Boolean {
        if (other === this)
            return true
        if (other !is Relation)
            return false
        return allAttr == other.allAttr
    }

    override fun hashCode(): Int {
        return allAttr.hashCode()
    }
}

class Decomposition(val dcmpSet: MutableSet<Relation> = mutableSetOf()) :
    MutableSet<Relation> by dcmpSet {

    fun add(set: Set<Attr>) {
        this.add(Relation(set.toMutableSet()))
    }

    fun copy() = Decomposition(dcmpSet.map { it.copy() }.toMutableSet())

    override fun toString() = this.joinToString("\n")

    override fun equals(other: Any?): Boolean {
        if (other === this)
            return true
        if (other !is Decomposition)
            return false
        return dcmpSet == other.dcmpSet
    }

    override fun hashCode(): Int {
        return dcmpSet.hashCode()
    }
}

// Extensions

fun Iterable<String>.toRelation() = Relation(this.map { it.toAttr() }.toMutableSet())

fun String.toAttr(type: TypeAttr = TypeAttr.NOT_KEY) = Attr(this, type)

fun Iterable<Relation>.toDecomposition() = Decomposition(this.toMutableSet())

fun FuncDeps.extractFor(rel: Relation): FuncDeps {
    val setLitInRel = rel.toSetLit()
    val newFD = FuncDeps()
    this.forEach { (det, dep) ->
        if (setLitInRel.containsAll(det)) {
            val newDep = dep intersect setLitInRel
            if (newDep.isNotEmpty())
                newFD[det] = newDep.toMutableSet()
        }
    }
    return newFD
}

fun FuncDeps.toUniversalDcmp() = listOf(this.allAttr.toRelation()).toDecomposition()