package com.amazing.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewbinding.ViewBinding
import com.amazing.extensions.setVisibility

interface IBaseNetworkContentView {
    fun getRetryButton(): View?
    fun getProgressView(): View?
    fun getRetryView(): View?

    fun showContent()
    fun showProgress(progressMessage: String? = null)
    fun showRetry(errorMessage: String? = null)
}

abstract class BaseNetworkContentView<T : ViewBinding> : BaseView<T>, IBaseNetworkContentView {

    enum class State {
        None, OnContent, OnProgress, OnRetry
    }

    var state: State = State.None
        private set
    private var contentViews: ArrayList<View>? = null
    private var onRetryClickedListeners: ArrayList<() -> Unit>? = null

    val isOnProgress: Boolean
        get() = state == State.OnProgress
    val isOnRetry: Boolean
        get() = state == State.OnRetry
    val isOnContent: Boolean
        get() = state == State.OnContent

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun initAction() {
        getRetryButton()?.setOnClickListener { onRetryClickedListeners?.forEach { it.invoke() } }
    }

    fun addContentView(view: View) {
        if (contentViews == null) contentViews = arrayListOf()
        val isAdded = contentViews?.any { it.hashCode() == view.hashCode() } ?: false
        if (!isAdded) contentViews?.add(view)
    }

    fun addOnRetryClickedListener(listener: () -> Unit) {
        if (onRetryClickedListeners == null) onRetryClickedListeners = arrayListOf()
        val existListener = onRetryClickedListeners?.any { it.hashCode() == listener.hashCode() } ?: false
        if (!existListener) onRetryClickedListeners!!.add(listener)
    }

    override fun showContent() {
        state = State.OnContent
        contentViews?.forEach { it.setVisibility(true) }
        setVisibility(false)
        getProgressView()?.setVisibility(false)
        getRetryView()?.setVisibility(false)
    }

    override fun showProgress(progressMessage: String?) {
        state = State.OnProgress
        contentViews?.forEach { it.setVisibility(false) }
        setVisibility(true)
        getProgressView()?.setVisibility(true)
        getRetryView()?.setVisibility(false)
    }

    override fun showRetry(errorMessage: String?) {
        state = State.OnRetry
        contentViews?.forEach { it.setVisibility(false) }
        setVisibility(true)
        getProgressView()?.setVisibility(false)
        getRetryView()?.setVisibility(true)
    }
}
