package com.pryanik.dbsolver

object Log {
    private var turnOn = false
    private var str = StringBuilder()
    private val switchList = mutableListOf<Boolean>()

    fun ln(line: String, tag: String = "") {
        if (turnOn)
            str.append(
                if (tag.isEmpty()) "$line<br/>"
                else "<$tag>$line</$tag><br/>"
            )
    }

    fun ln() {
        if (turnOn)
            str.append("<br/>")
    }

    fun l(line: String, vararg tag: String) {
        if (turnOn)
            if (tag.isEmpty()) str.append(line)
            else {
                tag.reversed().forEach { str.append("<$it>") }
                str.append(line)
                tag.forEach { str.append("</$it>") }
            }
    }

    fun clear() {
        str.clear()
    }

    fun setLogging(on: Boolean) {
        switchList.add(turnOn)
        turnOn = on
    }

    fun restoreLogging() {
        turnOn = switchList.removeLast()
    }

    override fun toString(): String {
        return str.toString()
    }

    /*fun toSpanned(): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY)
        else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(str)
        }
    }*/
}