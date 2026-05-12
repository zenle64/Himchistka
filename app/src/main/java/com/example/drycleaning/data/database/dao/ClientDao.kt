package com.example.drycleaning.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.drycleaning.data.entity.Client
import kotlinx.coroutines.flow.Flow

/** DAO для работы с клиентами */
@Dao
interface ClientDao {

    @Insert
    suspend fun insert(client: Client): Long

    @Update
    suspend fun update(client: Client)

    @Delete
    suspend fun delete(client: Client)

    @Query("SELECT * FROM clients ORDER BY fullName ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: Long): Client?

    @Query("SELECT * FROM clients WHERE id = :id")
    fun getClientByIdFlow(id: Long): Flow<Client?>

    @Query("SELECT * FROM clients WHERE phone LIKE '%' || :query || '%' OR fullName LIKE '%' || :query || '%'")
    fun searchClients(query: String): Flow<List<Client>>

    @Query("SELECT COUNT(*) FROM clients")
    fun getClientCount(): Flow<Int>
}
