package com.education.financetrackerrublik.ui.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.education.financetrackerrublik.databinding.FragmentAddTransactionBinding
import com.education.financetrackerrublik.ui.adapter.CategoryAdapter
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels()
    private var selectedDate = Calendar.getInstance()
    private var selectedCategory: Category? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
        viewModel.loadCategories(TransactionType.EXPENSE)
    }

    private fun setupViews() {
        binding.typeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val type = if (tab?.position == 0) TransactionType.EXPENSE else TransactionType.INCOME
                viewModel.loadCategories(type)
                selectedCategory = null
                binding.categoryEdit.setText("")
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.dateEdit.setOnClickListener {
            showDateTimePicker()
        }

        binding.saveButton.setOnClickListener {
            saveTransaction()
        }

        updateDateField()
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val adapter = CategoryAdapter(requireContext(), categories)
            binding.categoryEdit.setAdapter(adapter)
            binding.categoryEdit.setOnItemClickListener { _, _, position, _ ->
                selectedCategory = categories[position]
                binding.categoryEdit.setText(selectedCategory?.name)
            }
        }

        viewModel.transactionSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                findNavController().navigateUp()
            }
        }
    }

    private fun showDateTimePicker() {
        val currentDate = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        selectedDate.set(Calendar.HOUR_OF_DAY, hour)
                        selectedDate.set(Calendar.MINUTE, minute)
                        updateDateField()
                    },
                    currentDate.get(Calendar.HOUR_OF_DAY),
                    currentDate.get(Calendar.MINUTE),
                    true
                ).show()
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateField() {
        binding.dateEdit.setText(dateFormat.format(selectedDate.time))
    }

    private fun saveTransaction() {
        val amount = binding.amountEdit.text.toString().toDoubleOrNull()
        val description = binding.descriptionEdit.text.toString()
        val type = if (binding.typeTabs.selectedTabPosition == 0) {
            TransactionType.EXPENSE
        } else {
            TransactionType.INCOME
        }

        if (amount == null) {
            Toast.makeText(requireContext(), "Введите сумму", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Выберите категорию", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveTransaction(
            amount = amount,
            type = type,
            categoryId = selectedCategory!!.id,
            description = description,
            date = selectedDate.time
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 