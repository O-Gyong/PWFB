package com.pwfb.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pwfb.common.NutritionObject
import com.pwfb.databinding.ItemNutritionBinding
import com.pwfb.domain.entity.NutritionEntity
import com.pwfb.ui.NutrientUi
import com.pwfb.ui.showNutrientAvatar

class ListAdapter(
    private val nutritionEntityList: List<NutritionEntity>,
    private val kg: Double,
    private val dDay: Int
): RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    inner class ViewHolder(
        private val binding: ItemNutritionBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(nutritionEntity: NutritionEntity) {
            val context = binding.root.context
            binding.tvEmoji.showNutrientAvatar(NutrientUi.of(nutritionEntity.type))
            binding.tvExplain.setText(nutritionEntity.explainRes)

            val intake = NutritionIntake.of(nutritionEntity.type, dDay, kg)
            binding.tvIntake.text = context.getString(intake.resId, *intake.args.toTypedArray())

            // 마지막 항목은 구분선을 숨긴다
            binding.vDivider.visibility =
                if (nutritionEntity.type == NutritionObject.DIETARY_FIBER) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemNutritionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = nutritionEntityList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(nutritionEntityList[position])
    }
}
