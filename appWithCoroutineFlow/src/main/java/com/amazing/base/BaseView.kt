package com.amazing.base

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.amazing.extensions.activity
import com.amazing.extensions.className
import com.amazing.module.event.FlowBus
import com.amazing.module.event.FlowBusListener
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

interface IBaseView {
    fun getLayoutWidth(): Int
    fun getLayoutHeight(): Int
    fun getLifeCycleObserver(): LifecycleObserver?
    fun onViewCreated(view: View)
    fun init()
    fun initLayout()
    fun initAction()
    fun initObserver()
}

abstract class BaseView<T : ViewBinding> : LinearLayout, IBaseView, FlowBusListener {
    private var lifecycleObserver: LifecycleObserver? = null
    private var attributeSet: AttributeSet? = null
    private var defStyleAttr: Int = 0
    lateinit var viewBinding: T

    private lateinit var contentView: View

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this@BaseView.attributeSet = attrs
        this@BaseView.defStyleAttr = defStyleAttr
        this@BaseView.initConstructor()
    }

    private fun initConstructor() {
        viewBinding = fetchViewBinding()
        contentView = viewBinding.root
        gravity = Gravity.CENTER
        addView(contentView, LayoutParams(getLayoutWidth(), getLayoutHeight()))
        onViewCreated(contentView)
        if (activity != null) bindLifeCycle(activity!!)
    }

    private fun fetchViewBinding(): T {
        val clazz = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
        val method: Method = clazz.getMethod("inflate", LayoutInflater::class.java)
        return method.invoke(null, LayoutInflater.from(context)) as T
    }

    private fun bindLifeCycle(lifecycleOwner: LifecycleOwner) {
        if (lifecycleObserver != null) {
            Log.e("baseView", "already bind LifecycleObserver")
            return
        }
        lifecycleObserver = getLifeCycleObserver()
        if (lifecycleObserver != null) lifecycleOwner.lifecycle.addObserver(lifecycleObserver!!)
    }

    override fun onViewCreated(view: View) {
        init()
        initLayout()
        initAction()
        initObserver()
        initEventBus()
    }

    private fun initEventBus() {
        if (activity == null) {
            Log.e(className, "can not listen to FlowBus in $className, cause activity or LifecycleOwner not founded")
            return
        }
        FlowBus.initListener(this, activity!!)
    }

    fun getStyledAttributes(styleable: IntArray): TypedArray? {
        if (attributeSet == null) return null
        return context.theme.obtainStyledAttributes(attributeSet, styleable, defStyleAttr, 0)
    }

    override fun onNetworkAvailableChanged(isAvailable: Boolean) {}
    override fun onTimezoneChanged() {}
}
