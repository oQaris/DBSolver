package com.example.dbsolver

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerAdapter(
    val values: MutableList<Pair<String, String>>,
    private val h: Handler,
    //private val textChangedListener: (EditText, Int) -> Unit
) : RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {

    private var focusPos = 0
    private var isFocusDet = true
    private var isEditing = true

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
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

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNum: TextView = itemView.findViewById(R.id.txtNum)
        val txtDet: EditText = itemView.findViewById(R.id.txtDet)
        val txtDep: EditText = itemView.findViewById(R.id.txtDep)

        init {
            txtDet.addTextChangedListener { textChanged(txtDet, adapterPosition) }
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
        } else if (values.all { it.first.isNotEmpty() && it.second.isNotEmpty() }) {
            values.add("" to "")
            h.post(::notifyDataSetChanged)
        }
    }
}