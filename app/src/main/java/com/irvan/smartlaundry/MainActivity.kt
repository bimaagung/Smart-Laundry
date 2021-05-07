package com.irvan.smartlaundry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonMap1.setOnClickListener {
            var intentMap = Intent(this, MapsLaundryActivity::class.java)
            startActivity(intentMap)

        }
        buttonMap2.setOnClickListener {
            var intentTentang = Intent(this, TentangActivity::class.java)
            startActivity(intentTentang)

        }
        buttonMap.setOnClickListener {
            var intentPetunjuk = Intent(this, PetunjukActivity::class.java)
            startActivity(intentPetunjuk)

        }
    }
}
