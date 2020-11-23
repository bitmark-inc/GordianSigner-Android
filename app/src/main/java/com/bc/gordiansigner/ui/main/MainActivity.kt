package com.bc.gordiansigner.ui.main

import androidx.fragment.app.Fragment
import com.bc.gordiansigner.R
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.BaseViewModel
import com.bc.gordiansigner.ui.account.AccountsFragment
import com.bc.gordiansigner.ui.share_account.ShareAccountMapFragment
import com.bc.gordiansigner.ui.sign.PsbtSignFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseAppCompatActivity() {

    override fun layoutRes() = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        loadFragment(PsbtSignFragment())

        navigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_psbt -> {
                    loadFragment(PsbtSignFragment())
                    true
                }
                R.id.navigation_signers -> {
                    loadFragment(AccountsFragment())
                    true
                }
                R.id.navigation_account_map -> {
                    loadFragment(ShareAccountMapFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}