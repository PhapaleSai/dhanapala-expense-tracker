package com.phapalesai.dhanapala.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun fromType(type: TransactionType): String = type.name

    @TypeConverter
    fun toType(value: String): TransactionType = TransactionType.valueOf(value)
}

val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE app_settings ADD COLUMN roastLevel TEXT NOT NULL DEFAULT 'MEDIUM'")
        db.execSQL("ALTER TABLE app_settings ADD COLUMN roastLanguage TEXT NOT NULL DEFAULT 'HI'")
    }
}

val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE app_settings ADD COLUMN userName TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE transactions SET category = 'Anonymous Expenses' WHERE category = 'Uncategorized'")
    }
}

/**
 * Budgets move from being keyed by calendar month ("2026-08") to an explicit
 * start/end date range, so the user can set a budget for any custom period.
 * Existing month-keyed rows are converted to that month's first/last day so
 * no budget data is lost.
 */
val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE budgets_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                startDateMillis INTEGER NOT NULL,
                endDateMillis INTEGER NOT NULL,
                amount REAL NOT NULL,
                notified80 INTEGER NOT NULL DEFAULT 0,
                notifiedExceeded INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        val zone = java.time.ZoneId.systemDefault()
        db.query("SELECT month, amount, notified80, notifiedExceeded FROM budgets").use { cursor ->
            while (cursor.moveToNext()) {
                val month = cursor.getString(0)
                val amount = cursor.getDouble(1)
                val notified80 = cursor.getInt(2)
                val notifiedExceeded = cursor.getInt(3)

                val yearMonth = java.time.YearMonth.parse(month)
                val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

                db.execSQL(
                    "INSERT INTO budgets_new (startDateMillis, endDateMillis, amount, notified80, notifiedExceeded) VALUES (?, ?, ?, ?, ?)",
                    arrayOf<Any>(start, end, amount, notified80, notifiedExceeded)
                )
            }
        }

        db.execSQL("DROP TABLE budgets")
        db.execSQL("ALTER TABLE budgets_new RENAME TO budgets")
    }
}

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class, AppSettingsEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dhanapala.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    // Safety net only for schema jumps with no migration path
                    // (e.g. very old pre-release installs) — real installs from
                    // here on go through explicit migrations so data survives.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
