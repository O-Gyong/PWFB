package com.pwfb.ui.profile

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.installations.FirebaseInstallations
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.pwfb.R
import com.pwfb.base.BaseFragment
import com.pwfb.common.Firebase
import com.pwfb.databinding.FragmentProfileBinding
import com.pwfb.domain.entity.PrefKey
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

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    private var dDayPref = ""
    private var datePref = ""
    private var timePref = ""
    private var weightPref = 0.0
    private var weight = 0.0

    private var infoDialog: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListener()
        setCalendarView()
        viewModelObserve()

        init()
    }

    override fun onDestroyView() {
        infoDialog?.dismiss()
        infoDialog = null
        _binding = null
        super.onDestroyView()
    }

    private fun init() {
        infoDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(layoutInflater.inflate(R.layout.dialog_info, null))
        }
    }

    private fun setOnClickListener() {
        binding.btModify.setOnClickListener {
            viewModel.save(PrefKey.D_DAY, "$dDayPref $timePref")
            viewModel.save(PrefKey.WEIGHT, weight.toString())
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
            infoDialog?.show()
        }

        binding.clTheDayStrategy.setOnClickListener {
            StrategyDialogFragment().show(childFragmentManager, "strategy")
        }

        binding.clLogSend.setOnClickListener {
            viewModel.requestLogUpload()
        }
    }

    private fun viewModelObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state(PrefKey.NAME).collectLatest {
                        binding.tvName.text = it
                    }
                }

                launch {
                    viewModel.state(PrefKey.WEIGHT).collectLatest {
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
                    viewModel.state(PrefKey.D_DAY).collectLatest {
                        if(it.isEmpty()) {
                            return@collectLatest
                        }
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

                // 체중/D-Day 저장에 성공하면 수정 버튼 비활성화, 실패하면 사용자에게 알림
                launch {
                    viewModel.saveResult.collect {
                        if(!it.success) {
                            showToast(R.string.save_failed)
                        } else if(it.key == PrefKey.WEIGHT || it.key == PrefKey.D_DAY) {
                            binding.btModify.isEnabled = false
                            binding.btModify.setTextColor(requireContext().getColor(R.color.c_949292))
                        }
                    }
                }

                launch {
                    viewModel.logUpload.collect { uploadLog(it) }
                }
            }

        }
    }

    /**
     * 오늘 로그 파일을 Firebase Storage로 전송한다. file이 null이면 전송할 로그가 없는 것.
     * 같은 날 여러 사용자가 올려도 덮어쓰지 않도록 앱 설치 ID로 파일명을 구분한다.
     */
    private fun uploadLog(file: File?) {
        if(file == null) {
            showToast(R.string.log_empty)
            return
        }

        showToast(R.string.log_sending)
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            val name = if(task.isSuccessful) task.result else "unknown_${System.currentTimeMillis()}"
            try {
                Firebase.firebaseReference
                    .child("logs/${getCurrentToday()}/$name.txt")
                    .putFile(Uri.fromFile(file))
                    .addOnSuccessListener { showToast(R.string.log_send_success) }
                    .addOnFailureListener { showToast(R.string.log_send_fail) }
            } catch (e: Exception) {
                showToast(R.string.log_send_fail)
            }
        }
    }

    /**
     * 전송 결과처럼 화면이 사라진 뒤에 호출될 수 있는 콜백에서도 안전하게 토스트를 띄운다.
     */
    private fun showToast(@StringRes messageRes: Int) {
        val context = context ?: return
        Toast.makeText(context.applicationContext, messageRes, Toast.LENGTH_SHORT).show()
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