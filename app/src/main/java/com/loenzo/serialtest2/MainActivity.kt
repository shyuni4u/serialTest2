package com.loenzo.serialtest2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.loenzo.serialtest2.category.CategoryActivity
import com.loenzo.serialtest2.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val intent = Intent(this, CategoryActivity::class.java)
        startActivityForResult(intent, APPLICATION_SUCCESS)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == APPLICATION_SUCCESS && data != null) {
            finishAffinity()
            moveTaskToBack(true)
        }
    }
}
