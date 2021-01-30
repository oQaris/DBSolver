package com.example.dbsolver

import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView

class FDRecyclerAdapter(
    private val values: MutableList<Pair<String, String>>,
    private val h: Handler,
    private val rv: RecyclerView
) : RecyclerView.Adapter<FDRecyclerAdapter.FDViewHolder>() {

    private var focusPos = 0
    private var isFocusDet = true
    private var isEditing = true

    fun getFDs(): List<Pair<String, String>> {
        val pairs = values.subList(0, values.size - 1)
        if (pairs.any { it.first.isEmpty() || it.second.isEmpty() })
            throw IllegalArgumentException("У ФЗ должны быть заполнены обе части!")
        return pairs
    }

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FDViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fd_list_item, parent, false)
        return FDViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FDViewHolder, position: Int) {
        isEditing = false
        // Сохраняем фокус
        if (focusPos == position)
            if (isFocusDet) holder.txtDet.requestFocus()
            else holder.txtDep.requestFocus()

        holder.txtNum.text = "${position + 1})"
        holder.txtDet.setText(values[position].first)
        holder.txtDet.setSelection(holder.txtDet.text.length)
        holder.txtDep.setText(values[position].second)
        holder.txtDep.setSelection(holder.txtDep.text.length)
        isEditing = true
    }

    inner class FDViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNum: TextView = itemView.findViewById(R.id.txtNumF)
        val txtDet: EditText = itemView.findViewById(R.id.txtDet)
        val txtDep: EditText = itemView.findViewById(R.id.txtDep)

        init {
            txtDet.addTextChangedListener { textChanged(txtDet, adapterPosition) }
            //todo По нажатию Ентер переход на следующий
            txtDet.setOnKeyListener { _, keyCode, event ->
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_DONE) && event.action == KeyEvent.ACTION_DOWN)
                    txtDep.requestFocus()
                true
            }
            txtDep.addTextChangedListener { textChanged(txtDep, adapterPosition) }
        }
    }

    private fun textChanged(editText: EditText, position: Int) {
        if (!isEditing) // чтоб не выполнялось для вызова setText
            return
        focusPos = position
        val pair = values[position]
        when (editText.id) {
            R.id.txtDet -> {
                values[position] = pair.copy(first = editText.text.toString())
                isFocusDet = true
            }
            R.id.txtDep -> {
                values[position] = pair.copy(second = editText.text.toString())
                isFocusDet = false
            }
        }
        if (values.count { it.first.isEmpty() && it.second.isEmpty() } > 1) {
            //todo Сделать красиво!
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                values.removeIf { it.first.isNotEmpty() && it.second.isNotEmpty() }*/
            for (i in values.indices)
                if (values[i] == "" to "") {
                    values.removeAt(i)
                    focusPos = values.lastIndexOf("" to "")
                    break
                }
            h.post(::notifyDataSetChanged)
            h.post { rv.smoothScrollToPosition(itemCount) }
        } else if (values.all { it.first.isNotEmpty() && it.second.isNotEmpty() }) {
            values.add("" to "")
            h.post(::notifyDataSetChanged)
            h.post { rv.smoothScrollToPosition(itemCount) }
        }
    }
}

class DcmpRecyclerAdapter(
    private val values: MutableList<String>,
    private val h: Handler,
    private val rv: RecyclerView
) : RecyclerView.Adapter<DcmpRecyclerAdapter.DcmpViewHolder>() {

    private var focusPos = 0
    private var isEditing = true

    fun getDcmp(): List<String> {
        return values.subList(0, values.size - 1)
    }

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DcmpViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dcmp_list_item, parent, false)
        return DcmpViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DcmpViewHolder, position: Int) {
        isEditing = false
        // Сохраняем фокус
        if (focusPos == position)
            holder.txtDcmp.requestFocus()

        holder.txtNum.text = "${position + 1})"
        holder.txtDcmp.setText(values[position])
        holder.txtDcmp.setSelection(holder.txtDcmp.text.length)
        isEditing = true
    }

    inner class DcmpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNum: TextView = itemView.findViewById(R.id.txtNumD)
        val txtDcmp: EditText = itemView.findViewById(R.id.txtDcmp)

        init {
            txtDcmp.addTextChangedListener { textChanged(txtDcmp, adapterPosition) }
        }
    }

    private fun textChanged(editText: EditText, position: Int) {
        if (!isEditing) // чтоб не выполнялось для вызова setText
            return
        focusPos = position
        values[position] = editText.text.toString()
        if (values.count { it.isEmpty() } > 1) {
            //todo Сделать красиво!
            for (i in values.indices)
                if (values[i] == "") {
                    values.removeAt(i)
                    focusPos = values.lastIndexOf("")
                    break
                }
            h.post(::notifyDataSetChanged)
            h.post { rv.smoothScrollToPosition(itemCount) }
        } else if (values.all { it.isNotEmpty() }) {
            values.add("")
            h.post(::notifyDataSetChanged)
            h.post { rv.smoothScrollToPosition(itemCount) }
        }
    }
}