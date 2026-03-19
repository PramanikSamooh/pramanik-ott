package net.munipramansagar.ott.ui.tv.presenter

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import net.munipramansagar.ott.R
import net.munipramansagar.ott.data.model.Video

class VideoCardPresenter : Presenter() {

    companion object {
        private const val CARD_WIDTH = 300
        private const val CARD_HEIGHT = 176 // 16:9 ratio
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            setBackgroundColor(
                ContextCompat.getColor(context, R.color.surface)
            )
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val video = item as Video
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = video.title
        cardView.contentText = buildString {
            append(video.durationFormatted)
            if (video.viewCountFormatted.isNotEmpty()) {
                append(" • ")
                append(video.viewCountFormatted)
                append(" views")
            }
        }

        val thumbnailUrl = video.thumbnailUrlHQ.ifEmpty { video.thumbnailUrl }
        if (thumbnailUrl.isNotEmpty()) {
            Glide.with(cardView.context)
                .load(thumbnailUrl)
                .centerCrop()
                .into(cardView.mainImageView)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}
