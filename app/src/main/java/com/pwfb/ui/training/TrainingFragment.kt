package com.pwfb.ui.training

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pwfb.R
import androidx.fragment.app.Fragment
import com.pwfb.databinding.FragmentTrainingBinding
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.pwfb.util.calcDDay


@AndroidEntryPoint
class TrainingFragment : Fragment() {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)

        // 여러 줄 입력이라 완료 키가 없으므로, 입력칸 밖을 누르거나 탭을 옮겨 포커스가 빠질 때 저장한다
        binding.etTrainingProgram.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.save(PrefKey.TRAINING_PROGRAM, binding.etTrainingProgram.text.toString())
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelObserve()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    @SuppressLint("SetTextI18n")
    private fun viewModelObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state(PrefKey.D_DAY).collectLatest {
                        val dDay = calcDDay(it) ?: return@collectLatest

                        binding.header.tvDDay.text = "D$dDay"
                        binding.header.tvDate.text = it
                        setTextDDay(dDay)
                    }
                }

                // 저장 실패 시 사용자에게 알림
                launch {
                    viewModel.saveResult.collect {
                        if (!it.success) {
                            Toast.makeText(requireContext(), R.string.save_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                launch {
                    viewModel.state(PrefKey.TRAINING_PROGRAM).collectLatest {
                        if (binding.etTrainingProgram.text.toString() != it) {
                            binding.etTrainingProgram.setText(it)
                        }
                    }
                }
            }
        }
    }

    private fun setTextDDay(dDay: Int) {
        binding.tvTraining.text = when(dDay) {
            -6 -> getString(R.string.training_day_6)
            -5, -4 -> getString(R.string.training_day_54)
            -3 -> getString(R.string.training_day_3)
            -2 -> getString(R.string.training_day_2)
            -1 -> getString(R.string.training_day_1)
            else -> getString(R.string.nutrition_usually_intake, "🏋")
        }
    }
}
