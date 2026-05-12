package com.example.drycleaning.data.repository

import com.example.drycleaning.data.database.dao.ClientDao
import com.example.drycleaning.data.entity.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с клиентами.
 * Предоставляет CRUD-операции и поиск.
 */
@Singleton
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao
) {
    fun getAllClients(): Flow<List<Client>> = clientDao.getAllClients()

    fun searchClients(query: String): Flow<List<Client>> = clientDao.searchClients(query)

    fun getClientCount(): Flow<Int> = clientDao.getClientCount()

    fun getClientByIdFlow(id: Long): Flow<Client?> = clientDao.getClientByIdFlow(id)

    suspend fun getClientById(id: Long): Client? = clientDao.getClientById(id)

    suspend fun insertClient(client: Client): Long = clientDao.insert(client)

    suspend fun updateClient(client: Client) = clientDao.update(client)

    suspend fun deleteClient(client: Client) = clientDao.delete(client)
}
