package com.loenzo.serialtest2.camera

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.help.ManualActivity

class CategoryMenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.category_menu_fragment, container, false)

        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnRemove = view.findViewById<Button>(R.id.btnRemove)
        val btnHelp = view.findViewById<Button>(R.id.btnHelp)

        btnAdd.setOnClickListener {
            val addCategoryName = EditText(context)
            val builder = AlertDialog.Builder(context)

            builder.setTitle(context!!.resources.getString(R.string.add_category_title))
            builder.setView(addCategoryName)
            builder.setPositiveButton(context!!.resources.getString(R.string.add)
            ) { _, _ -> run {
                val newName = addCategoryName.text.toString()
                (context as CameraActivity).addRecyclerViewItem(newName)
            } }
            builder.setNegativeButton(context!!.resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }
        btnRemove.setOnClickListener {
            val check = CheckBox(context)
            val categoryName = (context as CameraActivity).getRecentName()
            check.text = context!!.resources.getString(R.string.include_images)
            val builder = AlertDialog.Builder(context)

            builder.setTitle(context!!.resources.getString(R.string.delete_category, categoryName))
            builder.setView(check)
            builder.setPositiveButton(context!!.resources.getString(R.string.delete)
            ) { _, _ -> run {
                (context as CameraActivity).removeRecyclerViewItem(check.isChecked)
            } }
            builder.setNegativeButton(context!!.resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }
        btnHelp.setOnClickListener {
            val intent = Intent(context, ManualActivity::class.java)
            startActivity(intent)
            btnHelp.isEnabled = false
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val btnHelp = view!!.findViewById<Button>(R.id.btnHelp)
        btnHelp.isEnabled = true
    }
}