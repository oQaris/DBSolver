package com.example.dbsolver

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dbsolver.databinding.ActivityMainBinding
import com.example.dbsolver.logic.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var h: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        h = Handler()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = CustomRecyclerAdapter(fillList(), h)
    }

    private fun fillList(): MutableList<Pair<String, String>> {
        val data = mutableListOf<Pair<String, String>>()
        data.add("K№  n" to "{A, f1, L}")
        data.add("" to "")
        return data
    }

    fun txtEndSelection(v: View) {
        if (v is EditText)
            v.setSelection(v.length())
    }

    fun btnSolveClick(v: View) {
        try {
            val pairs = (binding.recyclerView.adapter as CustomRecyclerAdapter).values
            pairs.removeLast()
            if (pairs.any { it.first.isEmpty() || it.second.isEmpty() })
                throw IllegalArgumentException("Невозможен пустой детерминант или зависимая часть!")

            val rel = readRelations(pairs)
            minCover(rel, true)
            allClosure(rel, true)
            minKeys(rel, true)
            nonTrivialFDs(rel, true)
            decomposition(rel, true)
            binding.textView.text = Log.str
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }
}