package com.education.financetrackerrublik.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.databinding.FragmentTransactionsBinding
import com.education.financetrackerrublik.ui.adapter.TransactionsAdapter
import com.education.financetrackerrublik.ui.adapter.TransactionListItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    private lateinit var transactionAdapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterButton()
        setupPeriodText()
        observeViewModel()

        viewModel.loadTransactions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionsAdapter { transactionWithCategory ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Удаление транзакции")
                .setMessage("Вы действительно хотите удалить эту транзакцию?")
                .setPositiveButton("Удалить") { _, _ ->
                    viewModel.deleteTransaction(transactionWithCategory)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
        binding.transactionsList.adapter = transactionAdapter
    }

    private fun setupFilterButton() {
        binding.filterButton.setOnClickListener { view ->
            showFilterMenu(view)
        }
    }

    private fun setupPeriodText() {
        binding.periodText.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showFilterMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(R.menu.transactions_filter_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.filter_all -> {
                        viewModel.setTransactionType(null)
                        true
                    }
                    R.id.filter_expense -> {
                        viewModel.setTransactionType(TransactionType.EXPENSE)
                        true
                    }
                    R.id.filter_income -> {
                        viewModel.setTransactionType(TransactionType.INCOME)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите период")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selection.first
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selection.second
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            updateDateRange(startDate.time, endDate.time)
        }

        dateRangePicker.show(parentFragmentManager, "date_range_picker")
    }

    private fun updateDateRange(startDate: Date, endDate: Date) {
        binding.periodText.text = getString(
            R.string.date_range_format,
            dateFormat.format(startDate),
            dateFormat.format(endDate)
        )
        viewModel.setDateRange(startDate, endDate)
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
            binding.emptyView.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 