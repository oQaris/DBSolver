package com.example.dbsolver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dbsolver.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {
    private lateinit var bind: ActivityHistoryBinding
    private lateinit var hAdapter: HistoryRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityHistoryBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)

        hAdapter = HistoryRecyclerAdapter(loadFDs(), loadDcmps())
        bind.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        bind.historyRecyclerView.adapter = hAdapter
    }

    private fun loadFDs(): List<String> {
        return mutableListOf("12312", "dfghxdhg", "9kjdfogi9845")
    }

    private fun loadDcmps(): List<String> {
        return mutableListOf("47568647", "fgh768456", "sdfg kasfog", "-- jk sdoi 4")
    }
}