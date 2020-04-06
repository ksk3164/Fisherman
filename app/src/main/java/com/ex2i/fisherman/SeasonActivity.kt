package com.ex2i.fisherman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_season.*

class SeasonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_season)

        spring_layout.setOnClickListener {
            val intent = Intent(this, SpringActivity::class.java)
            startActivity(intent)
        }

        summer_layout.setOnClickListener {
            val intent = Intent(this, SummerActivity::class.java)
            startActivity(intent)
        }

        autumn_lyaout.setOnClickListener {
            val intent = Intent(this, AutumnActivity::class.java)
            startActivity(intent)
        }

        winter_layout.setOnClickListener {
            val intent = Intent(this, WinterActivity::class.java)
            startActivity(intent)
        }


    }
}
