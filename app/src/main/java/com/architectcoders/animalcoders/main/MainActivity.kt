package com.architectcoders.animalcoders.main

import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.architectcoders.animalcoders.R
import com.architectcoders.animalcoders.login.LoginActivity
import com.architectcoders.animalcoders.profile.ProfileFragment
import com.architectcoders.animalcoders.search.SearchFragment
import com.architectcoders.animalcoders.search.detail.AnimalDetailActivity
import com.architectcoders.animalcoders.search.favourites.FavouritesActivity
import com.example.baseandroid.activity.BaseActivity
import com.example.baseandroid.extensions.addFragment
import com.example.baseandroid.extensions.getCurrentFragment
import com.example.baseandroid.extensions.goToActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel


class MainActivity :
    BaseActivity<MainActivityViewState, MainActivityViewTransition, MainActivityViewModel>() {

    private var stateExecuted = false

    override val viewModel: MainActivityViewModel by viewModel()

    override fun getLayout(): Int = R.layout.activity_main

    override fun initView() {
        //view configurations, adapter initialization
        setSupportActionBar(MainToolbar)
    }

    override fun manageState(state: MainActivityViewState) {
        stateExecuted = true

        when (state) {
            MainActivityViewState.SearchState -> {
                setSupportActionBarTitle(getString(R.string.tab_search))
                MainBottomNav.selectedItemId = R.id.bottom_action_search
            }
            MainActivityViewState.ProfileState -> {
                setSupportActionBarTitle(getString(R.string.tab_profile))
                MainBottomNav.selectedItemId = R.id.bottom_action_profile
            }
        }
    }

    override fun manageTransition(transition: MainActivityViewTransition) {
        when (transition) {
            is MainActivityViewTransition.NavigateToLogin -> {
                finish()
                goToActivity<LoginActivity>()
            }
            is MainActivityViewTransition.NavigateToSearch -> {
                replaceTabFragment(TabType.SEARCH)
            }
            is MainActivityViewTransition.NavigateToProfile -> {
                replaceTabFragment(TabType.PROFILE)
            }
            is MainActivityViewTransition.NavigateToAnimalDetail -> {
                goToActivity<AnimalDetailActivity>(clear = false) {
                    putExtra(AnimalDetailActivity.ANIMAL, transition.animal)
                }
            }
            is MainActivityViewTransition.NavigateToFavourites ->{
                goToActivity<FavouritesActivity>(clear = false) {
                }
            }
        }
    }

    override fun initListeners() {
        MainBottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_action_search -> {
                    if (!stateExecuted) {
                        viewModel.onSearchTabClicked()
                    }
                    stateExecuted = false
                    true
                }
                R.id.bottom_action_profile -> {
                    if (!stateExecuted) {
                        viewModel.onProfileTabClicked()
                    }
                    stateExecuted = false
                    true
                }
                else -> false
            }
        }
    }

    private fun setSupportActionBarTitle(title: String) {
        supportActionBar?.title = title
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_log_out_btn) {
            viewModel.logOut()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addFragmentToStack(
        fragment: Fragment,
        fragmentTag: String
    ): Fragment {
        addFragment(fragment, R.id.main_container, fragmentTag, null)
        return fragment
    }

    private fun replaceTabFragment(type: TabType?) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = when (type) {
            TabType.SEARCH -> {
                getCurrentFragment(SearchFragment.TAG)
                    ?: addFragmentToStack(SearchFragment.newInstance(), SearchFragment.TAG)
            }
            TabType.PROFILE -> {
                getCurrentFragment(ProfileFragment.TAG)
                    ?: addFragmentToStack(ProfileFragment.newInstance(), ProfileFragment.TAG)
            }
            else -> null
        }
        fragment?.run {
            when (this) {
                is SearchFragment -> {
                    getCurrentFragment(ProfileFragment.TAG)?.let {
                        fragmentTransaction.hide(it)
                    }
                }
                is ProfileFragment -> {
                    getCurrentFragment(SearchFragment.TAG)?.let {
                        fragmentTransaction.hide(it)
                    }
                }
            }
            fragmentTransaction.show(this)
            fragmentTransaction.commit()
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is SearchFragment -> {
                fragment.run {
                    navigateToDetail = { animal ->
                        this@MainActivity.viewModel.navigateToDetail(animal)
                    }
                }
            }
            is ProfileFragment -> {
                fragment.run {
                    navigateToLogin = {
                        this@MainActivity.viewModel.logOut()
                    }
                    navigateToFavourites = {
                        this@MainActivity.viewModel.navigateToFavourites()
                    }
                }
            }
        }
    }
}
