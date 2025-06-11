package com.example.fastpark.data

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.fastpark.R
import com.example.fastpark.viewmodel.AuthViewModel

// Di dalam Activity atau Fragment Anda (misal: TopUpActivity.kt)

class TopUpActivity : AppCompatActivity() {

    // Dapatkan instance dari AuthViewModel
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_up)

        val topUpButton: Button = findViewById(R.id.top_up_button)
        val amountEditText: EditText = findViewById(R.id.amount_edit_text)


        // 1. Panggil fungsi di ViewModel saat tombol diklik
        topUpButton.setOnClickListener {
            val amount = amountEditText.text.toString().toIntOrNull()
            if (amount != null && amount > 0) {
                authViewModel.requestTopUpPayment(amount, "M2")

            }else {
                Toast.makeText(this, "Masukkan jumlah yang valid", Toast.LENGTH_SHORT).show()

            }

        }

        // 2. Amati/Observe LiveData dari ViewModel
        authViewModel.paymentUrl.observe(this, Observer { url ->
            // Blok ini akan berjalan setiap kali nilai LiveData berubah
            if (url != null) {
                // 3. Buat Intent dan mulai Activity DI SINI
                val intent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("PAYMENT_URL", url)
                }
                startActivity(intent)

                // 4. Reset LiveData agar tidak memicu navigasi lagi
                authViewModel.onNavigationDone()
            }
        })
    }
}