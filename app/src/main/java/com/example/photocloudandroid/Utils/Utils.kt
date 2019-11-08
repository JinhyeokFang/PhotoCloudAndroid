package com.example.photocloudandroid.Utils

import android.content.Context

class Utils {
    companion object {
        fun getSavedToken (context: Context): String{
            val prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
            return prefs.getString("token", "") ?: ""
        }

        fun setSavedToken (context: Context, token: String) {
            val prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("token", token)
            editor.apply()
        }
    }
}