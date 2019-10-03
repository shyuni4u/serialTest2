package com.loenzo.serialtest2

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Rational
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Suppress("CAST_NEVER_SUCCEEDS")
class CategoryFragment : Fragment() {

    companion object {
        private const val TAG = "CategoryFrag: "
        private const val MOVIE = 1
        private const val GIF = 2
    }

    private lateinit var imageView: ImageView
    private lateinit var argObject: LastPicture
    private lateinit var tempTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.row_category, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "Call onViewCreated")

        val textView: TextView  = view.findViewById(R.id.categoryName)
        imageView = view.findViewById(R.id.lastImage)
        argObject = arguments!!.get("PARAM") as LastPicture
        textView.text = argObject.title
        tempTextView = textView

        Glide.with(context!!)
            .load(getRecentFilePathFromCategoryName(argObject.title, context!!))
            .thumbnail(0.1F)
            .into(imageView)

        /**
         * set button click listener
         */
        val btnNotification = view.findViewById<ImageButton>(R.id.btnNotification)
        val btnAdd = view.findViewById<ImageButton>(R.id.btnAdd)
        val btnRemove = view.findViewById<ImageButton>(R.id.btnRemove)
        val btnCamera = view.findViewById<ImageButton>(R.id.btnCamera)
        val btnVideo = view.findViewById<ImageButton>(R.id.btnVideo)
        val btnGif = view.findViewById<ImageButton>(R.id.btnGif)

        btnNotification.setOnClickListener {
            Toast.makeText(context, "Notification", Toast.LENGTH_SHORT).show()
        }

        btnAdd.setOnClickListener {
            val addCategoryName = EditText(context)
            val builder = AlertDialog.Builder(context)

            builder.setTitle(resources.getString(R.string.add_category_title))
            builder.setView(addCategoryName)
            builder.setPositiveButton(resources.getString(R.string.add)
            ) { _, _ -> run {
                (context as MainActivity).addCategoryFragment(addCategoryName.text.toString())
            } }
            builder.setNegativeButton(resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }

        btnRemove.setOnClickListener {
            val check = CheckBox(context)
            check.text = resources.getString(R.string.include_images)
            val builder = AlertDialog.Builder(context)

            builder.setTitle(resources.getString(R.string.delete_category, argObject.title))
            builder.setView(check)
            builder.setPositiveButton(resources.getString(R.string.delete)
            ) { _, _ -> run {
                (context as MainActivity).removeCategoryFragment(argObject.title, check.isChecked)
            } }
            builder.setNegativeButton(resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }

        btnCamera.setOnClickListener {
            (context as MainActivity).openCamera(argObject)
        }
        btnCamera.setOnLongClickListener {
            (context as MainActivity).openGallery(argObject)
            false
        }

        btnVideo.setOnClickListener {
            val builder = AlertDialog.Builder(context)

            val etName = EditText(context)
            etName.hint = resources.getString(R.string.movie_name)
            etName.text = etName.text.append(argObject.title)

            val etFps = EditText(context)
            etFps.hint = resources.getString(R.string.movie_time)
            etFps.inputType = InputType.TYPE_CLASS_NUMBER
            etFps.text = etFps.text.append("8")

            val lay = LinearLayout(context)
            lay.orientation = LinearLayout.VERTICAL
            lay.addView(etName)
            lay.addView(etFps)
            builder.setView(lay)

            builder.setTitle(resources.getString(R.string.add_movie_title))
            builder.setPositiveButton(resources.getString(R.string.apply)
            ) { _, _ -> run {
                if (etName.text.toString() != "" && etFps.text.toString() != "") {
                    AsyncMakeVideo().execute(
                        ParamVideo(etName.text.toString(), Integer.parseInt(etFps.text.toString()), MOVIE)
                    )
                }
            } }
            builder.setNegativeButton(resources.getString(R.string.cancel)
            ) { _, _ -> run {} }
            builder.show()
        }
        btnVideo.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath), "video/*")
            if (intent.resolveActivity(context!!.packageManager) != null) {
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        resources.getString(R.string.choose_app)
                    ), SELECT_MEDIA_SUCCESS
                )
            }
            false
        }

        btnGif.setOnClickListener {
            val builder = AlertDialog.Builder(context)

            val etName = EditText(context)
            etName.hint = resources.getString(R.string.movie_name)
            etName.text = etName.text.append(argObject.title)

            val etFps = EditText(context)
            etFps.hint = resources.getString(R.string.movie_time)
            etFps.inputType = InputType.TYPE_CLASS_NUMBER
            etFps.text = etFps.text.append("8")

            val lay = LinearLayout(context)
            lay.orientation = LinearLayout.VERTICAL
            lay.addView(etName)
            lay.addView(etFps)
            builder.setView(lay)

            builder.setTitle(resources.getString(R.string.add_movie_title))
            builder.setPositiveButton(resources.getString(R.string.apply)
            ) { _, _ -> run {
                if (etName.text.toString() != "" && etFps.text.toString() != "") {
                    AsyncMakeVideo().execute(
                        ParamVideo(etName.text.toString(), Integer.parseInt(etFps.text.toString()), GIF)
                    )
                }
            } }
            builder.setNegativeButton(resources.getString(R.string.cancel)
            ) { _, _ -> run {} }
            builder.show()
        }
        btnGif.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath), "image/*")
            if (intent.resolveActivity(context!!.packageManager) != null) {
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        resources.getString(R.string.choose_app)
                    ), SELECT_MEDIA_SUCCESS
                )
            }
            false
        }
    }

    inner class ParamVideo (val name: String, val fps: Int, val type: Int)

    @SuppressLint("StaticFieldLeak")
    inner class AsyncMakeVideo : AsyncTask<ParamVideo, Int, Int>() {

        private lateinit var strName: String
        private lateinit var strPath: String

        override fun doInBackground(vararg params: ParamVideo?): Int {
            var returnValue = -1
            if (params[0] != null) {
                val temp = params[0]!!
                val data = getRecentFilePathListFromCategoryName(argObject.title, context!!)

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
                                context!!.sendBroadcast(intent)
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
                        context!!.sendBroadcast(intent)
                    }
                    returnValue = temp.type
                }
            }
            return returnValue
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            tempTextView.text = argObject.title

            if (result == -1) {
                Toast.makeText(context!!, "Data is not exist", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context!!, resources.getString(R.string.complete_making, strName), Toast.LENGTH_SHORT).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == SELECT_MEDIA_SUCCESS && data != null) {
            //val selectedVideo = getRealPathFromURI(data.data!!, context!!)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                this.data = data.data
                this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (intent.resolveActivity(context!!.packageManager) != null) {
                startActivity(intent)
            }
        }
    }
}