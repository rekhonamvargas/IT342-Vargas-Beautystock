package com.beautystock.ui.xml

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.beautystock.R

class SuccessActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_NEXT_ACTIVITY_CLASS = "next_activity_class"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        val tvSuccessTitle = findViewById<TextView>(R.id.tvSuccessTitle)
        val tvSuccessMessage = findViewById<TextView>(R.id.tvSuccessMessage)
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        // Get intent extras
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Success!"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Your request was completed successfully."
        val nextActivityClassName = intent.getStringExtra(EXTRA_NEXT_ACTIVITY_CLASS)

        // Set text
        tvSuccessTitle.text = title
        tvSuccessMessage.text = message

        // Handle continue button
        btnContinue.setOnClickListener {
            if (nextActivityClassName != null) {
                try {
                    val nextActivityClass = Class.forName(nextActivityClassName)
                    val intent = Intent(this, nextActivityClass)
                    startActivity(intent)
                    finishAffinity()
                } catch (e: Exception) {
                    finish()
                }
            } else {
                finish()
            }
        }
    }
}
