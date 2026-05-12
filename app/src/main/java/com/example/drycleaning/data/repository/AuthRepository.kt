package com.example.drycleaning.data.repository

import com.example.drycleaning.data.database.dao.UserDao
import com.example.drycleaning.data.datastore.SessionManager
import com.example.drycleaning.data.entity.User
import com.example.drycleaning.data.entity.UserRole
import com.example.drycleaning.util.PasswordUtils
import javax.inject.Inject
import javax.inject.Singleton

/** Репозиторий авторизации с хешированием паролей */
@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val isLoggedIn = sessionManager.isLoggedIn
    val currentUserRole = sessionManager.role
    val currentUserName = sessionManager.fullName

    suspend fun login(username: String, password: String): Result<User> {
        val hashedPassword = PasswordUtils.hash(password)
        val user = userDao.authenticate(username, hashedPassword)
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
                    password = PasswordUtils.hash("admin123"),
                    fullName = "Администратор",
                    role = UserRole.ADMIN
                )
            )
            userDao.insert(
                User(
                    username = "manager",
                    password = PasswordUtils.hash("manager123"),
                    fullName = "Менеджер Иванов",
                    role = UserRole.MANAGER
                )
            )
        } else {
            migratePasswordsIfNeeded()
        }
    }

    private suspend fun migratePasswordsIfNeeded() {
        val admin = userDao.findByUsername("admin")
        if (admin != null && admin.password == "admin123") {
            userDao.updatePassword(admin.id, PasswordUtils.hash("admin123"))
        }
        val manager = userDao.findByUsername("manager")
        if (manager != null && manager.password == "manager123") {
            userDao.updatePassword(manager.id, PasswordUtils.hash("manager123"))
        }
    }
}
