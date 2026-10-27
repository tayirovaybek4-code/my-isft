package com.example.myapplication   // o'z paket nomingizga almashtiring

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView: TextView = findViewById(R.id.textView)
        val editText1: EditText = findViewById(R.id.editText1)   // login
        val editText2: EditText = findViewById(R.id.editText2)   // parol
        val button: Button = findViewById(R.id.button)           // tasdiqlash

        button.setOnClickListener {
            val login = editText1.text.toString().trim()
            val parol = editText2.text.toString().trim()

            when {
                login.isEmpty() || parol.isEmpty() -> {
                    Toast.makeText(this, "Login va parolni kiriting", Toast.LENGTH_SHORT).show()
                }
                login == "admin" && parol == "1234" -> {
                    textView.text = "Xush kelibsiz, $login!"
                }
                else -> {
                    textView.text = "Login yoki parol noto'g'ri"
                }
            }
        }
    }
}