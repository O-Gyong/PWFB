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
import androidx.fragment.app.Fragment
import com.pwfb.common.NutritionObject.nutritionEntityLists
import com.pwfb.databinding.FragmentHomeBinding
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.pwfb.util.calcDDay


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private var kg = 0.0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
                // 영양 섭취량은 체중(kg)에 의존하므로 체중과 D-Day가 모두 준비된 뒤에 그린다
                launch {
                    combine(
                        viewModel.state(PrefKey.WEIGHT),
                        viewModel.state(PrefKey.D_DAY)
                    ) { weight, dDayText -> weight to dDayText }.collectLatest { (weight, dDayText) ->
                        // 아직 읽기 전(빈 문자열)이면 무시
                        kg = weight.toDoubleOrNull() ?: return@collectLatest
                        binding.tvWeight.text = weight + getString(R.string.kg)

                        val dDay = calcDDay(dDayText) ?: return@collectLatest

                        binding.tvDDay.text = "D$dDay"
                        binding.tvDate.text = dDayText

                        binding.lvNutrition.adapter = ListAdapter(nutritionEntityLists, kg, dDay)

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