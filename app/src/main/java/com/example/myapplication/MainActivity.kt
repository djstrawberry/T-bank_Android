package com.example.myapplication

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isLandscape: Boolean = false
    private var currentItemId: Int = -1
    private var currentItemType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (savedInstanceState == null) {
            showLibraryFragment()
        } else {
            currentItemId = savedInstanceState.getInt("currentItemId", -1)
            currentItemType = savedInstanceState.getString("currentItemType", "")
        }
    }

    fun showItemFragment(itemId: Int, itemType: String) {

        currentItemId = itemId
        currentItemType = itemType

        if (isLandscape) {
            binding.fragmentContainerView2.visibility = View.VISIBLE
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container_view2,
                    ItemFragment.newInstance(false, itemId, itemType)
                )
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view1, ItemFragment.newInstance(
                    isEditMode = false,
                    itemId = itemId,
                    itemType = itemType
                ))
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentItemId", currentItemId)
        outState.putString("currentItemType", currentItemType)
    }

    fun showCreateFragment() {
        binding.fragmentContainerView2.visibility = View.VISIBLE
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view2, ItemFragment.newInstance(isEditMode = true))
            .addToBackStack(null)
            .commit()
    }

    private fun showLibraryFragment() {
        val fragment = LibraryFragment()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view1, fragment)
            .commit()
        println("MAIN_DEBUG: Fragment transaction committed")
    }

    override fun onBackPressed() {
        if (binding.fragmentContainerView2.visibility == View.VISIBLE) {
            binding.fragmentContainerView2.visibility = View.GONE
        }
            super.onBackPressed()
    }
}