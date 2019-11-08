package com.example.photocloudandroid.Activity

import ImagesAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.photocloudandroid.R

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.MergeCursor
import android.os.AsyncTask
import android.os.Build
import android.provider.MediaStore
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.photocloudandroid.Model.Image
import com.example.photocloudandroid.Utils.Utils
import com.example.photocloudandroid.WebClient.RetrofitClient

import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Response
import java.io.File

import java.util.ArrayList
import okhttp3.*
import okhttp3.MultipartBody
import retrofit2.Callback
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    var imageList = ArrayList<Image>()
    internal var loadImageTask: LoadImages = LoadImages()
    var savedToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overridePendingTransition(R.anim.`in`, R.anim.out)

        savedToken = Utils.getSavedToken(this)

        loadImageTask = LoadImages()
        loadImageTask.execute()

        val textView: TextView = TextView(this)

        val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1)
        }

        RetrofitClient.INSTANCE.getRetrofitService().getImage(savedToken).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val bodyString: String = response.body()!!.string().toString()
                val imageJSONArray: JSONArray= JSONObject(bodyString).getJSONObject("body").getJSONArray("photos")
                val imageList: ArrayList<Image> = ArrayList<Image>()

                for (i in 0 until imageJSONArray.length()) {
                    val imageJSON = imageJSONArray.getJSONObject(i)
                    imageList.add(Image("http://spear-server.run.goorm.io/" + imageJSON.getString("url"), imageJSON.getString("date")))
                }

                val adapter = ImagesAdapter(this@MainActivity, imageList)
                gridView.adapter = adapter
//            gridView.setOnItemClickListener { parent, view, position, id ->
//                Toast.makeText(applicationContext, imageList.get(position).url, Toast.LENGTH_SHORT).show()
//            }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })

        registerForContextMenu(gridView)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImageTask.execute()
        } else {
            Toast.makeText(this@MainActivity, "파일 읽기/쓰기 권한을 허가해주십시오.", Toast.LENGTH_LONG).show()
        }
    }

    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) !== PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    internal inner class LoadImages : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            imageList.clear()
        }
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

                imageList.add(Image(path, timestamp))

                val file = File(path)
                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val date = RequestBody.create(MediaType.parse("multipart/form-data"), timestamp);
                val token = RequestBody.create(MediaType.parse("multipart/form-data"), savedToken);

                RetrofitClient.INSTANCE.getRetrofitService().uploadImage(body, date, token).enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {}
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                })
            }
            cursor.close()

            return xml
        }
        override fun onPostExecute(xml: String) {}
    }
}