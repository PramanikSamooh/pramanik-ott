package net.munipramansagar.ott.shared.domain

import net.munipramansagar.ott.shared.model.PlaylistData

object PlaylistSorter {
    fun sortPlaylists(playlists: List<PlaylistData>): List<PlaylistData> {
        val visible = playlists.filter { it.visible }
        val pinned = visible.filter { it.pinned }.sortedBy { it.displayOrder }
        val monthly = visible.filter { !it.pinned && it.isMonthly }.sortedByDescending { it.title }
        val series = visible.filter { !it.pinned && !it.isMonthly }.sortedBy { it.displayOrder }
        return pinned + monthly + series
    }

    fun separateMonthlyAndSeries(playlists: List<PlaylistData>): Triple<List<PlaylistData>, List<PlaylistData>, List<PlaylistData>> {
        val pinned = playlists.filter { it.pinned }
        val nonPinned = playlists.filter { !it.pinned }
        val monthly = nonPinned.filter { it.isMonthly }.sortedByDescending { it.title }
        val series = nonPinned.filter { !it.isMonthly }.sortedBy { it.displayOrder }
        return Triple(pinned, monthly, series)
    }
}
