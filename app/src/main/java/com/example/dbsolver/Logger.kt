package com.example.dbsolver

import android.os.Build
import android.text.Html
import android.text.Spanned

object Log {
    var turnOn = true
    var str = ""
        private set

    fun ln(line: String, tag: String = "") {
        if (turnOn)
            str += if (tag.isEmpty()) "$line<br/>"
            else "<$tag>$line</$tag><br/>"
    }

    fun ln() {
        if (turnOn)
            str += "<br/>"
    }

    fun l(line: String, vararg tag: String) {
        if (turnOn)
            if (tag.isEmpty()) str += line
            else {
                tag.reversed().forEach { str += "<$it>" }
                str += line
                tag.forEach { str += "</$it>" }
            }
    }

    fun clear() {
        str = ""
    }

    fun toSpanned(): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY)
        else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(str)
        }
    }
}