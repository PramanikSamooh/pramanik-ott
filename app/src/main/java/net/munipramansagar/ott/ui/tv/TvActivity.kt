package net.munipramansagar.ott.ui.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import net.munipramansagar.ott.R

@AndroidEntryPoint
class TvActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tv_fragment_container, TvBrowseFragment())
                .commit()
        }
    }
}
