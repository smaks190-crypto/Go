package com.example.data.repository

import android.content.Context
import com.example.data.db.AccountDao
import com.example.data.db.AccountEntity
import com.example.data.db.BudgetProfileEntity
import com.example.data.db.CategoryDao
import com.example.data.db.CategoryEntity
import com.example.data.db.GoalDao
import com.example.data.db.GoalEntity
import com.example.data.db.TransactionDao
import com.example.data.db.TransactionEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BudgetBackup(
    val transactions: List<TransactionEntity>? = emptyList(),
    val goals: List<GoalEntity>? = emptyList(),
    val categories: List<CategoryEntity>? = emptyList(),
    val accounts: List<AccountEntity>? = emptyList()
)

class BackupRepository(
    private val context: Context,
    private val transactionDao: TransactionDao,
    private val goalDao: GoalDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val createProfileDelegate: suspend (String) -> BudgetProfileEntity,
    private val clearAllDataDelegate: suspend () -> Unit
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val backupAdapter = moshi.adapter(BudgetBackup::class.java)
    val storageFile = File(context.filesDir, "budget_storage.json")

    suspend fun syncToFile() {
        try {
            val txs = transactionDao.getAllTransactions().first()
            val goals = goalDao.getAllGoals().first()
            val cats = categoryDao.getAllCategories().first()
            val accs = accountDao.getAllAccounts().first()
            val backup = BudgetBackup(txs, goals, cats, accs)
            val json = backupAdapter.toJson(backup)
            storageFile.writeText(json)
        } catch (_: Exception) {}
    }

    suspend fun exportJson(): String {
        val txs = transactionDao.getAllTransactions().first()
        val goals = goalDao.getAllGoals().first()
        val cats = categoryDao.getAllCategories().first()
        val accs = accountDao.getAllAccounts().first()
        val backup = BudgetBackup(txs, goals, cats, accs)
        val json = backupAdapter.toJson(backup)
        try {
            storageFile.writeText(json)
        } catch (_: Exception) {}
        return json
    }

    suspend fun exportJsonForBudget(budgetId: String): String {
        val txs = transactionDao.getTransactionsByBudgetId(budgetId).first()
        val goals = goalDao.getGoalsByBudgetId(budgetId).first()
        val cats = categoryDao.getCategoriesByBudgetId(budgetId).first()
        val accs = accountDao.getAccountsByBudgetId(budgetId).first()
        val backup = BudgetBackup(txs, goals, cats, accs)
        val json = backupAdapter.toJson(backup)
        try {
            val file = File(context.filesDir, "budget_${budgetId}.json")
            file.writeText(json)
        } catch (_: Exception) {}
        return json
    }

    suspend fun importBackupAsNewBudget(jsonStr: String, customName: String? = null): BudgetProfileEntity? {
        return try {
            val backup = backupAdapter.fromJson(jsonStr) ?: return null
            val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
            val name = customName ?: "Бюджет из копии ($dateStr)"
            val profile = createProfileDelegate(name)
            val newBudgetId = profile.id

            val rawTxs = backup.transactions ?: emptyList()
            val rawGoals = backup.goals ?: emptyList()
            val rawCats = backup.categories ?: emptyList()
            val rawAccs = backup.accounts ?: emptyList()

            val accountIdMap = mutableMapOf<String, String>()
            val accs = rawAccs.map { oldAcc ->
                val newId = UUID.randomUUID().toString()
                accountIdMap[oldAcc.id] = newId
                oldAcc.copy(id = newId, budgetId = newBudgetId)
            }

            val txs = rawTxs.map { oldTx ->
                val mappedAccountId = oldTx.accountId?.let { accountIdMap[it] } ?: oldTx.accountId
                oldTx.copy(
                    id = UUID.randomUUID().toString(),
                    budgetId = newBudgetId,
                    accountId = mappedAccountId
                )
            }
            val goals = rawGoals.map { it.copy(id = UUID.randomUUID().toString(), budgetId = newBudgetId) }
            val cats = rawCats.map { it.copy(id = UUID.randomUUID().toString(), budgetId = newBudgetId) }

            if (accs.isNotEmpty()) accountDao.insertAccounts(accs)
            if (txs.isNotEmpty()) transactionDao.insertTransactions(txs)
            if (goals.isNotEmpty()) goalDao.insertGoals(goals)
            if (cats.isNotEmpty()) categoryDao.insertCategories(cats)

            val file = File(context.filesDir, "budget_${newBudgetId}.json")
            val updatedBackup = BudgetBackup(txs, goals, cats, accs)
            file.writeText(backupAdapter.toJson(updatedBackup))
            profile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importJson(jsonStr: String, targetBudgetId: String? = null): Boolean {
        return try {
            val backup = backupAdapter.fromJson(jsonStr) ?: return false
            clearAllDataDelegate()

            val bId = targetBudgetId ?: "default"
            val rawTxs = backup.transactions ?: emptyList()
            val rawGoals = backup.goals ?: emptyList()
            val rawCats = backup.categories ?: emptyList()
            val rawAccs = backup.accounts ?: emptyList()

            val accs = rawAccs.map { if (it.budgetId.isBlank()) it.copy(budgetId = bId) else it }
            val txs = rawTxs.map { if (it.budgetId.isBlank()) it.copy(budgetId = bId) else it }
            val goals = rawGoals.map { if (it.budgetId.isBlank()) it.copy(budgetId = bId) else it }
            val cats = rawCats.map { if (it.budgetId.isBlank()) it.copy(budgetId = bId) else it }

            if (accs.isNotEmpty()) accountDao.insertAccounts(accs)
            if (txs.isNotEmpty()) transactionDao.insertTransactions(txs)
            if (goals.isNotEmpty()) goalDao.insertGoals(goals)
            if (cats.isNotEmpty()) categoryDao.insertCategories(cats)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
