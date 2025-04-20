package com.example.myapplication

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var isLandscape: Boolean = false
    private var currentItemId: Int = -1
    private var currentItemType: String = ""
    private var isCreatingNewItem: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (savedInstanceState == null) {
            setupInitialFragments()
        } else {
            restoreState(savedInstanceState)
        }
    }

    private fun setupInitialFragments() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view1, LibraryFragment())
            .commit()

        if (isLandscape && (currentItemId != -1 || isCreatingNewItem)) {
            showDetailFragment()
        }
    }

    private fun showDetailFragment() {
        if (isCreatingNewItem) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view2, ItemFragment.newInstance(true))
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view2,
                    ItemFragment.newInstance(false, currentItemId, currentItemType))
                .commit()
        }
        binding.fragmentContainerView2.visibility = View.VISIBLE
    }

    fun showItemDetails(itemId: Int, itemType: String) {
        if (isLandscape) {
            clearDetailFragment()

            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view2,
                    ItemFragment.newInstance(false, itemId, itemType))
                .commitNow()

            binding.fragmentContainerView2.visibility = View.VISIBLE

        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view1,
                    ItemFragment.newInstance(false, itemId, itemType))
                .addToBackStack("details")
                .commit()
        }
    }

    fun showCreateItem() {
        isCreatingNewItem = true
        currentItemId = -1
        currentItemType = ""

        if (isLandscape) {
            showDetailFragment()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view1, ItemFragment.newInstance(true))
                .addToBackStack("create")
                .commit()
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        currentItemId = savedInstanceState.getInt("currentItemId", -1)
        currentItemType = savedInstanceState.getString("currentItemType", "")
        isCreatingNewItem = savedInstanceState.getBoolean("isCreatingNewItem", false)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentItemId", currentItemId)
        outState.putString("currentItemType", currentItemType)
        outState.putBoolean("isCreatingNewItem", isCreatingNewItem)
    }

    fun clearDetailFragment() {
        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view2)
        if (existingFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(existingFragment)
                .commitNow()
        }
        binding.fragmentContainerView2.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (isLandscape && binding.fragmentContainerView2.visibility == View.VISIBLE) {
            clearDetailFragment()
        } else {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                super.onBackPressed()
            }
        }
    }
}