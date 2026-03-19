package net.munipramansagar.ott.ui.tv

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.presenter.VideoCardPresenter
import net.munipramansagar.ott.viewmodel.SearchViewModel

@AndroidEntryPoint
class TvSearchFragment : SearchSupportFragment(),
    SearchSupportFragment.SearchResultProvider {

    private lateinit var viewModel: SearchViewModel
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        setSearchResultProvider(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnItemViewClickedListener(
            OnItemViewClickedListener { _, item, _, _ ->
                if (item is Video) {
                    val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra("videoId", item.id)
                        putExtra("videoTitle", item.title)
                    }
                    startActivity(intent)
                }
            }
        )

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.results.isNotEmpty()) {
                    val cardPresenter = VideoCardPresenter()
                    val header = HeaderItem(0, "Search Results")
                    val listRowAdapter = ArrayObjectAdapter(cardPresenter)
                    state.results.forEach { listRowAdapter.add(it) }
                    rowsAdapter.clear()
                    rowsAdapter.add(ListRow(header, listRowAdapter))
                } else if (state.hasSearched) {
                    rowsAdapter.clear()
                }
            }
        }
    }

    override fun getResultsAdapter(): ObjectAdapter = rowsAdapter

    override fun onQueryTextChange(newQuery: String): Boolean {
        viewModel.onQueryChanged(newQuery)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        viewModel.onQueryChanged(query)
        return true
    }
}
