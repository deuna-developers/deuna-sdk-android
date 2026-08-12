package com.deuna.maven.shared

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ProgressBar

internal class DeunaProgressDialog(private val context: Context) {

    private var dialog: AlertDialog? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun show() {
        mainHandler.post {
            val activity = context.findActivity() ?: return@post
            if (activity.isFinishing || activity.isDestroyed) return@post
            if (dialog?.isShowing == true) return@post

            val dp = activity.resources.displayMetrics.density
            val spinnerSize = (48 * dp).toInt()
            val padding = (20 * dp).toInt()
            val containerSize = spinnerSize + padding * 2

            val progressBar = ProgressBar(activity).apply { isIndeterminate = true }

            val circleBackground = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
            }

            val container = FrameLayout(activity).apply {
                background = circleBackground
                addView(progressBar, FrameLayout.LayoutParams(spinnerSize, spinnerSize).also {
                    it.setMargins(padding, padding, padding, padding)
                })
            }

            dialog = AlertDialog.Builder(activity)
                .setView(container)
                .setCancelable(false)
                .create()
                .also { d ->
                    d.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    d.show()
                    d.window?.setLayout(containerSize, containerSize)
                }
        }
    }

    fun dismiss() {
        mainHandler.post {
            runCatching { dialog?.dismiss() }
            dialog = null
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
