package com.loenzo.serialtest2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_item)

        val test = "http://movie.phinf.naver.net/20171107_251/1510033896133nWqxG_JPEG/movie_image.jpg"

        Glide.with(this)
            .load(test)
            .thumbnail(0.1f)
            .placeholder(R.drawable.photo_pressed)
            .into(this.findViewById(R.id.imgGalleryItem))
    }
}