package com.education.financetrackerrublik.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.data.model.Category
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.databinding.FragmentManageCategoriesBinding
import com.education.financetrackerrublik.ui.adapter.CategoryEditAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class ManageCategoriesFragment : Fragment() {
    private var _binding: FragmentManageCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageCategoriesViewModel by viewModels()
    private var currentType = TransactionType.EXPENSE

    private val adapter = CategoryEditAdapter(
        onEditClick = { category -> showEditCategoryDialog(category) },
        onDeleteClick = { category -> showDeleteCategoryDialog(category) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
        viewModel.loadCategories(currentType)
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.categoriesList.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentType = if (tab?.position == 0) {
                    TransactionType.EXPENSE
                } else {
                    TransactionType.INCOME
                }
                viewModel.loadCategories(currentType)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)
        }

        viewModel.canDelete.observe(viewLifecycleOwner) { canDelete ->
            if (!canDelete) {
                Toast.makeText(
                    requireContext(),
                    "Нельзя удалить категорию, которая используется в транзакциях",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showAddCategoryDialog() {
        CategoryEditDialog(
            context = requireContext(),
            onSave = { name, iconResId ->
                viewModel.addCategory(name, iconResId, currentType)
            }
        ).show()
    }

    private fun showEditCategoryDialog(category: Category) {
        CategoryEditDialog(
            context = requireContext(),
            category = category,
            onSave = { name, iconResId ->
                viewModel.updateCategory(category, name, iconResId)
            }
        ).show()
    }

    private fun showDeleteCategoryDialog(category: Category) {
        viewModel.checkCanDeleteCategory(category)
        viewModel.canDelete.observe(viewLifecycleOwner) { canDelete ->
            if (canDelete) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Удаление категории")
                    .setMessage("Вы действительно хотите удалить категорию ${category.name}?")
                    .setPositiveButton("Удалить") { _, _ ->
                        viewModel.deleteCategory(category)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 