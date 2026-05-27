package com.example.f4diwrotatorapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.f4diwrotatorapp.ui.control.ControlFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ControlFragment())
                .commit()
        }
    }
}