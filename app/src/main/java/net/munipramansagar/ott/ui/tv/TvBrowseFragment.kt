package net.munipramansagar.ott.ui.tv

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.munipramansagar.ott.R
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.presenter.VideoCardPresenter
import net.munipramansagar.ott.util.LanguageManager
import net.munipramansagar.ott.viewmodel.HomeSectionData
import net.munipramansagar.ott.viewmodel.HomeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class TvBrowseFragment : BrowseSupportFragment() {

    @Inject
    lateinit var languageManager: LanguageManager

    private lateinit var viewModel: HomeViewModel
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeData()
    }

    private fun setupUI() {
        title = if (languageManager.isHindi()) "प्रामाणिक" else "Pramanik"
        brandColor = ContextCompat.getColor(requireContext(), R.color.saffron)
        searchAffordanceColor = ContextCompat.getColor(requireContext(), R.color.gold)
        adapter = rowsAdapter

        // Handle video clicks
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Video) {
                val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra("videoId", item.id)
                    putExtra("videoTitle", item.title)
                }
                startActivity(intent)
            }
        }

        // Search click
        setOnSearchClickedListener {
            val fragment = TvSearchFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.tv_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (!state.isLoading && state.error == null) {
                    buildRows(state.sections)
                }
            }
        }
    }

    private fun buildRows(sections: List<HomeSectionData>) {
        rowsAdapter.clear()
        val isHindi = languageManager.isHindi()
        val cardPresenter = VideoCardPresenter()

        var rowIndex = 0
        sections.forEach { sectionData ->
            sectionData.playlists.forEach { playlistWithVideos ->
                val label = "${sectionData.section.getLabel(isHindi)} - ${playlistWithVideos.playlist.title}"
                val header = HeaderItem(rowIndex.toLong(), label)
                val listRowAdapter = ArrayObjectAdapter(cardPresenter)
                playlistWithVideos.videos.forEach { listRowAdapter.add(it) }
                rowsAdapter.add(ListRow(header, listRowAdapter))
                rowIndex++
            }
        }
    }
}
