package com.zkrallah.esp32connectiondemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zkrallah.esp32connectiondemo.R
import com.zkrallah.esp32connectiondemo.model.Message

class MessagesAdapter(private val list: MutableList<Message>) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.messageTxt.text = list[position].message
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addMessage(message: Message) {
        list.add(message)
        notifyItemInserted(list.size)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTxt = itemView.findViewById<TextView>(R.id.txt_message)
    }
}