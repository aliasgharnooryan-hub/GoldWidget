package com.example.goldwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class PriceWidget : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        thread { try { refresh(ctx, mgr, ids) } finally { pending.finish() } }
    }

    companion object {
        private val ITEMS = listOf(
            Triple(R.id.usd, "usd_sell", "💵 دلار"),
            Triple(R.id.eur, "eur_sell", "💶 یورو"),
            Triple(R.id.gold, "18ayar", "🥇 طلا ۱۸ (گرم)"),
            Triple(R.id.coin, "sekkeh", "🪙 سکه امامی")
        )

        fun refresh(ctx: Context, mgr: AppWidgetManager, widgetIds: IntArray) {
            val key = ctx.getSharedPreferences("p", 0).getString("key", "") ?: ""
            val views = RemoteViews(ctx.packageName, R.layout.widget)
            try {
                if (key.isBlank()) throw IllegalStateException("کلید API را در اپ وارد کنید")
                val c = URL("https://api.navasan.tech/latest/?api_key=$key").openConnection() as HttpURLConnection
                c.connectTimeout = 10000; c.readTimeout = 10000
                val json = JSONObject(c.inputStream.bufferedReader().readText())
                val nf = NumberFormat.getInstance(Locale("fa"))
                for ((viewId, k, label) in ITEMS) {
                    val v = json.optJSONObject(k)?.optString("value")?.toDoubleOrNull()
                    views.setTextViewText(viewId, "$label: " + (v?.let { nf.format(it.toLong()) + " تومان" } ?: "—"))
                }
                views.setTextViewText(R.id.time, "بروزرسانی " + SimpleDateFormat("HH:mm", Locale.US).format(Date()) + " • برای رفرش لمس کنید")
            } catch (e: Exception) {
                views.setTextViewText(R.id.time, "خطا: " + (e.message ?: "نامشخص"))
            }
            val i = Intent(ctx, PriceWidget::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            views.setOnClickPendingIntent(R.id.root,
                PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            for (id in widgetIds) mgr.updateAppWidget(id, views)
        }
    }
} 
