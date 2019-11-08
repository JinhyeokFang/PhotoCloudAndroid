package com.example.photocloudandroid.Activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.photocloudandroid.R
import com.example.photocloudandroid.Utils.Utils
import com.example.photocloudandroid.WebClient.RetrofitClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val handler = Handler()

        val savedToken: String = Utils.getSavedToken(this)

        handler.postDelayed({
            RetrofitClient.INSTANCE.getRetrofitService().checktoken(savedToken).enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    when (response.code()) {
                        200 -> {
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            val bodyString: String = response.body()!!.string().toString()
                            val body: JSONObject = JSONObject(bodyString).getJSONObject("body")
                            val token: String = body.getString("token")

                            val prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                            val editor = prefs.edit()
                            editor.putString("token", token)
                            editor.apply()

                            startActivity(intent)
                            finish()
                        }
                        401 -> {
                            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }, 1000)
    }
}
