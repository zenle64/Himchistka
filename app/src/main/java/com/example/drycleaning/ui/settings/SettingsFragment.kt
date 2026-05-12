package com.example.drycleaning.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.drycleaning.R
import com.example.drycleaning.data.entity.ServicePrice
import com.example.drycleaning.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент настроек приложения */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    /** Флаг для предотвращения зацикливания при программной установке RadioButton */
    private var isUpdatingThemeUI = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupThemeSelector()
        setupPriceList()
        setupBackup()
        setupLogout()
    }

    private fun setupThemeSelector() {
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingThemeUI) return@setOnCheckedChangeListener

            val theme = when (checkedId) {
                R.id.rbLight -> "light"
                R.id.rbDark -> "dark"
                else -> "system"
            }
            viewModel.setTheme(theme)

            val mode = when (theme) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            if (AppCompatDelegate.getDefaultNightMode() != mode) {
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.theme.collect { theme ->
                    isUpdatingThemeUI = true
                    when (theme) {
                        "light" -> binding.rbLight.isChecked = true
                        "dark" -> binding.rbDark.isChecked = true
                        else -> binding.rbSystem.isChecked = true
                    }
                    isUpdatingThemeUI = false
                }
            }
        }
    }

    private fun setupPriceList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prices.collect { prices ->
                    binding.llPrices.removeAllViews()
                    for (price in prices) {
                        val itemView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_price_setting, binding.llPrices, false)
                        val tvName = itemView.findViewById<android.widget.TextView>(R.id.tvPriceName)
                        val tvPrice = itemView.findViewById<android.widget.TextView>(R.id.tvPriceValue)
                        tvName.text = "${price.itemType} — ${price.serviceType}"
                        tvPrice.text = "${price.price.toInt()} ₽"
                        itemView.setOnClickListener { showEditPriceDialog(price) }
                        binding.llPrices.addView(itemView)
                    }
                }
            }
        }
    }

    private fun showEditPriceDialog(price: ServicePrice) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }
        val etPrice = TextInputEditText(requireContext()).apply {
            hint = "Цена"
            setText(price.price.toInt().toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(etPrice)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${price.itemType} — ${price.serviceType}")
            .setView(layout)
            .setPositiveButton("Сохранить") { _, _ ->
                val newPrice = etPrice.text.toString().toDoubleOrNull() ?: price.price
                viewModel.updatePrice(price.copy(price = newPrice))
                Snackbar.make(binding.root, "Цена обновлена", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun setupBackup() {
        binding.btnBackup.setOnClickListener {
            viewModel.backupDatabase(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.backupResult.collect { result ->
                    result?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.resetBackupResult()
                    }
                }
            }
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Выход")
                .setMessage("Выйти из системы?")
                .setPositiveButton("Выйти") { _, _ ->
                    viewModel.logout()
                    findNavController().navigate(R.id.action_settings_to_login)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
