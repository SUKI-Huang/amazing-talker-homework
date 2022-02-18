package com.amazing.mvvm.view.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amazing.base.BaseView
import com.amazing.extensions.*
import com.amazing.homework.R
import com.amazing.homework.databinding.ViewCalendarBinding
import com.amazing.homework.databinding.ViewPeriodBinding
import com.amazing.module.network.component.RequestState
import com.amazing.mvvm.model.data.calendar.*
import com.amazing.mvvm.viewModel.calendar.CalendarViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CalendarView : BaseView<ViewCalendarBinding> {

    private lateinit var viewModel: CalendarViewModel
    private lateinit var clickShareFlow: MutableSharedFlow<() -> Unit>
    private var onPeriodClickListener: ((isAvailable: Boolean, startAt: Date, endAt: Date) -> Unit)? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getLayoutWidth(): Int = LayoutParams.MATCH_PARENT
    override fun getLayoutHeight(): Int = LayoutParams.WRAP_CONTENT
    override fun getLifeCycleObserver(): LifecycleObserver? = null

    override fun init() {
        viewModel = ViewModelProvider.NewInstanceFactory().create(CalendarViewModel::class.java)
        clickShareFlow = MutableSharedFlow()
    }

    override fun initLayout() {
        processStart()
        initDayTitle()
        initTimezone()
    }

    override fun initAction() {
        // prevent user operate component when network loading or error
        viewBinding.networkContentView.setOnClickListener { /* mask */ }

        viewBinding.networkContentView.addOnRetryClickedListener { viewModel.retry() }

        // prevent user click two or more period at same time (if needed)
        clickShareFlow.filterNotNull().throttleFirst(500).onEach { it.invoke() }.launchIn(activity!!.lifecycleScope)

        viewBinding.tvGoPrevious.onClick(activity!!.lifecycleScope) { viewModel.goPreviousWeek() }
        viewBinding.tvGoNext.onClick(activity!!.lifecycleScope) { viewModel.goNextWeek() }
    }

    override fun initObserver() {
        viewModel.teacherScheduleRequestState.filterNotNull().onEach {
            when (it) {
                is RequestState.OnStart -> processStart()
                is RequestState.OnSuccess -> processSuccess(it.data)
                is RequestState.OnError -> processError(it.throwable)
            }
        }.launchIn(activity!!.lifecycleScope)

        viewModel.currentCalendarStartAtState.filterNotNull().onEach { updateCalendarDateRange(it) }.launchIn(activity!!.lifecycleScope)

        viewModel.isPreviousWeekAvailableState.onEach {
            // change go previous button style when previous week available changed
            viewBinding.tvGoPrevious.isClickable = it
            viewBinding.tvGoPrevious.alpha = if (it) 1F else 0.5F
        }.launchIn(activity!!.lifecycleScope)
    }

    private fun initDayTitle() {
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val calendar = Calendar.getInstance()
        viewBinding.tvSundayText.text = dateFormat.format(calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) }.time)
        viewBinding.tvMondayText.text = dateFormat.format(calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONTH) }.time)
        viewBinding.tvTuesdayText.text = dateFormat.format(calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY) }.time)
        viewBinding.tvWednesdayText.text = dateFormat.format(calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY) }.time)
        viewBinding.tvThursdayText.text = dateFormat.format(calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY) }.time)
        viewBinding.tvFridayText.text = dateFormat.format(calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY) }.time)
        viewBinding.tvSaturdayText.text = dateFormat.format(calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) }.time)
    }

    private fun initTimezone() {
        viewBinding.tvTimezone.text = context.getString(R.string.view_calendar_timezone_info, TimeZone.getDefault().displayInformation)
    }

    @SuppressLint("SetTextI18n")
    private fun updateCalendarDateRange(startAt: Date) {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
        val endAt = startAt.plus(6, TimeUnit.DAYS)
        val startAtText = dateFormat.format(startAt)
        val endAtText = dateFormat.format(endAt)
        viewBinding.tvDateRange.text = "$startAtText - $endAtText"
        viewBinding.tvSundayNumber.text = Calendar.getInstance().apply { time = startAt }.get(Calendar.DAY_OF_MONTH).toString()
        viewBinding.tvMondayNumber.text = Calendar.getInstance().apply { time = startAt.plus(1, TimeUnit.DAYS) }.get(Calendar.DAY_OF_MONTH).toString()
        viewBinding.tvTuesdayNumber.text = Calendar.getInstance().apply { time = startAt.plus(2, TimeUnit.DAYS) }.get(Calendar.DAY_OF_MONTH).toString()
        viewBinding.tvWednesdayNumber.text = Calendar.getInstance().apply { time = startAt.plus(3, TimeUnit.DAYS) }.get(Calendar.DAY_OF_MONTH).toString()
        viewBinding.tvThursdayNumber.text = Calendar.getInstance().apply { time = startAt.plus(4, TimeUnit.DAYS) }.get(Calendar.DAY_OF_MONTH).toString()
        viewBinding.tvFridayNumber.text = Calendar.getInstance().apply { time = startAt.plus(5, TimeUnit.DAYS) }.get(Calendar.DAY_OF_MONTH).toString()
        viewBinding.tvSaturdayNumber.text = Calendar.getInstance().apply { time = endAt }.get(Calendar.DAY_OF_MONTH).toString()
    }

    private fun processStart() {
        viewBinding.networkContentView.showProgress()
        viewBinding.llContent.alpha = 0.2F
    }

    private fun processSuccess(calendarData: CalendarData) {
        viewBinding.networkContentView.showContent()
        viewBinding.llContent.alpha = 1F

        // clear period views on container
        // (use the old view on container is better, but I don't really have to much time on this homework； Life is hard you know )
        viewBinding.llSundayPeriodContainer.removeAllViews()
        viewBinding.llMondayPeriodContainer.removeAllViews()
        viewBinding.llTuesdayPeriodContainer.removeAllViews()
        viewBinding.llWednesdayPeriodContainer.removeAllViews()
        viewBinding.llThursdayPeriodContainer.removeAllViews()
        viewBinding.llFridayPeriodContainer.removeAllViews()
        viewBinding.llSaturdayPeriodContainer.removeAllViews()

        calendarData.sunday.onEach { viewBinding.llSundayPeriodContainer.addView(createPeriodView(it), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
        calendarData.monday.onEach { viewBinding.llMondayPeriodContainer.addView(createPeriodView(it), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
        calendarData.tuesday.onEach { viewBinding.llTuesdayPeriodContainer.addView(createPeriodView(it), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
        calendarData.wednesday.onEach { viewBinding.llWednesdayPeriodContainer.addView(createPeriodView(it), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
        calendarData.thursday.onEach { viewBinding.llThursdayPeriodContainer.addView(createPeriodView(it), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
        calendarData.friday.onEach { viewBinding.llFridayPeriodContainer.addView(createPeriodView(it), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
        calendarData.saturday.onEach { viewBinding.llSaturdayPeriodContainer.addView(createPeriodView(it), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }

        // if startAt from server response is passed, do not allow user access earlier time
        // for the safety, protect it again on ui layer
        val currentTime = viewModel.getCurrentDateTime()
        val isSundayAvailable = calendarData.isSundayAvailable(currentTime)
        val isMondayAvailable = calendarData.isMondayAvailable(currentTime)
        val isTuesdayAvailable = calendarData.isTuesdayAvailable(currentTime)
        val isWednesdayAvailable = calendarData.isWednesdayAvailable(currentTime)
        val isThursdayAvailable = calendarData.isThursdayAvailable(currentTime)
        val isFridayAvailable = calendarData.isFridayAvailable(currentTime)
        val isSaturdayAvailable = calendarData.isSaturdayAvailable(currentTime)

        // update bar (can be optimized, If needed)
        viewBinding.vSundayBar.background = ContextCompat.getDrawable(context, if (isSundayAvailable) R.drawable.background_calendar_day_bar_available else R.drawable.background_calendar_day_bar_unavailable)
        viewBinding.vMondayBar.background = ContextCompat.getDrawable(context, if (isMondayAvailable) R.drawable.background_calendar_day_bar_available else R.drawable.background_calendar_day_bar_unavailable)
        viewBinding.vTuesdayBar.background = ContextCompat.getDrawable(context, if (isTuesdayAvailable) R.drawable.background_calendar_day_bar_available else R.drawable.background_calendar_day_bar_unavailable)
        viewBinding.vWednesdayBar.background = ContextCompat.getDrawable(context, if (isWednesdayAvailable) R.drawable.background_calendar_day_bar_available else R.drawable.background_calendar_day_bar_unavailable)
        viewBinding.vThursdayBar.background = ContextCompat.getDrawable(context, if (isThursdayAvailable) R.drawable.background_calendar_day_bar_available else R.drawable.background_calendar_day_bar_unavailable)
        viewBinding.vFridayBar.background = ContextCompat.getDrawable(context, if (isFridayAvailable) R.drawable.background_calendar_day_bar_available else R.drawable.background_calendar_day_bar_unavailable)
        viewBinding.vSaturdayBar.background = ContextCompat.getDrawable(context, if (isSaturdayAvailable) R.drawable.background_calendar_day_bar_available else R.drawable.background_calendar_day_bar_unavailable)

        // update day text (can be optimized, If needed)
        viewBinding.tvSundayText.setTextColor(ContextCompat.getColor(context, if (isSundayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvMondayText.setTextColor(ContextCompat.getColor(context, if (isMondayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvTuesdayText.setTextColor(ContextCompat.getColor(context, if (isTuesdayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvWednesdayText.setTextColor(ContextCompat.getColor(context, if (isWednesdayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvThursdayText.setTextColor(ContextCompat.getColor(context, if (isThursdayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvFridayText.setTextColor(ContextCompat.getColor(context, if (isFridayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvSaturdayText.setTextColor(ContextCompat.getColor(context, if (isSaturdayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))

        // update day number (can be optimized, If needed)
        viewBinding.tvSundayNumber.setTextColor(ContextCompat.getColor(context, if (isSundayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvMondayNumber.setTextColor(ContextCompat.getColor(context, if (isMondayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvTuesdayNumber.setTextColor(ContextCompat.getColor(context, if (isTuesdayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvWednesdayNumber.setTextColor(ContextCompat.getColor(context, if (isWednesdayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvThursdayNumber.setTextColor(ContextCompat.getColor(context, if (isThursdayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvFridayNumber.setTextColor(ContextCompat.getColor(context, if (isFridayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
        viewBinding.tvSaturdayNumber.setTextColor(ContextCompat.getColor(context, if (isSaturdayAvailable) R.color.calendar_view_day_text_available else R.color.calendar_view_day_text_unavailable))
    }

    private fun processError(throwable: Throwable) {
        viewBinding.llContent.alpha = 0.2F
        viewBinding.networkContentView.showRetry(throwable.message)
    }

    private fun createPeriodView(period: CalendarData.Period): View {
        // if startAt from server response is passed, do not allow user access earlier time
        // for the safety, protect it again on ui layer
        val currentTime = viewModel.getCurrentDateTime()
        val isAvailable = period.isDisplayAvailable(currentTime)
        val binding = ViewPeriodBinding.inflate(LayoutInflater.from(context))
        binding.tvTime.setTextColor(ContextCompat.getColor(context, if (isAvailable) R.color.calendar_view_period_text_available else R.color.calendar_view_period_text_unavailable))
        binding.tvTime.text = period.displayTime
        if (isAvailable) binding.tvTime.setTypeface(null, Typeface.BOLD)
        binding.tvTime.setOnClickListener {
            activity?.lifecycleScope?.launch {
                clickShareFlow.emit { onPeriodClickListener?.invoke(isAvailable, period.startAt, period.endAt) }
            }
        }
        return binding.root
    }

    // set teacher from outside (if needed)
    fun setTeacher(teacherName: String) = viewModel.setTeacher(teacherName)

    fun setOnPeriodClickListener(listener: (isAvailable: Boolean, startAt: Date, endAt: Date) -> Unit) {
        onPeriodClickListener = listener
    }

    override fun onTimezoneChanged() {
        // when user get into system setting and change the timezone, onTimezoneChanged triggered
        // when user go aboard, onTimezoneChanged triggered
        if (activity!!.isDestroyed) return
        // update the timezone information
        initTimezone()
        // request latest date by new timezone
        viewModel.retry()
    }

    override fun onNetworkAvailableChanged(isAvailable: Boolean) {
        // when network is available and current data request is error then retry automatically.
        // get better user experience.
        if (isAvailable && viewBinding.networkContentView.isOnRetry) viewModel.retry()
    }
}
