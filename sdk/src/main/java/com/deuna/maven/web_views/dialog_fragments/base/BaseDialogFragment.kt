package com.deuna.maven.web_views.dialog_fragments.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.DialogFragment.STYLE_NORMAL
import com.deuna.maven.web_views.base.BaseWebView

abstract class BaseDialogFragment(context: Context, private val fullSize: Boolean = true) :
    Dialog(context) {

    lateinit var baseWebView: BaseWebView

    abstract fun onBackButtonPressed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (fullSize) {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onBackPressed() {
        onBackButtonPressed()
    }

    override fun onStart() {
        super.onStart()
        if (fullSize) {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onStop() {
        baseWebView.destroy()
        super.onStop()
    }
}