package com.pratiksahu.dokify

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
//        setContentView(R.layout.welcome_splash_screen)
//        val handler = Handler()
//        handler.postDelayed({
//        }, 3000)
    }

}