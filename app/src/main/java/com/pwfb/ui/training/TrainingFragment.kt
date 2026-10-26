package com.pwfb.ui.training

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pwfb.R
import com.pwfb.base.BaseFragment
import com.pwfb.common.DataStoreResult
import com.pwfb.databinding.FragmentTrainingBinding
import com.pwfb.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar


@AndroidEntryPoint
class TrainingFragment : BaseFragment() {

    private lateinit var binding: FragmentTrainingBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingBinding.inflate(layoutInflater)

        viewModelObserve()
        viewModel.getDDay()
        viewModel.getTrainingProgram()

        binding.etTrainingProgram.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setTrainingProgram(binding.etTrainingProgram.text.toString())
                binding.etTrainingProgram.setText(binding.etTrainingProgram.text.toString())
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
                handled = true
            }
            handled
        }

        return binding.root
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun viewModelObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dDayObserve.collectLatest {
                        if(it == DataStoreResult.SET_D_DAY) {
                            // todo
                            return@collectLatest
                        }
                        if(it.isEmpty()) {
                            return@collectLatest
                        }
                        val datePref = it.substring(0..9)
                        val targetDate = SimpleDateFormat("yyyy.MM.dd").parse(datePref)!!.time
                        val today = Calendar.getInstance().time.time

                        val dDay = (today - targetDate) / (60*60*24*1000)

                        binding.tvDDay.text = "D$dDay"
                        binding.tvDate.text = it
                        setTextDDay(dDay.toInt())
                    }
                }

                launch {
                    viewModel.trainingProgramObserve.collectLatest {
                        if(it == DataStoreResult.SET_TRAINING_PROGRAM) {
                            // todo
                            return@collectLatest
                        } else {
                            binding.etTrainingProgram.setText(it)
                        }
                    }
                }
            }
        }
    }

    private fun setTextDDay(dDay: Int) {
        when(dDay) {
            -7 -> {
                binding.tvTraining.text = getString(R.string.nutrition_usually_intake, "\uD83C\uDFCB")
            }
            -6 -> {
                binding.tvTraining.text = getString(R.string.training_day_6)
            }
            -5, -4 -> {
                binding.tvTraining.text = getString(R.string.training_day_54)
            }
            -3 -> {
                binding.tvTraining.text = getString(R.string.training_day_3)
            }
            -2 -> {
                binding.tvTraining.text = getString(R.string.training_day_2)
            }
            -1 -> {
                binding.tvTraining.text = getString(R.string.training_day_1)
            }
            else -> {
                binding.tvTraining.text = getString(R.string.nutrition_usually_intake, "\uD83C\uDFCB")
            }
        }
    }
}