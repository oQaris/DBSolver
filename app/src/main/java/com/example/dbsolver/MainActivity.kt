package com.example.dbsolver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dbsolver.databinding.ActivityMainBinding
import com.example.dbsolver.logic.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val h = Handler()
    private lateinit var fAdapter: FDRecyclerAdapter
    private lateinit var dAdapter: DcmpRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fAdapter = FDRecyclerAdapter(fillListFD(), h, binding.fdRecyclerView)
        dAdapter = DcmpRecyclerAdapter(fillListDcmp(), h, binding.dcmpRecyclerView)

        binding.fdRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.dcmpRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.fdRecyclerView.adapter = fAdapter
        binding.dcmpRecyclerView.adapter = dAdapter
    }

    private fun fillListFD(): MutableList<Pair<String, String>> {
        val data = mutableListOf<Pair<String, String>>()
        data.add("A B" to "C D")
        data.add("D A" to "C")
        data.add("C" to "A")
        data.add("" to "")
        return data
    }

    private fun fillListDcmp(): MutableList<String> {
        val data = mutableListOf<String>()
        data.add("A B")
        data.add("C D")
        data.add("")
        return data
    }

    fun txtEndSelection(v: View) {
        if (v is EditText)
            v.setSelection(v.length())
    }

    fun btnFuncClick(v: View) {
        startActivity(Intent(this, OptionsActivity::class.java))
    }

    fun btnSolveClick(v: View) {
        // Скрываем клавиатуру
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        try {
            val rel = readRelations(fAdapter.getFDs())
            val dcmp = readDecomposition(dAdapter.getDcmp(), rel)
            if (rel.isEmpty()) {
                throw IllegalArgumentException("Введите функциональные зависимости!")
            }
            Log.clear()
            hasInput(rel)
            minCover(rel, true)
            allClosure(rel, true)
            minKeys(rel, true)
            nonTrivialFDs(rel, true)
            decomposition(rel, true)
            if (dcmp.isNotEmpty()) {
                isLosslessConnection(rel, dcmp, true)
                isFuncDepPersistence(rel, dcmp, true)
            }
            binding.textView.loadDataWithBaseURL(
                "file:///android_asset/.",
                Log.str,
                "text/html",
                "UTF-8",
                null
            )
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }
}