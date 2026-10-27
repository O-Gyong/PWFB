package com.pwfb.ui.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.storage.StorageReference
import com.pwfb.R
import com.pwfb.databinding.DialogInfoBinding
import com.pwfb.databinding.FragmentProfileBinding
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.MainViewModel
import com.pwfb.util.calcDDay
import com.pwfb.util.formatDatePref
import com.pwfb.util.formatTimeDisplay
import com.pwfb.util.formatTimeStore
import com.pwfb.util.getCurrentToday
import com.pwfb.util.parseDatePref
import com.pwfb.util.parseTimePref
import com.pwfb.util.reattachPwfbPickers
import com.pwfb.util.showPwfbDatePicker
import com.pwfb.util.showPwfbTimePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject


@AndroidEntryPoint
@SuppressLint("SetTextI18n")
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var logStorageReference: StorageReference

    // 화면에서 고른(아직 저장 전일 수 있는) 대회 날짜·시간·체중
    private var dDayDate: LocalDate? = null
    private var timeHour = 0
    private var timeMinute = 0
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
        viewModelObserve()
        childFragmentManager.reattachPwfbPickers(::onDatePicked, ::onTimePicked)

        init()
    }

    override fun onDestroyView() {
        infoDialog?.dismiss()
        infoDialog = null
        _binding = null
        super.onDestroyView()
    }

    private fun init() {
        val info = DialogInfoBinding.inflate(layoutInflater)
        val versionName = runCatching {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        }.getOrNull() ?: "-"
        info.tvVersion.text = getString(R.string.app_version_format, versionName)

        infoDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(info.root)
        }
    }

    private fun setOnClickListener() {
        binding.btModify.setOnClickListener {
            dDayDate?.let { date ->
                viewModel.save(PrefKey.D_DAY, "${formatDatePref(date)} ${formatTimeStore(timeHour, timeMinute)}")
            }
            viewModel.save(PrefKey.WEIGHT, weight.toString())
        }

        binding.btDate.setOnClickListener {
            childFragmentManager.showPwfbDatePicker(dDayDate, ::onDatePicked)
        }

        binding.btTime.setOnClickListener {
            childFragmentManager.showPwfbTimePicker(timeHour, timeMinute, ::onTimePicked)
        }

        binding.btMinus.setOnClickListener {
            weight = (weight*10 - 1).toInt() / 10.0
            binding.tvWeight.text = weight.toString()+"Kg"
            setWeightBtn()
        }

        binding.btPlus.setOnClickListener {
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
                        binding.tvAvatar.text = it.trim().firstOrNull()?.toString() ?: "?"
                    }
                }

                launch {
                    viewModel.state(PrefKey.WEIGHT).collectLatest {
                        // 아직 읽기 전(빈 문자열)이면 무시
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
                        val savedDate = parseDatePref(it) ?: return@collectLatest
                        dDayDate = savedDate
                        binding.btDate.text = formatDatePref(savedDate)
                        setDDay()

                        val (hour, minute) = parseTimePref(it) ?: LocalTime.now().let { now -> now.hour to now.minute }
                        timeHour = hour
                        timeMinute = minute
                        binding.btTime.text = formatTimeDisplay(hour, minute)
                    }
                }

                // 체중/D-Day 저장에 성공하면 변경 버튼 비활성화, 실패하면 사용자에게 알림
                launch {
                    viewModel.saveResult.collect {
                        if(!it.success) {
                            showToast(R.string.save_failed)
                        } else if(it.key == PrefKey.WEIGHT || it.key == PrefKey.D_DAY) {
                            binding.btModify.isEnabled = false
                        }
                    }
                }

                launch {
                    viewModel.logUpload.collect { uploadLog(it) }
                }
            }
        }
    }

    private fun onDatePicked(date: LocalDate) {
        dDayDate = date
        binding.btDate.text = formatDatePref(date)
        setDDay()
        binding.btModify.isEnabled = true
    }

    private fun onTimePicked(hour: Int, minute: Int) {
        timeHour = hour
        timeMinute = minute
        binding.btTime.text = formatTimeDisplay(hour, minute)
        binding.btModify.isEnabled = true
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
                logStorageReference
                    .child("${getCurrentToday()}/$name.txt")
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

    private fun setDDay() {
        val date = dDayDate ?: return
        val dDay = calcDDay(formatDatePref(date)) ?: return
        binding.tvDDay.text = "D$dDay"
    }

    private fun setWeightBtn() {
        binding.btModify.isEnabled = weightPref != weight
    }
}
