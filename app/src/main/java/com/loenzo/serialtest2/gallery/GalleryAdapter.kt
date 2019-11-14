package com.loenzo.serialtest2.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.util.*
import java.io.File

class GalleryAdapter(private var context: Context, private var data: ArrayList<String>, private val state: GalleryState):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    var actionmode = false
    val selectedItem = ArrayList<String>()
    val selectedView = ArrayList<View>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
        return ItemHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        fun selectItem(item: String, view: View) {
            if (actionmode) {
                if (selectedItem.contains(item)) {
                    selectedItem.remove(item)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        view.foreground = null
                    }
                } else {
                    selectedItem.add(item)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        view.foreground = view.resources.getDrawable(R.drawable.select_image, null)
                    }
                }
            }
        }

        when (state) {
            GalleryState.CAMERA -> Glide.with(context)
                .load(data[position])
                .thumbnail(0.3F)
                .override(200, 200)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.itemView.findViewById(R.id.imgGalleryItem))
            GalleryState.GIF -> Glide.with(context)
                .asGif()
                .load(data[position])
                .thumbnail(0.3F)
                .override(200, 200)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.itemView.findViewById(R.id.imgGalleryItem))
            else -> Glide.with(context)
                .asBitmap()
                .load(data[position])
                .thumbnail(0.3F)
                .override(200, 200)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.itemView.findViewById(R.id.imgGalleryItem))
        }

        holder.itemView.setOnClickListener {
            val url = Uri.parse(data[holder.adapterPosition])
            if (actionmode) {
                selectItem(data[holder.adapterPosition], it)
                selectedView.add(it)
            } else {
                if (state == GalleryState.CAMERA || state == GalleryState.GIF) {
                    val intent = Intent(context, GalleryDetail::class.java)
                    intent.putExtra("STATE", state)
                    intent.putExtra("PARAM", data)
                    intent.putExtra("POSITION", holder.adapterPosition)
                    context.startActivity(intent)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, url).apply {
                        setDataAndType(url, "video/mp4")
                    }
                    context.startActivity(intent)
                }
            }
        }

        val actionModeCallbacks = object: ActionMode.Callback {

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                for (temp in selectedItem) {
                    data.remove(temp)
                    val delFile = File(temp)
                    delFile.delete()
                    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                        data = Uri.fromFile(delFile)
                    }
                    context.sendBroadcast(intent)
                }
                mode?.finish()
                return true
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                actionmode = true
                menu?.add(context.resources.getString(R.string.delete))
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                actionmode = false
                selectedItem.clear()
                for (temp in selectedView) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        temp.foreground = null
                    }
                }
                selectedView.clear()
                notifyDataSetChanged()
            }

        }

        holder.itemView.setOnLongClickListener {
            if (actionmode) {
                true
            } else {
                (it.context as AppCompatActivity).startSupportActionMode(actionModeCallbacks)
                selectItem(data[holder.adapterPosition], it)
                selectedView.add(it)
                false
            }
        }
    }
}