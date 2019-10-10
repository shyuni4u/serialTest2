package com.loenzo.serialtest2

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.scale
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.loenzo.serialtest2.encoder.AnimatedGifEncoder
import com.loenzo.serialtest2.room.LastPicture
import com.loenzo.serialtest2.util.*
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Rational
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class CategoryAdapter (private var context: Context, private var data: ArrayList<LastPicture>, private val recyclerView: RecyclerView):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val MOVIE = 1
        private const val GIF = 2
    }

    private lateinit var imageView: ImageView
    private lateinit var tempTextView: TextView

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun changeAlarmState(position: Int) {
        data[position].alarmState = !data[position].alarmState
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.row_category, parent, false)
        return ItemHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        imageView = holder.itemView.findViewById(R.id.lastImage)
        val item = data[position]

        val textView  = holder.itemView.findViewById<TextView>(R.id.categoryName)
        textView.text = item.title
        tempTextView = textView

        Glide.with(context)
            .load(
                getRecentFilePathFromCategoryName(
                    item.title,
                    context
                )
            )
            .thumbnail(0.1F)
            .into(imageView)

        /**
         * set button click listener
         */
        val btnNotification = holder.itemView.findViewById<ImageButton>(R.id.btnNotification)
        val btnAdd = holder.itemView.findViewById<ImageButton>(R.id.btnAdd)
        val btnRemove = holder.itemView.findViewById<ImageButton>(R.id.btnRemove)
        val btnCamera = holder.itemView.findViewById<ImageButton>(R.id.btnCamera)
        val btnVideo = holder.itemView.findViewById<ImageButton>(R.id.btnVideo)
        val btnGif = holder.itemView.findViewById<ImageButton>(R.id.btnGif)

        if (data[position].alarmState) {
            btnNotification.setImageResource(R.drawable.main_btn_notification)
        } else {
            btnNotification.setImageResource(R.drawable.main_btn_notification_off)
        }
        btnNotification.setOnClickListener {
            if (data[position].alarmState) {    //Off
                scheduleNotificationStop(context, data[position].id)
                data[position].alarmState = false
                btnNotification.setImageResource(R.drawable.main_btn_notification_off)
                notifyDataSetChanged()
            } else {    //On
                scheduleNotification(
                    context,
                    data[position].alarmMilliseconds,
                    data[position].title,
                    data[position].id
                )
                data[position].alarmState = true
                btnNotification.setImageResource(R.drawable.main_btn_notification)
                notifyDataSetChanged()
            }
            val array = arrayOfNulls<LastPicture>(data.size)
            writeSetting(data.toArray(array))
        }
        btnNotification.setOnLongClickListener {
            val originCalendar = Calendar.getInstance()
            originCalendar.timeInMillis = data[position].alarmMilliseconds
            if (originCalendar.timeInMillis.toInt() == 0) {
                originCalendar.timeInMillis = System.currentTimeMillis()
            }

            TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    cal.set(Calendar.MINUTE, minute)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    data[position].alarmMilliseconds = cal.timeInMillis
                }, originCalendar.get(Calendar.HOUR_OF_DAY), originCalendar.get(Calendar.MINUTE), true).apply { show() }

            val array = arrayOfNulls<LastPicture>(data.size)
            writeSetting(data.toArray(array))
            false
        }

        btnAdd.setOnClickListener {
            val addCategoryName = EditText(context)
            val builder = AlertDialog.Builder(context)

            builder.setTitle(context.resources.getString(R.string.add_category_title))
            builder.setView(addCategoryName)
            builder.setPositiveButton(context.resources.getString(R.string.add)
            ) { _, _ -> run {
                val newName = addCategoryName.text.toString()
                var isDuplicated = false

                // check newName exists
                for (info in data) {
                    if (info.title == newName) {
                        Toast.makeText(context, context.resources.getString(R.string.duplicate_name), Toast.LENGTH_SHORT).apply {
                            setGravity(Gravity.BOTTOM, 0, 100)
                        }.show()
                        isDuplicated = true
                    }
                }

                if (!isDuplicated) {
                    data.add(LastPicture(newName, ""))
                    notifyItemInserted(data.size)
                    recyclerView.smoothScrollToPosition(data.size)

                    val array = arrayOfNulls<LastPicture>(data.size)
                    writeSetting(data.toArray(array))
                }
            } }
            builder.setNegativeButton(context.resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }

        btnRemove.setOnClickListener {
            val check = CheckBox(context)
            check.text = context.resources.getString(R.string.include_images)
            val builder = AlertDialog.Builder(context)

            builder.setTitle(context.resources.getString(R.string.delete_category, item.title))
            builder.setView(check)
            builder.setPositiveButton(context.resources.getString(R.string.delete)
            ) { _, _ -> run {
                var temp: LastPicture? = null
                for(el in data) {
                    if (el.title == item.title) {
                        temp = el
                    }
                }

                if (data.size > 1) {
                    if (temp != null) data.remove(temp)
                    notifyItemRemoved(position)

                    val array = arrayOfNulls<LastPicture>(data.size)
                    writeSetting(data.toArray(array))

                    scheduleNotificationStop(context, item.id)

                    if (check.isChecked) {
                        val sdcard: String = Environment.getExternalStorageState()
                        val rootDir: File = when (sdcard != Environment.MEDIA_MOUNTED) {
                            true -> Environment.getRootDirectory()
                            false -> Environment.getExternalStorageDirectory()
                        }
                        setDirectoryEmpty(rootDir.absolutePath + "/$APP_NAME/${item.title}")
                    }
                }
            } }
            builder.setNegativeButton(context.resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }

        btnCamera.setOnClickListener {
            (context as MainActivity).openCamera(item)
        }
        btnCamera.setOnLongClickListener {
            (context as MainActivity).openGallery(item)
            false
        }

        btnVideo.setOnClickListener {
            val builder = AlertDialog.Builder(context)

            val etName = EditText(context)
            etName.hint = context.resources.getString(R.string.movie_name)
            etName.text = etName.text.append(item.title)

            val etFps = EditText(context)
            etFps.hint = context.resources.getString(R.string.movie_time)
            etFps.inputType = InputType.TYPE_CLASS_NUMBER
            etFps.text = etFps.text.append("8")

            val lay = LinearLayout(context)
            lay.orientation = LinearLayout.VERTICAL
            lay.addView(etName)
            lay.addView(etFps)
            builder.setView(lay)

            builder.setTitle(context.resources.getString(R.string.add_movie_title))
            builder.setPositiveButton(context.resources.getString(R.string.apply)
            ) { _, _ -> run {
                if (etName.text.toString() != "" && etFps.text.toString() != "") {
                    AsyncMakeVideo().execute(
                        ParamVideo(item.title, etName.text.toString(), Integer.parseInt(etFps.text.toString()), MOVIE)
                    )
                }
            } }
            builder.setNegativeButton(context.resources.getString(R.string.cancel)
            ) { _, _ -> run {} }
            builder.show()
        }
        btnVideo.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setDataAndType(
                Uri.parse(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES).absolutePath), "video/*")
            if (intent.resolveActivity(context.packageManager) != null) {
                (context as Activity).startActivityForResult(
                    Intent.createChooser(
                        intent,
                        context.resources.getString(R.string.choose_app)
                    ), SELECT_MEDIA_SUCCESS
                )
            }
            false
        }

        btnGif.setOnClickListener {
            val builder = AlertDialog.Builder(context)

            val etName = EditText(context)
            etName.hint = context.resources.getString(R.string.movie_name)
            etName.text = etName.text.append(item.title)

            val etFps = EditText(context)
            etFps.hint = context.resources.getString(R.string.movie_time)
            etFps.inputType = InputType.TYPE_CLASS_NUMBER
            etFps.text = etFps.text.append("8")

            val lay = LinearLayout(context)
            lay.orientation = LinearLayout.VERTICAL
            lay.addView(etName)
            lay.addView(etFps)
            builder.setView(lay)

            builder.setTitle(context.resources.getString(R.string.add_movie_title))
            builder.setPositiveButton(context.resources.getString(R.string.apply)
            ) { _, _ -> run {
                if (etName.text.toString() != "" && etFps.text.toString() != "") {
                    AsyncMakeVideo().execute(
                        ParamVideo(item.title, etName.text.toString(), Integer.parseInt(etFps.text.toString()), GIF)
                    )
                }
            } }
            builder.setNegativeButton(context.resources.getString(R.string.cancel)
            ) { _, _ -> run {} }
            builder.show()
        }
        btnGif.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setDataAndType(
                Uri.parse(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).absolutePath), "image/*")
            if (intent.resolveActivity(context.packageManager) != null) {
                (context as Activity).startActivityForResult(
                    Intent.createChooser(
                        intent,
                        context.resources.getString(R.string.choose_app)
                    ), SELECT_MEDIA_SUCCESS
                )
            }
            false
        }
    }

    inner class ParamVideo (val categoryName: String, val name: String, val fps: Int, val type: Int)

    @SuppressLint("StaticFieldLeak")
    inner class AsyncMakeVideo : AsyncTask<ParamVideo, Int, Int>() {

        private lateinit var strCategory: String
        private lateinit var strName: String
        private lateinit var strPath: String

        override fun doInBackground(vararg params: ParamVideo?): Int {
            var returnValue = -1
            if (params[0] != null) {
                val temp = params[0]!!
                val data = getRecentFilePathListFromCategoryName(
                    temp.categoryName,
                    context
                )
                strCategory = temp.categoryName

                if (data.size > 0) {
                    if (temp.type == MOVIE) {
                        val rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                        val filePath = rootDir.absolutePath + "/${temp.name}.mp4"
                        strName = "${temp.name}.mp4"
                        strPath = filePath
                        NIOUtils.writableFileChannel(filePath).use { fileChannel ->
                            AndroidSequenceEncoder(
                                fileChannel,
                                Rational.R(temp.fps, 1)
                            ).let { encoder ->
                                data.map {
                                    val nProgress = 100 * data.indexOf(it) / data.size
                                    publishProgress(nProgress)
                                    encoder.encodeImage(
                                        BitmapFactory.decodeFile(it).scale(
                                            1280,
                                            720,
                                            false
                                        )
                                    )
                                }
                                encoder.finish()
                                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                                    this.data = Uri.fromFile(File(filePath))
                                }
                                context.sendBroadcast(intent)
                            }
                        }
                    } else if (temp.type == GIF) {
                        val bos = ByteArrayOutputStream()
                        val gifEncoder = AnimatedGifEncoder()
                        gifEncoder.setDelay(temp.fps / 1000)
                        gifEncoder.setRepeat(0)
                        gifEncoder.start(bos)

                        val rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val filePath = rootDir.absolutePath + "/${temp.name}.gif"
                        strName = "${temp.name}.gif"
                        strPath = filePath

                        for (img in data) {
                            gifEncoder.addFrame(BitmapFactory.decodeFile(img).scale(1280, 720, false))
                            val nProgress = 100 * data.indexOf(img) / data.size
                            publishProgress(nProgress)
                        }
                        gifEncoder.finish()
                        try {
                            FileOutputStream(filePath).also {
                                it.write(bos.toByteArray())
                            }
                        } catch (e: IOException) {}
                        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                            this.data = Uri.fromFile(File(filePath))
                        }
                        context.sendBroadcast(intent)
                    }
                    returnValue = temp.type
                }
            }
            return returnValue
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            tempTextView.text = strCategory

            if (result == -1) {
                Toast.makeText(context, "Data is not exist", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.resources.getString(R.string.complete_making, strName), Toast.LENGTH_SHORT).show()
                /*
                val builder = AlertDialog.Builder(context)
                val resourceType = when (result) {
                    MOVIE -> "video/*"
                    GIF -> "image/gif"
                    else -> "*/*"
                }

                builder.setTitle(resources.getString(R.string.complete_making, strName))
                builder.setPositiveButton(
                    resources.getString(R.string.ok)
                ) { _, _ ->
                    run {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            setDataAndType(Uri.parse(strPath), resourceType)
                        }
                        startActivity(intent)
                    }
                }
                builder.setNegativeButton(
                    resources.getString(R.string.cancel)
                ) { _, _ -> run {} }

                builder.show()
                */
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            if (values[0] != null) tempTextView.text = "${values[0].toString()}%"
        }
    }
}