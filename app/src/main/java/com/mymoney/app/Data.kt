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
@Dao
interface MoneyDao {
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
@Database(entities = [Account::class, PaymentMethod::class, MoneyTransaction::class], version = 1, exportSchema = false)
abstract class MoneyDatabase: RoomDatabase() { abstract fun dao(): MoneyDao }
