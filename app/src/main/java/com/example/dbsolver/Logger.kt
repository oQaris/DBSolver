package com.example.dbsolver

object Log {
    var turnOn = true
    var str = ""
        private set

    fun ln(line: String) {
        if (turnOn)
            str += line + "\n"
    }

    fun ln() {
        if (turnOn)
            str += "\n"
    }

    fun l(line: String) {
        if (turnOn)
            str += line
    }

    fun clear() {
        str = ""
    }
}