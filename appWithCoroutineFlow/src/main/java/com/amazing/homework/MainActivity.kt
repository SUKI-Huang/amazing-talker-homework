package com.amazing.homework

import android.widget.Toast
import com.amazing.base.BaseActivity
import com.amazing.homework.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun initContentView() {
        super.initContentView()
        loadTeacher()
    }

    override fun initAction() {
        viewBinding.calendarView.setOnPeriodClickListener { isAvailable, startAt, endAt -> processClick(isAvailable, startAt, endAt) }
    }

    override fun initObserver() {}

    private fun processClick(isAvailable: Boolean, startAt: Date, endAt: Date) {
        // DateFormat just for display toast, please don't mind.
        val dateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        Toast.makeText(this, "  isAvailable:$isAvailable \n  startAt:${dateFormat.format(startAt)} \n  endAt:${dateFormat.format(endAt)}", Toast.LENGTH_SHORT).show()
    }

    private fun loadTeacher() {
        viewBinding.calendarView.setTeacher("amy-estrada")
    }
}
