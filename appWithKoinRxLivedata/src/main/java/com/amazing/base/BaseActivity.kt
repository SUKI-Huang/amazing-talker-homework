package com.amazing.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
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

    private var compositeDisposable = CompositeDisposable()
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

    fun addDisposable(disposable: Disposable): Disposable {
        compositeDisposable.add(disposable)
        return disposable
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) compositeDisposable.dispose()
        super.onDestroy()
    }
}
