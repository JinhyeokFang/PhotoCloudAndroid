package com.example.photocloudandroid.Activity

import android.R.attr.bitmap
import android.database.MergeCursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.photocloudandroid.R
import com.example.photocloudandroid.Utils.Utils
import com.example.photocloudandroid.WebClient.RetrofitClient
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_picture.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream


class PictureActivity : AppCompatActivity() {
    internal var removeImagesTask: RemoveImages = RemoveImages()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        Glide.with(applicationContext).load(intent.getStringExtra("url")).asBitmap().listener(object:
            RequestListener<String, Bitmap> {
            override fun onException(e: Exception?, model: String?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Bitmap?, model: String?, target: Target<Bitmap>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                val tempFile = File(filesDir, "temp.jpg")
                try {
                    tempFile.createNewFile()
                    val out = FileOutputStream(tempFile)
                    resource!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.close()
                    UCrop.of(Uri.parse("file://" + tempFile.path), Uri.parse(tempFile.absolutePath))
                        .withAspectRatio(16.0f, 9.0f)
                        .start(this@PictureActivity)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                return false
            }
        }).into(FullImageView)
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
