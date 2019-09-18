package com.loenzo.serialtest2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class GalleryDetailAdapter(private var context: Context, private var data: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class DetailItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.gallery_detail_item, parent, false)
        return DetailItemHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Glide.with(context)
            .load(data[position])
            .thumbnail(0.3F)
            .error(R.drawable.no_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.itemView.findViewById(R.id.imgGalleryDetail))
    }
}