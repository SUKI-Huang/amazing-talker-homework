package com.amazing.mvvm.view.network

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleObserver
import com.amazing.base.BaseNetworkContentView
import com.amazing.homework.databinding.ViewNetworkContentBinding

class NetworkContentView : BaseNetworkContentView<ViewNetworkContentBinding> {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getLayoutWidth() = ViewGroup.LayoutParams.MATCH_PARENT
    override fun getLayoutHeight() = ViewGroup.LayoutParams.WRAP_CONTENT
    override fun getLifeCycleObserver(): LifecycleObserver? = null
    override fun getRetryButton(): View = viewBinding.tvBtnRetry
    override fun getProgressView(): View = viewBinding.llProgress
    override fun getRetryView(): View = viewBinding.llRetry
    override fun init() {}
    override fun initLayout() {}
    override fun initObserver() {}
}
