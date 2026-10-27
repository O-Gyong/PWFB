package com.pwfb.ui.nutrition

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pwfb.R
import androidx.fragment.app.Fragment
import com.pwfb.common.NutritionObject
import com.pwfb.databinding.FragmentNutritionBinding
import com.pwfb.databinding.ViewNutritionInputBinding
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.MainViewModel
import com.pwfb.ui.NutrientUi
import com.pwfb.ui.showNutrientAvatar
import com.pwfb.util.calcDDay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NutritionFragment : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    /**
     * 영양소 항목 카드와 저장 키의 대응
     */
    private class Field(val key: PrefKey, val view: ViewNutritionInputBinding, val ui: NutrientUi)

    private fun fields(): List<Field> = listOf(
        Field(PrefKey.CARBOHYDRATE, binding.carbohydrate, NutrientUi.of(NutritionObject.CARBOHYDRATE)),
        Field(PrefKey.PROTEIN, binding.protein, NutrientUi.of(NutritionObject.PROTEIN)),
        Field(PrefKey.FAT, binding.fat, NutrientUi.of(NutritionObject.FAT)),
        Field(PrefKey.WATER, binding.water, NutrientUi.of(NutritionObject.WATER)),
        Field(PrefKey.SODIUM, binding.sodium, NutrientUi.of(NutritionObject.SODIUM)),
        Field(PrefKey.POTASSIUM, binding.potassium, NutrientUi.of(NutritionObject.POTASSIUM)),
        Field(PrefKey.CREATINE, binding.creatine, NutrientUi.of(NutritionObject.CREATINE)),
        Field(PrefKey.DIETARY_FIBER, binding.dietaryFiber, NutrientUi.of(NutritionObject.DIETARY_FIBER))
    )

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
        setupFields()
        viewModelObserve()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * 카드마다 이모지·이름·단위를 넣고, 완료 키나 포커스 이동 시 입력값을 저장한다
     */
    private fun setupFields() {
        fields().forEach { field ->
            field.view.tvEmoji.showNutrientAvatar(field.ui)
            field.view.tvName.setText(field.ui.nameRes)
            field.view.tilInput.suffixText = getString(field.ui.unitRes)

            val editText = field.view.etInput
            editText.setOnEditorActionListener { _, action, _ ->
                if (action == EditorInfo.IME_ACTION_DONE) {
                    viewModel.save(field.key, editText.text.toString())
                    editText.clearFocus()
                    hideKeyBoard()
                    true
                } else {
                    false
                }
            }
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) viewModel.save(field.key, editText.text.toString())
            }
        }
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

                fields().forEach { field ->
                    launch {
                        viewModel.state(field.key).collectLatest {
                            val editText = field.view.etInput
                            if (editText.text.toString() != it) {
                                editText.setText(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setTextDDay(dDay: Int) {
        val carbohydrate = binding.carbohydrate.tvGuide
        val protein = binding.protein.tvGuide
        val fat = binding.fat.tvGuide
        val water = binding.water.tvGuide
        val sodium = binding.sodium.tvGuide
        val potassium = binding.potassium.tvGuide
        val creatine = binding.creatine.tvGuide
        val dietaryFiber = binding.dietaryFiber.tvGuide

        when(dDay) {
            -7 -> {
                carbohydrate.text = getString(R.string.nutrition_carbohydrate_day_7)
                protein.text = getString(R.string.nutrition_usually_intake, "🥩")
                fat.text = getString(R.string.nutrition_fat_day_7654)
                water.text = getString(R.string.nutrition_usually_intake_2, "💧")
                sodium.text = getString(R.string.nutrition_usually_intake_2, "🧂")
                potassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                creatine.text = getString(R.string.nutrition_creatine_day_7654)
                dietaryFiber.text = getString(R.string.nutrition_usually_intake_2, "🥦")
            }
            -6, -5, -4 -> {
                carbohydrate.text = getString(R.string.nutrition_carbohydrate_day_654)
                protein.text = getString(R.string.nutrition_protein_day_654)
                fat.text = getString(R.string.nutrition_fat_day_7654)
                water.text = getString(R.string.nutrition_usually_intake_2, "💧")
                sodium.text = getString(R.string.nutrition_usually_intake_2, "🧂")
                potassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                creatine.text = getString(R.string.nutrition_creatine_day_7654)
                dietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_654)
            }
            -3 -> {
                carbohydrate.text = getString(R.string.nutrition_carbohydrate_day_3)
                protein.text = getString(R.string.nutrition_protein_day_32)
                fat.text = getString(R.string.nutrition_fat_day_32)
                water.text = getString(R.string.nutrition_water_day_3)
                sodium.text = getString(R.string.nutrition_sodium_day_3)
                potassium.text = getString(R.string.nutrition_potassium_day_3)
                creatine.text = getString(R.string.nutrition_creatine_day_321)
                dietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_321)
            }
            -2 -> {
                carbohydrate.text = getString(R.string.nutrition_carbohydrate_day_2)
                protein.text = getString(R.string.nutrition_protein_day_32)
                fat.text = getString(R.string.nutrition_fat_day_32)
                water.text = getString(R.string.nutrition_water_day_2)
                sodium.text = getString(R.string.nutrition_sodium_day_2)
                potassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                creatine.text = getString(R.string.nutrition_creatine_day_321)
                dietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_321)
            }
            -1 -> {
                carbohydrate.text = getString(R.string.nutrition_carbohydrate_day_1)
                protein.text = getString(R.string.nutrition_protein_day_1)
                fat.text = getString(R.string.nutrition_fat_day_1)
                water.text = getString(R.string.nutrition_water_day_1)
                sodium.text = getString(R.string.nutrition_sodium_day_1)
                potassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                creatine.text = getString(R.string.nutrition_creatine_day_321)
                dietaryFiber.text = getString(R.string.nutrition_dietary_fiber_day_321)
            }
            else -> {
                // 피크위크 밖: 평소대로
                carbohydrate.text = getString(R.string.nutrition_usually_intake_2, "🌾")
                protein.text = getString(R.string.nutrition_usually_intake_2, "🥩")
                fat.text = getString(R.string.nutrition_usually_intake_2, "🥜")
                water.text = getString(R.string.nutrition_usually_intake_2, "💧")
                sodium.text = getString(R.string.nutrition_usually_intake_2, "🧂")
                potassium.text = getString(R.string.nutrition_usually_intake_2, "🍌")
                creatine.text = getString(R.string.nutrition_usually_intake_2, "🔥")
                dietaryFiber.text = getString(R.string.nutrition_usually_intake_2, "🥦")
            }
        }
    }

    private fun hideKeyBoard() {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }
}
