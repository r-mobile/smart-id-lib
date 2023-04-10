package kg.onoi.smart_sdk.ui.moderation.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.models.ModerationItem
import kotlinx.android.synthetic.main.item_moderation.view.*

class RefusedItemAdapter(private val listener: ModerationItemClick) :
    RecyclerView.Adapter<RefusedItemVH>() {

    var items = mutableListOf<ModerationItem>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefusedItemVH =
        RefusedItemVH.create(parent, listener)


    override fun getItemCount(): Int = items.count()

    override fun onBindViewHolder(holder: RefusedItemVH, position: Int) =
        holder.bind(items[position])
}

class RefusedItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var moderationItem: ModerationItem? = null

    fun bind(item: ModerationItem) {
        moderationItem = item
        itemView.tv_type.setText(moderationItem!!.titleResId)
        itemView.btn_action.setText(if (moderationItem!!.type == null) R.string.change else R.string.rephoto)
        updateButton(moderationItem!!.isSuccessUpdated)
    }

    private fun updateButton(isSuccess: Boolean) {
        val btnState: Pair<Int?, Int> = if (isSuccess) Pair(R.string.done, R.color.md_green600)
        else Pair(null, R.color.md_red800)
        itemView.btn_action.apply {
            btnState.first?.let { text = context.getString(it) }
            backgroundTintList = ContextCompat.getColorStateList(context, btnState.second)
        }
    }

    private fun setClickListener(listener: ModerationItemClick) {
        itemView.btn_action.setOnClickListener {
            if (moderationItem?.isSuccessUpdated != true) listener.onItemClick(moderationItem!!)
            else null
        }
    }

    companion object {
        fun create(parent: ViewGroup, listener: ModerationItemClick): RefusedItemVH {
            val itemVH = RefusedItemVH(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_moderation, parent, false)
            )
            itemVH.setClickListener(listener)
            return itemVH
        }
    }
}