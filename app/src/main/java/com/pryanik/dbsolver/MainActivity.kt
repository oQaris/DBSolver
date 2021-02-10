package com.pryanik.dbsolver

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dbsolver.R
import com.example.dbsolver.databinding.ActivityMainBinding
import com.google.firebase.perf.metrics.AddTrace
import com.pryanik.dbsolver.logic.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private val h = Handler(Looper.getMainLooper())
    private lateinit var fAdapter: FDRecyclerAdapter
    private lateinit var dAdapter: DcmpRecyclerAdapter
    private val menuArr = Array(7) { true }
    //private val sp = getSharedPreferences("history", MODE_PRIVATE)

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
        //todo убрать в продакшине
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
        //Toast.makeText(this, "Скоро будет доступно!", Toast.LENGTH_SHORT).show()
    }

    @AddTrace(name = "btnSolveTrace", enabled = true)
    fun btnSolveClick(v: MenuItem) {
        // Скрываем клавиатуру
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        try {
            val rel = parseRelations(fAdapter.getFDs())
            val dcmp = parseDecomposition(dAdapter.getDcmp(), rel)
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
            bind.txtResult.loadDataWithBaseURL(null, Log.toString(), null, null, null)
            save(
                rel.toString("\n").replace("<.*?>".toRegex(), ""),
                dcmp.joinToString("\n") { toStr(it) })
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun save(fd: String, dcmp: String) {
        getSharedPreferences(
            "sv-" + SimpleDateFormat(
                "yyyy.MM.dd\nHH:mm:ss.SSS",
                Locale.getDefault()
            ).format(Date()), MODE_PRIVATE
        ).edit()
            .putString("fd", fd)
            .putString("dcmp", dcmp)
            .apply()
    }

    fun btnGenerateDecompositionClick(v: View) {
        try {
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
            } else changeDecomposition()
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeDecomposition() {
        dAdapter.values.clear()
        val rel = parseRelations(fAdapter.getFDs())
        if (rel.isEmpty())
            throw IllegalArgumentException("Введите функциональные зависимости!")
        dAdapter.values.addAll(decomposition(rel)
            .map { set -> set.joinToString(", ") })
        dAdapter.values.add("")
        dAdapter.notifyDataSetChanged()
        //notifyItemInserted(position)
        Toast.makeText(this, "Декомпозиция сгенерирована!", Toast.LENGTH_SHORT).show()
    }
}