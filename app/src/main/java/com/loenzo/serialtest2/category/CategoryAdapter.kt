package com.loenzo.serialtest2.category

import android.content.Context
import android.graphics.Point
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.room.LastPicture

class CategoryAdapter (private var context: Context, private var data: ArrayList<LastPicture>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v= LayoutInflater.from(parent.context).inflate(R.layout.category_main, parent, false)
        return ItemHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val textView = holder.itemView.findViewById<TextView>(R.id.text_view_category)
        val item = data[position]

        textView.text = item.title
    }

}