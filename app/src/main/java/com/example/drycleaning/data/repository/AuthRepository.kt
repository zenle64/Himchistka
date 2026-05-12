package com.example.drycleaning.data.repository

import com.example.drycleaning.data.database.dao.UserDao
import com.example.drycleaning.data.datastore.SessionManager
import com.example.drycleaning.data.entity.User
import com.example.drycleaning.data.entity.UserRole
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий авторизации.
 * Управляет аутентификацией и сессией пользователя.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val isLoggedIn = sessionManager.isLoggedIn
    val currentUserRole = sessionManager.role
    val currentUserName = sessionManager.fullName

    suspend fun login(username: String, password: String): Result<User> {
        val user = userDao.authenticate(username, password)
            ?: return Result.failure(Exception("Неверный логин или пароль"))
        sessionManager.saveSession(user.id, user.username, user.fullName, user.role.name)
        return Result.success(user)
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun ensureDefaultUsers() {
        if (userDao.getUserCount() == 0) {
            userDao.insert(
                User(
                    username = "admin",
                    password = "admin123",
                    fullName = "Администратор",
                    role = UserRole.ADMIN
                )
            )
            userDao.insert(
                User(
                    username = "manager",
                    password = "manager123",
                    fullName = "Менеджер Иванов",
                    role = UserRole.MANAGER
                )
            )
        }
    }
}
