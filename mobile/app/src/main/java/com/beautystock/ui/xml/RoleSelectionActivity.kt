package com.beautystock.ui.xml

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.beautystock.R
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.model.RoleUpdateRequest
import kotlinx.coroutines.launch

class RoleSelectionActivity : ComponentActivity() {

    private var selectedRole: String = "ROLE_ADULT"
    private lateinit var btnYouth: Button
    private lateinit var btnAdult: Button
    private lateinit var continueButton: Button
    private lateinit var errorText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)
        RetrofitClient.init(this)

        btnYouth = findViewById(R.id.btnYouthRole)
        btnAdult = findViewById(R.id.btnAdultRole)
        continueButton = findViewById(R.id.btnContinueRole)
        errorText = findViewById(R.id.tvRoleError)

        updateSelection(selectedRole)

        btnYouth.setOnClickListener {
            selectedRole = "ROLE_YOUTH"
            updateSelection(selectedRole)
        }

        btnAdult.setOnClickListener {
            selectedRole = "ROLE_ADULT"
            updateSelection(selectedRole)
        }

        continueButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    continueButton.isEnabled = false
                    errorText.text = ""

                    val response = RetrofitClient.api.updateRole(RoleUpdateRequest(selectedRole))
                    if (response.isSuccessful && response.body() != null) {
                        startActivity(Intent(this@RoleSelectionActivity, DashboardActivity::class.java))
                        finishAffinity()
                    } else {
                        val message = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                            ?: "Failed to set role"
                        errorText.text = message
                    }
                } catch (e: Exception) {
                    errorText.text = e.message ?: "Failed to set role"
                    Toast.makeText(this@RoleSelectionActivity, errorText.text, Toast.LENGTH_SHORT).show()
                } finally {
                    continueButton.isEnabled = true
                }
            }
        }
    }

    private fun updateSelection(role: String) {
        if (role == "ROLE_YOUTH") {
            btnYouth.setBackgroundResource(R.drawable.bg_btn_secondary)
            btnYouth.setTextColor(getColor(R.color.bs_pink))
            btnAdult.setBackgroundResource(R.drawable.bg_card_surface)
            btnAdult.setTextColor(getColor(R.color.bs_muted_dark))
        } else {
            btnYouth.setBackgroundResource(R.drawable.bg_card_surface)
            btnYouth.setTextColor(getColor(R.color.bs_muted_dark))
            btnAdult.setBackgroundResource(R.drawable.bg_btn_secondary)
            btnAdult.setTextColor(getColor(R.color.bs_pink))
        }
    }
}