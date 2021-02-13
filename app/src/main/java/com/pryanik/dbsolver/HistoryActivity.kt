package com.pryanik.dbsolver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
        File(cacheDir.parent!! + "/shared_prefs/").list()
            ?.filter { it.startsWith("sv-") }
            ?.reversed()
            ?.forEach { name ->
                val pair = load(name.dropLast(4))
                dates.add(name.substring(3..name.length - 9))
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
}