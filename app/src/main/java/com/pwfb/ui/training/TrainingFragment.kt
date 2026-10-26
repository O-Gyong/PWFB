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
import com.pwfb.databinding.FragmentTrainingBinding
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.pwfb.util.calcDDay


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

        binding.etTrainingProgram.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.save(PrefKey.TRAINING_PROGRAM, binding.etTrainingProgram.text.toString())
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
                handled = true
            }
            handled
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun viewModelObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state(PrefKey.D_DAY).collectLatest {
                        val dDay = calcDDay(it) ?: return@collectLatest

                        binding.tvDDay.text = "D$dDay"
                        binding.tvDate.text = it
                        setTextDDay(dDay)
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