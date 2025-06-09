package com.education.financetrackerrublik.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTotalAmounts()
    }

    private fun setupViews() {
        binding.darkThemeSwitch.apply {
            isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            setOnCheckedChangeListener { _, isChecked ->
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        binding.manageCategories.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_categories)
        }
    }

    private fun observeViewModel() {
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.totalIncome.text = income
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.totalExpense.text = expense
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 