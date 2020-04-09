package com.example.ipscannerk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        //do update the SQLite OUI Database
        startActivity(Intent(this@SplashScreenActivity,MainActivity::class.java))
    }
}
