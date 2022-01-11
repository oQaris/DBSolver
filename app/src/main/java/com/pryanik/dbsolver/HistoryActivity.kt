package com.pryanik.dbsolver

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dbsolver.R
import com.example.dbsolver.databinding.ActivityHistoryBinding
import java.io.File

class HistoryActivity : AppCompatActivity() {
    private lateinit var bind: ActivityHistoryBinding
    private lateinit var hAdapter: HistoryRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityHistoryBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)

        hAdapter = fillAdapter()
        bind.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        bind.historyRecyclerView.adapter = hAdapter
    }

    private fun fillAdapter(): HistoryRecyclerAdapter {
        val fds = mutableListOf<String>()
        val dcmps = mutableListOf<String>()
        val dates = mutableListOf<String>()
        savesFiles().forEach { file ->
            val pair = load(file.name.dropLast(4))
            dates.add(file.name.drop(3).dropLast(8))
            fds.add(pair.first)
            dcmps.add(pair.second)
        }
        return HistoryRecyclerAdapter(fds, dcmps, dates, this)
    }

    private fun load(name: String): Pair<String, String> {
        val sp = getSharedPreferences(name, MODE_PRIVATE)
        val fd = sp.getString("fd", "")!!
        val dcmp = sp.getString("dcmp", "")!!
        return (fd to dcmp)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    fun btnClearHistoryClick(item: MenuItem) {
        AlertDialog.Builder(this)
            .setCancelable(true)
            .setTitle("Очистить историю?")
            .setMessage("Все записи в истории будут удалены. Продолжить?")
            .setPositiveButton("Да") { _: DialogInterface?, _: Int ->
                savesFiles().forEach { it.delete() }
                finish()
            }
            .setNegativeButton("Нет") { _, _ -> }
            .create()
            .show()
    }

    private fun savesFiles(): List<File> {
        return File(cacheDir.parent!! + "/shared_prefs/").listFiles()!!
            .filter { it.name.startsWith("sv-") }
            .reversed()
    }
}