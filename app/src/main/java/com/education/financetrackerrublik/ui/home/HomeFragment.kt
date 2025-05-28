package com.education.financetrackerrublik.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.databinding.FragmentHomeBinding
import com.education.financetrackerrublik.ui.adapter.TransactionsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var transactionAdapter: TransactionsAdapter
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeViewModel()
        viewModel.loadData()
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

        binding.transactionsList.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFab() {
        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addTransaction)
        }
    }

    private fun observeViewModel() {
        viewModel.todayTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
            binding.emptyView.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.monthlyIncome.observe(viewLifecycleOwner) { income ->
            binding.incomeAmount.text = numberFormat.format(income)
        }

        viewModel.monthlyExpense.observe(viewLifecycleOwner) { expense ->
            binding.expenseAmount.text = numberFormat.format(expense)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 