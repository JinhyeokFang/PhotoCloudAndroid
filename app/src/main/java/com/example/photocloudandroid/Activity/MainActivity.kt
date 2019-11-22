package com.example.photocloudandroid.Activity

import ImagesAdapter
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.MergeCursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photocloudandroid.Model.Image
import com.example.photocloudandroid.R
import com.example.photocloudandroid.Utils.Utils
import com.example.photocloudandroid.WebClient.RetrofitClient
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    internal var loadImageTask: LoadImages = LoadImages()
    var savedToken: String = ""
    val imageList: ArrayList<Image> = ArrayList<Image>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overridePendingTransition(R.anim.`in`, R.anim.out)

        savedToken = Utils.getSavedToken(this)
        loadImageTask = LoadImages()
        loadImageTask.execute()

        RetrofitClient.INSTANCE.getRetrofitService().getImage(savedToken).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val bodyString: String = response.body()!!.string().toString()
                val imageJSONArray: JSONArray= JSONObject(bodyString).getJSONObject("body").getJSONArray("photos")
                for (i in 0 until imageJSONArray.length()) {
                    val imageJSON = imageJSONArray.getJSONObject(i)
                    imageList.add(Image("http://spear-server.run.goorm.io/" + imageJSON.getString("url"), imageJSON.getString("date")))
                }
                val adapter = ImagesAdapter(this@MainActivity, imageList)
                gridView.layoutManager = GridLayoutManager(applicationContext,2)
                gridView.adapter = adapter
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })

        swipe.setOnRefreshListener {
            loadImageTask = LoadImages()
            loadImageTask.execute()

            RetrofitClient.INSTANCE.getRetrofitService().getImage(savedToken).enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val bodyString: String = response.body()!!.string().toString()
                    val imageJSONArray: JSONArray= JSONObject(bodyString).getJSONObject("body").getJSONArray("photos")
                    for (i in 0 until imageJSONArray.length()) {
                        val imageJSON = imageJSONArray.getJSONObject(i)
                        imageList.add(Image("http://spear-server.run.goorm.io/" + imageJSON.getString("url"), imageJSON.getString("date")))
                    }
                    val adapter = ImagesAdapter(this@MainActivity, imageList)
                    gridView.adapter = adapter
                    swipe.isRefreshing = false
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            })
        }

        fab.setOnClickListener {
            gridView.smoothScrollToPosition(0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImageTask.execute()
        } else {
            Toast.makeText(this@MainActivity, "파일 읽기/쓰기 권한을 허가해주십시오.", Toast.LENGTH_LONG).show()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri: Uri? = UCrop.getOutput(data!!)
            val file = File(resultUri!!.path)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val date = RequestBody.create(MediaType.parse("multipart/form-data"), Calendar.getInstance().time.toString())
            val token = RequestBody.create(MediaType.parse("multipart/form-data"), savedToken)

            RetrofitClient.INSTANCE.getRetrofitService().uploadImage(body, date, token).enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {}
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            })
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
        }
    }
}