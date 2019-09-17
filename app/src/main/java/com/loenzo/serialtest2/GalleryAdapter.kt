package com.loenzo.serialtest2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class GalleryAdapter(private var context: Context, private var data: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var mImg: ImageView? = null
        init {
            mImg = itemView.findViewById(R.id.imgGalleryItem)
        }

        override fun onClick(v: View?) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
        return ItemHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemHolder).mImg?.let {
            Glide.with(context)
                .load(data[position])
                .thumbnail(0.3F)
                .error(R.drawable.no_image)
                .override(200, 200)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(it)
        }
    }
}