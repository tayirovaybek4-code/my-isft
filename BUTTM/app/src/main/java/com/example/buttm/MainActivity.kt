package com.example.myapptemperature



import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtLogin = findViewById<TextView>(R.id.edtLogin)
        val edtPassword = findViewById<TextView>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {

            val login = edtLogin.text.toString().trim()
            val password = edtPassword.text.toString()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Login va parolni kiriting!",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (login == "admin" &&
                password == "12345") {

                Toast.makeText(
                    this,
                    "Muvaffaqiyatli kirdingiz!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(
                    this,
                    "Login yoki parol noto'g'ri!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}