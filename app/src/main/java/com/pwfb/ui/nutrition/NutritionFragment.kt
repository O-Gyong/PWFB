package com.pwfb.ui.nutrition

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
import com.pwfb.databinding.FragmentNutritionBinding
import com.pwfb.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.pwfb.util.calcDDay


@AndroidEntryPoint
class NutritionFragment : BaseFragment() {

    private lateinit var binding: FragmentNutritionBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNutritionBinding.inflate(layoutInflater)

        viewModelObserve()
        viewModel.getDDay()
        viewModel.getCarbohydrate()
        viewModel.getProtein()
        viewModel.getFat()
        viewModel.getWater()
        viewModel.getSodium()
        viewModel.getPotassium()
        viewModel.getCreatine()
        viewModel.getDietaryFiber()

        setPref()

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
                        val dDay = calcDDay(it) ?: return@collectLatest

                        binding.tvDDay.text = "D$dDay"
                        binding.tvDate.text = it

                        setTextDDay(dDay)
                    }
                }

                launch {
                    viewModel.carbohydrateObserve.collectLatest {
                        if(it == DataStoreResult.SET_CARBOHYDRATE) {
                            return@collectLatest
                        } else {
                            binding.etCarbohydrate.setText(it)
                        }
                    }
                }

                launch {
                    viewModel.proteinObserve.collectLatest {
                        if(it == DataStoreResult.SET_PROTEIN) {
                            return@collectLatest
                        } else {
                            binding.etProtein.setText(it)
                        }
                    }
                }

                launch {
                    viewModel.fatObserve.collectLatest {
                        if (it == DataStoreResult.SET_FAT) {
                            return@collectLatest
                        } else {
                            binding.etFat.setText(it)
                        }

                    }
                }

                launch {
                    viewModel.waterObserve.collectLatest {
                        if(it == DataStoreResult.SET_WATER) {
                            return@collectLatest
                        } else {
                            binding.etWater.setText(it)
                        }
                    }
                }

                launch {
                    viewModel.sodiumObserve.collectLatest {
                        if(it == DataStoreResult.SET_SODIUM) {
                            return@collectLatest
                        } else {
                            binding.etSodium.setText(it)
                        }
                    }
                }

                launch {
                    viewModel.potassiumObserve.collectLatest {
                        if(it == DataStoreResult.SET_POTASSIUM) {
                            return@collectLatest
                        } else {
                            binding.etPotassium.setText(it)
                        }
                    }
                }

                launch {
                    viewModel.creatineObserve.collectLatest {
                        if(it == DataStoreResult.SET_CREATINE) {
                            return@collectLatest
                        } else {
                            binding.etCreatine.setText(it)
                        }
                    }
                }

                launch {
                    viewModel.dietaryFiber.collectLatest {
                        if(it == DataStoreResult.SET_DIETARY_FIBER) {
                            return@collectLatest
                        } else {
                            binding.etDietaryFiber.setText(it)
                        }
                    }
                }
            }
        }
    }

    private fun setPref() {
        binding.etCarbohydrate.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setCarbohydrate(binding.etCarbohydrate.text.toString())
                binding.etCarbohydrate.setText(binding.etCarbohydrate.text.toString())
                hideKeyBoard()
                handled = true
            }
            handled
        }

        binding.etProtein.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setProtein(binding.etProtein.text.toString())
                binding.etProtein.setText(binding.etProtein.text.toString())
                hideKeyBoard()
                handled = true
            }
            handled
        }

        binding.etFat.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setFat(binding.etFat.text.toString())
                binding.etFat.setText(binding.etFat.text.toString())
                hideKeyBoard()
                handled = true
            }
            handled
        }

        binding.etWater.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setWater(binding.etWater.text.toString())
                binding.etWater.setText(binding.etWater.text.toString())
                hideKeyBoard()
                handled = true
            }
            handled
        }

        binding.etSodium.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setSodium(binding.etSodium.text.toString())
                binding.etSodium.setText(binding.etSodium.text.toString())
                hideKeyBoard()
                handled = true
            }
            handled
        }

        binding.etPotassium.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setPotassium(binding.etPotassium.text.toString())
                binding.etPotassium.setText(binding.etPotassium.text.toString())
                hideKeyBoard()
                handled = true
            }
            handled
        }

        binding.etCreatine.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setCreatine(binding.etCreatine.text.toString())
                binding.etCreatine.setText(binding.etCreatine.text.toString())
                hideKeyBoard()
                handled = true
            }
            handled
        }

        binding.etDietaryFiber.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if(action == EditorInfo.IME_ACTION_DONE) {
                viewModel.setDietaryFiber(binding.etDietaryFiber.text.toString())
                binding.etDietaryFiber.setText(binding.etDietaryFiber.text.toString())
                hideKeyBoard()
                handled = true
            }
            handled
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