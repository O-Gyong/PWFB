package com.pwfb.ui.nutrition

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pwfb.R
import androidx.fragment.app.Fragment
import com.pwfb.databinding.FragmentNutritionBinding
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.MainViewModel
import com.pwfb.util.calcDDay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NutritionFragment : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNutritionBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelObserve()
        setPref()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * 영양소 항목과 입력칸의 대응
     */
    private fun nutritionFields(): Map<PrefKey, EditText> = mapOf(
        PrefKey.CARBOHYDRATE to binding.etCarbohydrate,
        PrefKey.PROTEIN to binding.etProtein,
        PrefKey.FAT to binding.etFat,
        PrefKey.WATER to binding.etWater,
        PrefKey.SODIUM to binding.etSodium,
        PrefKey.POTASSIUM to binding.etPotassium,
        PrefKey.CREATINE to binding.etCreatine,
        PrefKey.DIETARY_FIBER to binding.etDietaryFiber
    )

    @SuppressLint("SetTextI18n")
    private fun viewModelObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state(PrefKey.D_DAY).collectLatest {
                        val dDay = calcDDay(it) ?: return@collectLatest

                        binding.tvDDay.text = "D$dDay"
                        binding.tvDate.text = it

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

                nutritionFields().forEach { (key, editText) ->
                    launch {
                        viewModel.state(key).collectLatest {
                            if (editText.text.toString() != it) {
                                editText.setText(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setPref() {
        nutritionFields().forEach { (key, editText) ->
            editText.setOnEditorActionListener { _, action, _ ->
                if (action == EditorInfo.IME_ACTION_DONE) {
                    viewModel.save(key, editText.text.toString())
                    hideKeyBoard()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setTextDDay(dDay: Int) {
        when(dDay) {
            -7 -> {
                binding.tvCarbohydrate.text = getString(R.string.nutrition_carbohydrate_day_7)
                binding.tvProtein.text = getString(R.string.nutrition_usually_intake, "🥩")
                binding.tvFat.text = getString(R.string.nutrition_fat_day_7654)
                binding.tvWater.text = getString(R.string.nutrition_usually_intake_2, "💧")
                binding.tvSodium.text = getString(R.string.nutrition_usually_intake_2, "🧂")
                binding.tvPotassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                binding.tvCreatine.text = getString(R.string.nutrition_creatine_day_7654)
                binding.tvDietaryFiber.text = getString(R.string.nutrition_usually_intake_2, "🥦")
            }
            -6 -> {
                binding.tvCarbohydrate.text = getString(R.string.nutrition_carbohydrate_day_654)
                binding.tvProtein.text = getString(R.string.nutrition_protein_day_654)
                binding.tvFat.text = getString(R.string.nutrition_fat_day_7654)
                binding.tvWater.text = getString(R.string.nutrition_usually_intake_2, "💧")
                binding.tvSodium.text = getString(R.string.nutrition_usually_intake_2, "🧂")
                binding.tvPotassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                binding.tvCreatine.text = getString(R.string.nutrition_creatine_day_7654)
                binding.tvDietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_654)
            }
            -5 -> {
                binding.tvCarbohydrate.text = getString(R.string.nutrition_carbohydrate_day_654)
                binding.tvProtein.text = getString(R.string.nutrition_protein_day_654)
                binding.tvFat.text = getString(R.string.nutrition_fat_day_7654)
                binding.tvWater.text = getString(R.string.nutrition_usually_intake_2, "💧")
                binding.tvSodium.text = getString(R.string.nutrition_usually_intake_2, "🧂")
                binding.tvPotassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                binding.tvCreatine.text = getString(R.string.nutrition_creatine_day_7654)
                binding.tvDietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_654)
            }
            -4 -> {
                binding.tvCarbohydrate.text = getString(R.string.nutrition_carbohydrate_day_654)
                binding.tvProtein.text = getString(R.string.nutrition_protein_day_654)
                binding.tvFat.text = getString(R.string.nutrition_fat_day_7654)
                binding.tvWater.text = getString(R.string.nutrition_usually_intake_2, "💧")
                binding.tvSodium.text = getString(R.string.nutrition_usually_intake_2, "🧂")
                binding.tvPotassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                binding.tvCreatine.text = getString(R.string.nutrition_creatine_day_7654)
                binding.tvDietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_654)
            }
            -3 -> {
                binding.tvCarbohydrate.text = getString(R.string.nutrition_carbohydrate_day_3)
                binding.tvProtein.text = getString(R.string.nutrition_protein_day_32)
                binding.tvFat.text = getString(R.string.nutrition_fat_day_32)
                binding.tvWater.text = getString(R.string.nutrition_water_day_3)
                binding.tvSodium.text = getString(R.string.nutrition_sodium_day_3)
                binding.tvPotassium.text = getString(R.string.nutrition_potassium_day_3)
                binding.tvCreatine.text = getString(R.string.nutrition_creatine_day_321)
                binding.tvDietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_321)
            }
            -2 -> {
                binding.tvCarbohydrate.text = getString(R.string.nutrition_carbohydrate_day_2)
                binding.tvProtein.text = getString(R.string.nutrition_protein_day_32)
                binding.tvFat.text = getString(R.string.nutrition_fat_day_32)
                binding.tvWater.text = getString(R.string.nutrition_water_day_2)
                binding.tvSodium.text = getString(R.string.nutrition_sodium_day_2)
                binding.tvPotassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                binding.tvCreatine.text = getString(R.string.nutrition_creatine_day_321)
                binding.tvDietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_321)
            }
            -1 -> {
                binding.tvCarbohydrate.text = getString(R.string.nutrition_carbohydrate_day_1)
                binding.tvProtein.text = getString(R.string.nutrition_protein_day_1)
                binding.tvFat.text = getString(R.string.nutrition_fat_day_1)
                binding.tvWater.text = getString(R.string.nutrition_water_day_1)
                binding.tvSodium.text = getString(R.string.nutrition_sodium_day_1)
                binding.tvPotassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                binding.tvCreatine.text = getString(R.string.nutrition_creatine_day_321)
                binding.tvDietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_321)
            }
        }
    }

    private fun hideKeyBoard() {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }
}