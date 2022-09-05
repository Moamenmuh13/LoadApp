package com.udacity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        titleNameFileResultTxtView.text = intent.extras!!.getString("title")
        val result = intent.extras!!.getBoolean("completeStatus")
        if (result) {
            statusResultTxtView.text = "Success"
            statusResultTxtView.setTextColor(Color.parseColor("#00FF00"))
        } else {
            statusResultTxtView.text = "Fail"
            statusResultTxtView.setTextColor(Color.parseColor("#FF0000"))
        }

        okBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
