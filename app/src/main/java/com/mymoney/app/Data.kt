package com.mymoney.app

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val openingPaise: Long = 0,
    val creditLimitPaise: Long = 0,
    val statementDay: Int = 1,
    val dueDay: Int = 1
)
@Entity(tableName = "payment_methods")
data class PaymentMethod(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val details: String = ""
)
@Entity(tableName = "transactions")
data class MoneyTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val amountPaise: Long,
    val category: String,
    val accountId: Long,
    val paymentMethodId: Long? = null,
    val date: String,
    val note: String = ""
)
@Entity(tableName = "finance_plans")
data class FinancePlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val name: String,
    val amountPaise: Long,
    val progressPaise: Long = 0,
    val day: Int = 1,
    val months: Int = 1
)
@Dao
interface MoneyDao {
    @Query("SELECT * FROM finance_plans ORDER BY id DESC") fun plans(): Flow<List<FinancePlan>>
    @Insert suspend fun addPlan(item: FinancePlan)
    @Update suspend fun updatePlan(item: FinancePlan)
    @Delete suspend fun deletePlan(item: FinancePlan)
    @Query("SELECT * FROM accounts ORDER BY id DESC") fun accounts(): Flow<List<Account>>
    @Query("SELECT * FROM payment_methods ORDER BY id DESC") fun methods(): Flow<List<PaymentMethod>>
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC") fun transactions(): Flow<List<MoneyTransaction>>
    @Insert suspend fun addAccount(item: Account)
    @Update suspend fun updateAccount(item: Account)
    @Delete suspend fun deleteAccount(item: Account)
    @Insert suspend fun addMethod(item: PaymentMethod)
    @Update suspend fun updateMethod(item: PaymentMethod)
    @Delete suspend fun deleteMethod(item: PaymentMethod)
    @Insert suspend fun addTransaction(item: MoneyTransaction)
    @Update suspend fun updateTransaction(item: MoneyTransaction)
    @Delete suspend fun deleteTransaction(item: MoneyTransaction)
    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :id") suspend fun accountUsage(id: Long): Int
    @Query("SELECT COUNT(*) FROM transactions WHERE paymentMethodId = :id") suspend fun methodUsage(id: Long): Int
}
@Database(entities = [Account::class, PaymentMethod::class, MoneyTransaction::class, FinancePlan::class], version = 2, exportSchema = false)
abstract class MoneyDatabase: RoomDatabase() { abstract fun dao(): MoneyDao }

val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS finance_plans (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, kind TEXT NOT NULL, name TEXT NOT NULL, amountPaise INTEGER NOT NULL, progressPaise INTEGER NOT NULL, day INTEGER NOT NULL, months INTEGER NOT NULL)")
    }
}
