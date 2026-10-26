package com.pwfb.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pwfb.R
import com.pwfb.base.BaseFragment
import com.pwfb.common.DataStoreResult
import com.pwfb.common.NutritionObject.nutritionEntityLists
import com.pwfb.databinding.FragmentHomeBinding
import com.pwfb.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.pwfb.util.calcDDay


@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MainViewModel by viewModels()
    private var kg = 0.0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        viewModelObserve()
        viewModel.getWeight()



        return binding.root
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun viewModelObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.weightObserve.collectLatest {
                        if(it == DataStoreResult.SET_WEIGHT){
                            // todo
                            return@collectLatest
                        }
                        // 초기값("")이나 미설정 값("WeightNull")은 숫자가 아니므로 무시
                        kg = it.toDoubleOrNull() ?: return@collectLatest
                        binding.tvWeight.text = it + getString(R.string.kg)
                        viewModel.getDDay()
                    }
                }

                launch {
                    viewModel.dDayObserve.collectLatest {
                        if(it == DataStoreResult.SET_D_DAY) {
                            // todo
                            return@collectLatest
                        }
                        if(it.isEmpty()) {
                            // todo
                            return@collectLatest
                        }
                        val dDay = calcDDay(it) ?: return@collectLatest

                        binding.tvDDay.text = "D$dDay"
                        binding.tvDate.text = it

                        binding.lvNutrition.adapter = ListAdapter(requireContext(), nutritionEntityLists, kg, dDay)

                        binding.tvTraining.text = when(dDay) {
                            -6 -> {
                                getString(R.string.home_training_day_6)
                            }
                            -5, -4 -> {
                                getString(R.string.home_training_day_54)
                            }
                            -3 -> {
                                getString(R.string.home_training_day_3)
                            }
                            -2 -> {
                                getString(R.string.home_training_day_2)
                            }
                            -1 -> {
                                getString(R.string.home_training_day_1)
                            }
                            else -> {
                                getString(R.string.nutrition_usually_intake, "💪")
                            }
                        }

                        binding.tvPeakWeekStrategy.text = when(dDay) {
                            -7 -> {
                                getString(R.string.re_feed_day)
                            }
                            -6, -5, -4 -> {
                                getString(R.string.low_carbohydrate_day)
                            }
                            -3, -2 -> {
                                getString(R.string.high_carbohydrate_day)
                            }
                            -1 -> {
                                getString(R.string.correction_day)
                            }
                            0 -> {
                                getString(R.string.man_of_the_hour)
                            }
                            else -> {
                                getString(R.string.gonna_be_alright)
                            }
                        }
                    }
                }
            }
        }
    }
}