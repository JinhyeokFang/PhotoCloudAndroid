package com.example.photocloudandroid.Activity

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.photocloudandroid.WebClient.RetrofitClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.photocloudandroid.R
import com.example.photocloudandroid.Utils.Utils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1)
        }

        loginButton.setOnClickListener { run {
            RetrofitClient.INSTANCE.getRetrofitService().login(idtv.text.toString(),passwordtv.text.toString()).enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    when (response.code()) {
                        200 -> {
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            overridePendingTransition(R.anim.`in`, R.anim.out)
                            val bodyString: String = response.body()!!.string().toString()
                            val body: JSONObject = JSONObject(bodyString).getJSONObject("body")
                            Utils.setSavedToken(applicationContext, body.getString("token"))
                            startActivity(intent)
                            finish()
                        }
                        404 -> {
                            Toast.makeText(applicationContext, "입력하신 정보가 옳지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                        500 -> {
                            Toast.makeText(applicationContext, "서버 오류로 인해 로그인하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(applicationContext, "로그인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", t.message)
                }
            })
        } }
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
}
