package com.example.drycleaning.ui.clients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.drycleaning.data.entity.Client
import com.example.drycleaning.databinding.FragmentCreateClientBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент создания / редактирования клиента */
@AndroidEntryPoint
class CreateClientFragment : Fragment() {

    private var _binding: FragmentCreateClientBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClientsViewModel by viewModels()
    private val args: CreateClientFragmentArgs by navArgs()
    private var editingClient: Client? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadClientIfEditing()

        binding.btnSave.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val notes = binding.etNotes.text.toString().trim()

            if (fullName.isBlank() || phone.isBlank()) {
                Snackbar.make(binding.root, "Заполните ФИО и телефон", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val client = Client(
                id = editingClient?.id ?: 0,
                fullName = fullName,
                phone = phone,
                email = email,
                address = address,
                notes = notes,
                createdAt = editingClient?.createdAt ?: System.currentTimeMillis()
            )

            if (editingClient != null) {
                viewModel.updateClient(client)
            } else {
                viewModel.saveClient(client)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    result?.let {
                        it.onSuccess {
                            Snackbar.make(binding.root, "Клиент сохранён", Snackbar.LENGTH_SHORT).show()
                            viewModel.resetSaveResult()
                            findNavController().popBackStack()
                        }
                        it.onFailure { e ->
                            Snackbar.make(binding.root, "Ошибка: ${e.message}", Snackbar.LENGTH_SHORT).show()
                            viewModel.resetSaveResult()
                        }
                    }
                }
            }
        }
    }

    private fun loadClientIfEditing() {
        val clientId = args.clientId
        if (clientId > 0) {
            binding.tvTitle.text = "Редактирование клиента"
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getClientByIdFlow(clientId).collect { client ->
                    client?.let {
                        editingClient = it
                        binding.etFullName.setText(it.fullName)
                        binding.etPhone.setText(it.phone)
                        binding.etEmail.setText(it.email)
                        binding.etAddress.setText(it.address)
                        binding.etNotes.setText(it.notes)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
