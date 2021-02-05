package com.example.dbsolver

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dbsolver.databinding.ActivityMainBinding
import com.example.dbsolver.logic.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val h = Handler()
    private lateinit var fAdapter: FDRecyclerAdapter
    private lateinit var dAdapter: DcmpRecyclerAdapter

    /*private var isMinCover = true
    private var isClosure = true
    private var isKeys = true
    private var isNonTrivial = true
    private var isDecomposition = true
    private var isLosslessConnection = true
    private var isPersistence = true*/
    private val menuArr = Array(7) { true }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.line.clipToOutline = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        menu.forEach { it.isChecked = true }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i: Int
        when (item.itemId) {
            R.id.action_min_cover -> {
                i = 0
            }
            R.id.action_closure -> {
                i = 1
            }
            R.id.action_keys -> {
                i = 2
            }
            R.id.action_non_trivial -> {
                i = 3
            }
            R.id.action_decomposition -> {
                i = 4
            }
            R.id.action_lossless_connection -> {
                i = 5
            }
            R.id.action_persistence -> {
                i = 6
            }
            else -> return super.onOptionsItemSelected(item)
        }
        item.isChecked = !item.isChecked
        menuArr[i] = item.isChecked
        return true
    }

    private fun fillListFD(): MutableList<Pair<String, String>> {
        val data = mutableListOf<Pair<String, String>>()
        data.add("A" to "C B")
        data.add("C" to "D E")
        data.add("F" to "I")
        data.add("A F" to "G H")
        data.add("" to "")
        return data
    }

    private fun fillListDcmp(): MutableList<String> {
        val data = mutableListOf<String>()
        data.add("")
        return data
    }

    fun txtEndSelection(v: View) {
        if (v is EditText)
            v.setSelection(v.length())
    }

    fun btnHistoryClick(v: MenuItem) {
        Toast.makeText(this, "В разработке!", Toast.LENGTH_SHORT).show()
    }

    fun btnSolveClick(v: MenuItem) {
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
            if (menuArr[0])
                minCover(rel, true)
            if (menuArr[1])
                allClosure(rel, true)
            if (menuArr[2])
                minKeys(rel, true)
            if (menuArr[3])
                nonTrivialFDs(rel, true)
            if (menuArr[4])
                decomposition(rel, true)
            if (dcmp.isNotEmpty()) {
                if (menuArr[5])
                    isLosslessConnection(rel, dcmp, true)
                if (menuArr[6])
                    isFuncDepPersistence(rel, dcmp, true)
            }
            binding.txtResult.loadDataWithBaseURL(
                "file:///android_asset/.",
                Log.toString(),
                "text/html",
                "UTF-8",
                null
            )
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }
}