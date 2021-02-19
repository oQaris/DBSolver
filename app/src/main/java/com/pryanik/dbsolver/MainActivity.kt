package com.pryanik.dbsolver

import android.annotation.SuppressLint
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

    @AddTrace(name = "onCreateTrace", enabled = true)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)

        val arguments = intent.extras
        fAdapter = FDRecyclerAdapter(fillListFD(arguments), h, bind.fdRecyclerView)
        dAdapter = DcmpRecyclerAdapter(fillListDcmp(arguments), h, bind.dcmpRecyclerView)

        bind.fdRecyclerView.layoutManager = LinearLayoutManager(this)
        bind.dcmpRecyclerView.layoutManager = LinearLayoutManager(this)
        bind.fdRecyclerView.adapter = fAdapter
        bind.dcmpRecyclerView.adapter = dAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val arguments = intent?.extras
        fAdapter.values.clear()
        fAdapter.values.addAll(fillListFD(arguments))
        fAdapter.notifyDataSetChanged()
        dAdapter.values.clear()
        dAdapter.values.addAll(fillListDcmp(arguments))
        dAdapter.notifyDataSetChanged()
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

    private fun fillListFD(bundle: Bundle?): MutableList<Pair<String, String>> {
        val data = mutableListOf<Pair<String, String>>()
        if (bundle != null)
            bundle.getString("fds")?.let { parsePairs(it) }?.let { data.addAll(it) }
        data.add("" to "")
        return data
    }

    private fun fillListDcmp(bundle: Bundle?): MutableList<String> {
        val data = mutableListOf<String>()
        if (bundle != null)
            bundle.getString("dcmps")?.let { parseDcmpStr(it) }?.let { data.addAll(it) }
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
            val rel = parseRelations(fAdapter.getFDs())
            val dcmp = parseDecomposition(dAdapter.getDcmp(), rel)
            require(rel.isNotEmpty()) { "Введите функциональные зависимости!" }
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
                        5 -> if (dcmp.isNotEmpty()) isLosslessJoin(rel, dcmp, true)
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

    @SuppressLint("NotifyDataSetChanged")
    private fun changeDecomposition() {
        val rel = parseRelations(fAdapter.getFDs())
        require(rel.isNotEmpty()) { "Введите функциональные зависимости!" }
        dAdapter.values.clear()
        dAdapter.values.addAll(decomposition(rel)
            .map { set -> set.joinToString(", ") })
        dAdapter.values.add("")
        dAdapter.notifyDataSetChanged()
        //notifyItemInserted(position)
        //notifyItemChanged(position)
        Toast.makeText(this, "Декомпозиция сгенерирована!", Toast.LENGTH_SHORT).show()
    }
}