package com.pwfb.ui.profile

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.pwfb.R
import com.pwfb.base.BaseFragment
import com.pwfb.common.DataStoreResult
import com.pwfb.common.Firebase
import com.pwfb.databinding.FragmentProfileBinding
import com.pwfb.ui.MainViewModel
import com.pwfb.util.ClearDecorator
import com.pwfb.util.DayDisableDecorator
import com.pwfb.util.SelectDecorator
import com.pwfb.util.TodayDecorator
import com.pwfb.util.calcDDay
import com.pwfb.util.formatTimeDisplay
import com.pwfb.util.formatTimeStore
import com.pwfb.util.getCurrentToday
import com.pwfb.util.getWeekKor
import com.pwfb.util.parseStoredTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.util.Calendar


@AndroidEntryPoint
@SuppressLint("SimpleDateFormat", "SetTextI18n", "InflateParams")
class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: MainViewModel by viewModels()

    private var dDayPref = ""
    private var datePref = ""
    private var timePref = ""
    private var weightPref = 0.0
    private var weight = 0.0

    private var infoDialogView: View? = null
    private lateinit var infoDialog: BottomSheetDialog
    private val strategyDialog = StrategyDialogFragment()

    private var file: File? = null
    private var filePath: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        setOnClickListener()
        setCalendarView()
        viewModelObserve()

        init()
        return binding.root
    }

    private fun init() {
        viewModel.getName()
        viewModel.getWeight()
        viewModel.getDDay()

        infoDialogView = layoutInflater.inflate(R.layout.dialog_info, null)
        infoDialog = BottomSheetDialog(requireContext())
        infoDialog.setContentView(infoDialogView!!)
    }

    private fun setOnClickListener() {
        binding.btModify.setOnClickListener {
            viewModel.setDDay("$dDayPref $timePref")
            viewModel.setWeight(weight.toString())
        }

        binding.btTime.setOnClickListener {
            setTimeSpinner()
        }

        binding.ivMinus.setOnClickListener {
            weight = (weight*10 - 1).toInt() / 10.0
            binding.tvWeight.text = weight.toString()+"Kg"
            setWeightBtn()
        }

        binding.ivPlus.setOnClickListener {
            weight = (weight*10 + 1).toInt() / 10.0
            binding.tvWeight.text = weight.toString()+"Kg"
            setWeightBtn()
        }

        binding.clPwfbInformation.setOnClickListener {
            infoDialog.show()
        }

        binding.clTheDayStrategy.setOnClickListener {
            strategyDialog.show(activity?.supportFragmentManager!!, strategyDialog.tag)
        }

        binding.clLogSend.setOnClickListener {
            viewModel.setUploadFile()
        }
    }

    private fun viewModelObserve() {
        viewModel.logFile.observe(viewLifecycleOwner) {
            if(it == null) return@observe

            try {
                file = it
                filePath = "logs/${getCurrentToday()}/${file?.name}"

                Firebase.firebaseReference
                    .child(filePath)
                    .putFile(Uri.fromFile(file))
                    .addOnProgressListener {
                        Toast.makeText(requireContext(), "로그 전송 중...", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        Toast.makeText(requireContext(), "로그 전송 완료", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "로그 전송 실패", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                // 전송할 파일이 없는 경우
                if (e.cause == null && e.message == "file") {
                    Toast.makeText(requireContext(), "전송할 로그가 없음.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "로그 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.nameObserve.collectLatest {
                        if(it == DataStoreResult.SET_NAME) {
                            // todo
                            return@collectLatest
                        }
                        binding.tvName.text = it
                    }
                }

                launch {
                    viewModel.weightObserve.collectLatest {
                        if(it == DataStoreResult.SET_WEIGHT) {
                            binding.btModify.isEnabled = false
                            binding.btModify.setTextColor(requireContext().getColor(R.color.c_949292))
                            viewModel.getWeight()
                            return@collectLatest
                        }

                        // 초기값("")이나 미설정 값("WeightNull")은 숫자가 아니므로 무시
                        val savedWeight = it.toDoubleOrNull() ?: return@collectLatest
                        weightPref = savedWeight
                        weight = savedWeight

                        binding.tvIdealWeight.text = getString(
                            R.string.profile_weight,
                            (weight*0.99).toString(),
                            weight.toString()
                        )

                        binding.tvWeight.text = weight.toString()+"Kg"
                    }
                }

                launch {
                    viewModel.dDayObserve.collectLatest {
                        if(it == DataStoreResult.SET_D_DAY) {
                            binding.btModify.isEnabled = false
                            binding.btModify.setTextColor(requireContext().getColor(R.color.c_949292))
                            return@collectLatest
                        } else if(it.isEmpty()) {
                            return@collectLatest
                        } else {
                            dDayPref = it
                            datePref = it.substring(0..9)
                            setDDay()

                            if(it.length >= 19) setTime(it.substring(14, 19))

                            val targetCalendarDay = CalendarDay.from(org.threeten.bp.LocalDate.parse(
                                datePref,
                                DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                            )
                            val targetDecorator = SelectDecorator(
                                requireContext().getColor(R.color.c_caab3f),
                                targetCalendarDay
                            )
                            binding.cvCalendar.selectedDate = targetCalendarDay
                            binding.cvCalendar.addDecorator(targetDecorator)
                        }
                    }
                }
            }

        }
    }

    private fun setCalendarView() {
        // 연, 월 헤더 스타일
        binding.cvCalendar.setHeaderTextAppearance(R.style.CalendarWidgetHeader)

        // 1~12월, 월간 표시
        binding.cvCalendar.setTitleFormatter(MonthArrayTitleFormatter(resources.getTextArray(R.array.calendar_month)))

        // 일~토, 주간 표시
        binding.cvCalendar.setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.calendar_week)))

        // 헤더 년 월로 표시
        binding.cvCalendar.setTitleFormatter { day ->
            val inputText = day.date
            val calendarHeaderElements = inputText.toString().split("-")
            val calendarHeaderBuilder = StringBuilder()

            calendarHeaderBuilder
                .append(calendarHeaderElements[0]).append("년 ")
                .append(calendarHeaderElements[1]).append("월")

            calendarHeaderBuilder.toString()
        }

        val today = CalendarDay.today()
        val disabledDates = hashSetOf<CalendarDay>()

        val dayDisableDecorator = DayDisableDecorator(disabledDates, today, requireContext().getColor(R.color.c_949292))
        val todayDecorator = TodayDecorator(requireContext().getColor(R.color.c_caab3f))

        binding.cvCalendar.addDecorators(dayDisableDecorator, todayDecorator)

        // 날짜 선택 시 처리
        binding.cvCalendar.setOnDateChangedListener { _, date, _ ->
            binding.cvCalendar.addDecorators(
                ClearDecorator(requireContext().getColor(R.color.white), date),
                dayDisableDecorator, todayDecorator,
                SelectDecorator(requireContext().getColor(R.color.c_caab3f), date)
            )

            binding.btModify.isEnabled = true
            binding.btModify.setTextColor(requireContext().getColor(R.color.white))

            val month = if(date.month<10) "0${date.month}" else "${date.month}"
            val day = if(date.day<10) "0${date.day}" else "${date.day}"

            dDayPref = "${date.year}.$month.$day${getWeekKor(date.year, date.month, date.day)}"

            setDDay()
        }
    }

    private fun setTime(time: String) {
        val (hour, minute) = parseStoredTime(time) ?: return

        binding.btTime.text = formatTimeDisplay(hour, minute)
        timePref = formatTimeStore(hour, minute)
    }

    private fun setDDay() {
        val dDay = calcDDay(dDayPref) ?: return
        binding.tvDDay.text = "D$dDay"
    }

    private fun setTimeSpinner() {
        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, pHour, pMinute ->
            binding.btTime.text = formatTimeDisplay(pHour, pMinute)
            timePref = formatTimeStore(pHour, pMinute)
        }

        val timePickerDialog = TimePickerDialog(
            requireContext(),
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

    private fun setWeightBtn() {
        if(weightPref != weight) {
            binding.btModify.isEnabled = true
            binding.btModify.setTextColor(requireContext().getColor(R.color.white))
        } else {
            binding.btModify.isEnabled = false
            binding.btModify.setTextColor(requireContext().getColor(R.color.c_949292))
        }
    }
}