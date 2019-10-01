package com.loenzo.serialtest2

import android.annotation.SuppressLint
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
import java.io.File

@Suppress("CAST_NEVER_SUCCEEDS")
class CategoryFragment : Fragment() {

    companion object {
        private const val TAG = "CategoryFrag: "
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

        //imageView.setImageBitmap(getRecentFileFromCategoryName(argObject.title, context!!))
        Glide.with(context!!)
            .load(getRecentFilePathFromCategoryName(argObject.title, context!!))
            .thumbnail(0.1F)
            .into(imageView)

        /**
         * set button click listener
         */
        val btnAdd: ImageButton = view.findViewById(R.id.btnAdd)
        val btnList: ImageButton = view.findViewById(R.id.btnList)
        val btnCamera: ImageButton = view.findViewById(R.id.btnCamera)
        val btnVideo: ImageButton = view.findViewById(R.id.btnVideo)
        val btnOption: ImageButton = view.findViewById(R.id.btnOption)

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

        btnList.setOnClickListener {
            (context as MainActivity).openGallery(argObject)
        }

        btnCamera.setOnClickListener {
            (context as MainActivity).openCamera(argObject)
        }

        btnVideo.setOnClickListener {
            //(context as MainActivity).makeVideo(argObject)
            val builder = AlertDialog.Builder(context)

            val etName = EditText(context)
            etName.hint = resources.getString(R.string.movie_name)

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
                    //Handler(Looper.getMainLooper()).post {
                        AsyncMakeVideo().execute(
                            ParamVideo(etName.text.toString(), Integer.parseInt(etFps.text.toString()))
                        )
                    //}
                }
            } }
            builder.setNegativeButton(resources.getString(R.string.cancel)
            ) { _, _ -> run {} }
            builder.show()
        }

        btnOption.setOnClickListener {
            Toast.makeText(context, "OPTION BUTTON", Toast.LENGTH_SHORT).show()
        }
    }

    inner class ParamVideo (val name: String, val fps: Int)

    @SuppressLint("StaticFieldLeak")
    inner class AsyncMakeVideo : AsyncTask<ParamVideo, Int, Int>() {

        override fun doInBackground(vararg params: ParamVideo?): Int {
            var nProgress = 0
            if (params[0] != null) {
                val temp = params[0]!!
                val data = getRecentFilePathListFromCategoryName(argObject.title, context!!)
                val rootDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

                val filePath = rootDir.absolutePath + "/${temp.name}.mp4"
                NIOUtils.writableFileChannel(filePath).use {
                        fileChannel -> AndroidSequenceEncoder(fileChannel, Rational.R(temp.fps, 1)).let {
                            encoder ->  data.map {
                                nProgress = 100 * data.indexOf(it) / data.size
                                publishProgress(nProgress)
                                encoder.encodeImage(BitmapFactory.decodeFile(it).scale(1280, 720, false))
                            }
                            encoder.finish()
                            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                                this.data = Uri.fromFile(File(filePath))
                            }
                            context!!.sendBroadcast(intent)
                        }
                }
            }
            return nProgress
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            tempTextView.text = argObject.title
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        }

        @SuppressLint("SetTextI18n")
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            Log.i(TAG, "${values[0].toString()}%")
            if (values[0] != null) tempTextView.text = "${values[0].toString()}%"
        }
    }
}