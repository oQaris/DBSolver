package com.example.dbsolver

object Log {
    var str = ""
        private set

    fun ln(line: String, isLog: Boolean) {
        if (isLog)
            str += line + "\n"
    }

    fun ln(isLog: Boolean) {
        if (isLog)
            str += "\n"
    }

    fun l(line: String, isLog: Boolean) {
        if (isLog)
            str += line
    }
}