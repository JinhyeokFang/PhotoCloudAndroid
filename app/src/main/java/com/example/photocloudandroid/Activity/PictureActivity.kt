package com.example.photocloudandroid.Activity

import android.content.Context
import android.content.ContextWrapper
import android.database.MergeCursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_picture.*
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.photocloudandroid.R
import com.example.photocloudandroid.Utils.Utils
import com.example.photocloudandroid.WebClient.RetrofitClient
import ja.burhanrashid52.photoeditor.PhotoEditor
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception

class PictureActivity : AppCompatActivity() {
    internal var removeImagesTask: RemoveImages = RemoveImages()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        val FullImageVIew: PhotoEditor = PhotoEditor.Builder(this, FullImageView)
         .setPinchTextScalable(true)
         .build()

        Glide.with(applicationContext).load(intent.getStringExtra("url")).asBitmap().listener(object:
            RequestListener<String, Bitmap> {
            override fun onException(e: Exception?, model: String?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: String?, target: Target<Bitmap>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                Log.d("asdf", resource.toString())
                return false
            }
        })
        FullImageView.source.setImageResource(R.mipmap.icon)
        Log.d("asdf", "asdf")
        toolbar.setTitle("사진 편집")
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.imagemenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.delete) {
            RetrofitClient.INSTANCE.getRetrofitService().removeImage(Utils.getSavedToken(this), intent.getStringExtra("date")).enqueue(object: retrofit2.Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    removeImagesTask.execute()
                    finish()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(applicationContext, "이미지 삭제를 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return if (id == R.id.delete) {
            true
        } else super.onOptionsItemSelected(item)
    }

    internal inner class RemoveImages : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg args: String): String {
            val xml = ""

            var path: String?
            var timestamp: String?
            val uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED
            )

            val cursorExternal = contentResolver.query(uriExternal, projection, "", null, null)
            val cursorInternal = contentResolver.query(uriInternal, projection, "", null, null)
            val cursor = MergeCursor(arrayOf(cursorExternal, cursorInternal))
            while (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)) ?: "0"

                val file = File(path)
                if (timestamp.toString() == intent.getStringExtra("date")){
                    file.delete()
                    return ""
                }
            }
            cursor.close()

            return xml
        }
    }
}
