package com.example.goldwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.*
import kotlin.concurrent.thread

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val p = getSharedPreferences("p", 0)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 96, 48, 48) }
        val info = TextView(this).apply {
            text = "۱) کلید API رایگان را از navasan.tech بگیرید و اینجا وارد کنید.\n۲) ذخیره بزنید.\n۳) روی صفحه اصلی گوشی نگه دارید ← Widgets ← «نرخ بازار»."
            textSize = 16f
        }
        val et = EditText(this).apply { hint = "API Key"; setText(p.getString("key", "")); setSingleLine() }
        val btn = Button(this).apply { text = "ذخیره و رفرش" }
        btn.setOnClickListener {
            p.edit().putString("key", et.text.toString().trim()).apply()
            val m = AppWidgetManager.getInstance(this)
            val ids = m.getAppWidgetIds(ComponentName(this, PriceWidget::class.java))
            thread { PriceWidget.refresh(applicationContext, m, ids) }
            Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show()
        }
        box.addView(info); box.addView(et); box.addView(btn)
        setContentView(box)
    }
}
