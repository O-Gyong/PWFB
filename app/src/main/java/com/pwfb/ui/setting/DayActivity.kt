package com.pwfb.ui.setting

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pwfb.R
import com.pwfb.databinding.ActivityDayBinding
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.ui.MainActivity
import com.pwfb.util.formatDatePref
import com.pwfb.util.formatTimeDisplay
import com.pwfb.util.formatTimeStore
import com.pwfb.util.setupPwfbCalendar
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.util.Calendar


@AndroidEntryPoint
class DayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDayBinding
    private val viewModel: DayViewModel by viewModels()

    private var datePref = ""
    private var timePref = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cvCalendar.setupPwfbCalendar { date ->
            binding.btGo.isEnabled = true
            binding.btGo.setTextColor(getColor(R.color.white))
            datePref = formatDatePref(date)
        }
        getLocalTime()

        binding.btTime.setOnClickListener {
            setTimeSpinner()
        }

        binding.btGo.setOnClickListener {
            viewModel.setDDay("$datePref $timePref")
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

    private fun getLocalTime() {
        val localTime = LocalTime.now()

        binding.btTime.text = formatTimeDisplay(localTime.hour, localTime.minute)
        timePref = formatTimeStore(localTime.hour, localTime.minute)
    }

    private fun setTimeSpinner() {
        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, pHour, pMinute ->
            binding.btTime.text = formatTimeDisplay(pHour, pMinute)
            timePref = formatTimeStore(pHour, pMinute)
        }

        val timePickerDialog = TimePickerDialog(
            this,
            R.style.TimePickerTheme,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE, getString(R.string.confirm), timePickerDialog)
        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), timePickerDialog)
        timePickerDialog.show()
    }
}
