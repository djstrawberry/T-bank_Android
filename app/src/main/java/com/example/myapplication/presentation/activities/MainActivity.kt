package com.example.myapplication.presentation.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.dao.GetDb
import com.example.myapplication.presentation.fragments.ItemFragment
import com.example.myapplication.presentation.fragments.LibraryFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.domain.repositories.LibraryRepository
import com.example.myapplication.domain.usecases.AddItemUseCase
import com.example.myapplication.domain.usecases.GetLibraryItemsUseCase
import com.example.myapplication.presentation.MyApplication
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var getLibraryItemsUseCase: GetLibraryItemsUseCase
    @Inject lateinit var addItemUseCase: AddItemUseCase
    @Inject
    lateinit var repository: LibraryRepository

    private lateinit var binding: ActivityMainBinding
    private var isLandscape: Boolean = false
    private var currentItemId: Int = -1
    private var currentItemType: String = ""
    private var isCreatingNewItem: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        val app = application as MyApplication

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (savedInstanceState == null) {
            setupInitialFragments()
        } else {
            restoreState(savedInstanceState)
            if (isLandscape && (currentItemId != -1 || isCreatingNewItem)) {
                showDetailFragment()
            }
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
                .replace(
                    R.id.fragment_container_view2,
                    ItemFragment.newInstance(true)
                )
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container_view2,
                    ItemFragment.newInstance(
                        false,
                        currentItemId,
                        currentItemType,
                    )
                )
                .commit()
        }
        binding.fragmentContainerView2.visibility = View.VISIBLE
    }

    fun showItemDetails(itemId: Int, itemType: String) {

        currentItemId = itemId
        currentItemType = itemType
        isCreatingNewItem = false

        val fragment = ItemFragment.newInstance(
            isEditMode = false,
            itemId = itemId,
            itemType = itemType,
        )

        if (isLandscape) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view2, fragment)
                .commit()
            binding.fragmentContainerView2.visibility = View.VISIBLE
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view1, fragment)
                .addToBackStack("details")
                .commit()
        }
    }

    fun showCreateItem() {
        currentItemId = -1
        currentItemType = ""
        isCreatingNewItem = true

        val fragment = ItemFragment.newInstance(isEditMode = true)

        if (isLandscape) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view2, fragment)
                .commit()
            binding.fragmentContainerView2.visibility = View.VISIBLE
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view1, fragment)
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

    private fun clearDetailFragment() {
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