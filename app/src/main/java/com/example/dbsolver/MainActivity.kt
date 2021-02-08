package com.example.dbsolver

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import com.google.firebase.perf.metrics.AddTrace

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
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

    @AddTrace(name = "onCreateTrace", enabled = true)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)

        fAdapter = FDRecyclerAdapter(fillListFD(), h, bind.fdRecyclerView)
        dAdapter = DcmpRecyclerAdapter(fillListDcmp(), h, bind.dcmpRecyclerView)

        bind.fdRecyclerView.layoutManager = LinearLayoutManager(this)
        bind.dcmpRecyclerView.layoutManager = LinearLayoutManager(this)
        bind.fdRecyclerView.adapter = fAdapter
        bind.dcmpRecyclerView.adapter = dAdapter
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bind.line.clipToOutline = true
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        menu.forEach { it.isChecked = true }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val idx = when (item.itemId) {
            R.id.action_min_cover -> 0
            R.id.action_closure -> 1
            R.id.action_keys -> 2
            R.id.action_non_trivial -> 3
            R.id.action_decomposition -> 4
            R.id.action_lossless_connection -> 5
            R.id.action_persistence -> 6
            else -> return super.onOptionsItemSelected(item)
        }
        item.isChecked = !item.isChecked
        menuArr[idx] = item.isChecked
        return true
    }

    private fun fillListFD(): MutableList<Pair<String, String>> {
        val data = mutableListOf<Pair<String, String>>()
        /*data.add("A" to "C B")
        data.add("C" to "D E")
        data.add("F" to "I")
        data.add("A F" to "G H")*/
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
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    @AddTrace(name = "btnSolveTrace", enabled = true)
    fun btnSolveClick(v: MenuItem) {
        // Скрываем клавиатуру
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        try {
            val rel = readRelations(fAdapter.getFDs())
            val dcmp = readDecomposition(dAdapter.getDcmp(), rel)
            if (rel.isEmpty())
                throw IllegalArgumentException("Введите функциональные зависимости!")
            Log.clear()
            hasInput(rel)
            for (i in menuArr.indices) {
                if (menuArr[i])
                    when (i) {
                        0 -> minCover(rel, true)
                        1 -> allClosure(rel, true)
                        2 -> minKeys(rel, true)
                        3 -> nonTrivialFDs(rel, true)
                        4 -> decomposition(rel, true)
                        5 -> if (dcmp.isNotEmpty()) isLosslessConnection(rel, dcmp, true)
                        6 -> if (dcmp.isNotEmpty()) isFuncDepPersistence(rel, dcmp, true)
                    }
            }
            /*if (menuArr[0])
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
            }*/
            bind.txtResult.loadDataWithBaseURL(null, Log.toString(), null, null, null)
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun btnGenerateDecompositionClick(v: View) {
        if (dAdapter.getDcmp().isNotEmpty()) {
            AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("Сгенерировать декомпозицию?")
                .setMessage("Введённая декомпозици заменится на сгенерированную. Продолжить?")
                .setPositiveButton("Да") { _: DialogInterface?, _: Int ->
                    changeDecomposition()
                }
                .setNegativeButton("Нет") { _, _ -> }
                .create()
                .show()
        } else {
            changeDecomposition()
            Toast.makeText(this, "Декомпозиция сгенерирована!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeDecomposition() {
        dAdapter.values.clear()
        dAdapter.values.addAll(decomposition(readRelations(fAdapter.getFDs()))
            .map { set -> set.joinToString(", ") })
        dAdapter.values.add("")
        dAdapter.notifyDataSetChanged()
        //notifyItemInserted(position)
    }
}