package com.amazing.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

interface IBaseActivity {
    fun initContentView()
    fun init()
    fun initLayout()
    fun initAction()
    fun initObserver()
}

open class BaseActivity<T : ViewBinding> : AppCompatActivity(), IBaseActivity {

    lateinit var viewBinding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView()
        init()
        initLayout()
        initAction()
        initObserver()
    }

    override fun initContentView() {
        fetchViewBinding()
    }

    private fun fetchViewBinding() {
        val clazz = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
        val method: Method = clazz.getMethod("inflate", LayoutInflater::class.java)
        viewBinding = method.invoke(null, layoutInflater) as T
        setContentView(viewBinding.root)
    }

    override fun init() {}

    override fun initLayout() {}

    override fun initAction() {}

    override fun initObserver() {}
}
