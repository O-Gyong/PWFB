package com.pwfb.presentation.setting

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pwfb.presentation.databinding.ActivityDayBinding
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.presentation.MainActivity
import com.pwfb.presentation.util.applySystemBarInsetsAsPadding
import com.pwfb.presentation.util.enablePwfbEdgeToEdge
import com.pwfb.presentation.util.formatDatePref
import com.pwfb.presentation.util.formatTimeDisplay
import com.pwfb.presentation.util.formatTimeStore
import com.pwfb.presentation.util.reattachPwfbPickers
import com.pwfb.presentation.util.showPwfbDatePicker
import com.pwfb.presentation.util.showPwfbTimePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime


@AndroidEntryPoint
class DayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDayBinding
    private val viewModel: DayViewModel by viewModels()

    private var date: LocalDate? = null
    private var timeHour = 0
    private var timeMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        enablePwfbEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityDayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.clName.applySystemBarInsetsAsPadding()

        // 시간은 현재 시각을 기본값으로
        val now = LocalTime.now()
        onTimePicked(now.hour, now.minute)
        supportFragmentManager.reattachPwfbPickers(::onDatePicked, ::onTimePicked)

        binding.btDate.setOnClickListener {
            supportFragmentManager.showPwfbDatePicker(date, ::onDatePicked)
        }

        binding.btTime.setOnClickListener {
            supportFragmentManager.showPwfbTimePicker(timeHour, timeMinute, ::onTimePicked)
        }

        binding.btGo.setOnClickListener {
            val picked = date ?: return@setOnClickListener
            viewModel.setDDay("${formatDatePref(picked)} ${formatTimeStore(timeHour, timeMinute)}")
        }

        viewModel.dDayObserve.observe(this) {
            if(it is PwfbResultEntity.Success) {
                viewModel.setFirstInit()
            }
        }

        viewModel.firstInitObserve.observe(this) {
            if(it is PwfbResultEntity.Success) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun onDatePicked(picked: LocalDate) {
        date = picked
        binding.btDate.text = formatDatePref(picked)
        binding.btGo.isEnabled = true
    }

    private fun onTimePicked(hour: Int, minute: Int) {
        timeHour = hour
        timeMinute = minute
        binding.btTime.text = formatTimeDisplay(hour, minute)
    }
}
