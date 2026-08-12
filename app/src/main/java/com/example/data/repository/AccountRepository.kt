package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.db.AccountDao
import com.example.data.db.AccountEntity
import com.example.data.db.AppDatabase
import com.example.data.db.NotificationDao
import com.example.data.db.NotificationEntity
import com.example.utils.GlobalConsoleLogger
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class AccountRepository(
    private val accountDao: AccountDao,
    private val notificationDao: NotificationDao,
    private val db: AppDatabase
) {
    fun getAccountsForBudget(budgetId: String): Flow<List<AccountEntity>> =
        accountDao.getAccountsByBudgetId(budgetId)

    fun getNotificationsForBudget(budgetId: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsByBudgetId(budgetId)

    suspend fun insertAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun deleteAccountById(id: String) {
        accountDao.deleteAccountById(id)
    }

    suspend fun transferBetweenAccounts(
        budgetId: String,
        fromAccountId: String,
        toAccountId: String,
        amount: Double,
        fromName: String,
        toName: String
    ) {
        db.withTransaction {
            accountDao.updateBalance(fromAccountId, -amount)
            accountDao.updateBalance(toAccountId, amount)
            notificationDao.insertNotification(
                NotificationEntity(
                    budgetId = budgetId,
                    title = "Перевод между счетами",
                    description = "Переведено ${String.format(Locale.US, "%.0f", amount)} ₽ со счета \"$fromName\" на счет \"$toName\"",
                    icon = "repeat",
                    color = "indigo500",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun insertNotification(notification: NotificationEntity) {
        GlobalConsoleLogger.i("NOTIFICATION", "Создано новое уведомление: [${notification.title}] ${notification.description}")
        notificationDao.insertNotification(notification)
    }

    suspend fun markNotificationsAsRead(budgetId: String) {
        notificationDao.markAllAsRead(budgetId)
    }
}
