package com.example.fastpark.data

// Di dalam PaymentActivity.kt (VERSI YANG BENAR)

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.fastpark.R

class PaymentActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // 1. Ambil WebView dari layout
        val webView: WebView = findViewById(R.id.payment_webview)

        // 2. Ambil URL yang dikirim dari activity sebelumnya
        val paymentUrl = intent.getStringExtra("PAYMENT_URL")

        // 3. Aktifkan JavaScript
        webView.settings.javaScriptEnabled = true

        // 4. Muat URL ke WebView
        if (paymentUrl != null) {
            webView.loadUrl(paymentUrl)
        }
    }
}