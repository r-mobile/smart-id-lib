package kg.onoi.smart_sdk.ui.moderation.adapters

import kg.onoi.smart_sdk.models.ModerationItem

interface ModerationItemClick {
    fun onItemClick(item: ModerationItem)
}