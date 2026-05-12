package com.example.drycleaning.ui.clients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drycleaning.R
import com.example.drycleaning.databinding.FragmentClientsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Фрагмент списка клиентов */
@AndroidEntryPoint
class ClientsFragment : Fragment() {

    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClientsViewModel by viewModels()
    private lateinit var adapter: ClientAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFab()
        observeClients()
    }

    private fun setupRecyclerView() {
        adapter = ClientAdapter { client ->
            val action = ClientsFragmentDirections.actionClientsToClientDetail(client.id)
            findNavController().navigate(action)
        }
        binding.rvClients.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClients.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }
    }

    private fun setupFab() {
        binding.fabNewClient.setOnClickListener {
            findNavController().navigate(R.id.action_clients_to_createClient)
        }
    }

    private fun observeClients() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.clients.collect { clients ->
                    adapter.submitList(clients)
                    binding.tvEmpty.visibility = if (clients.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
