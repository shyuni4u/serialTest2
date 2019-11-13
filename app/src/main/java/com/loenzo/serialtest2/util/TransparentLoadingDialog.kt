package com.loenzo.serialtest2.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.loenzo.serialtest2.R

class TransparentLoadingDialog(context: Context): Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading_dialog)
        setCanceledOnTouchOutside(false)
    }

    override fun onBackPressed() {}
}