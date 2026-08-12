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

/**
 * Adds three independent, additive pieces of schema in one release: a new
 * category_budgets table for per-category sub-budgets, a tags column on
 * transactions, and a biometricLockEnabled flag on app_settings. None of
 * these touch existing columns, so no data conversion is needed.
 */
val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE category_budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                budgetId INTEGER NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                notifiedExceeded INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("ALTER TABLE transactions ADD COLUMN tags TEXT")
        db.execSQL("ALTER TABLE app_settings ADD COLUMN biometricLockEnabled INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Adds the account-nickname table (multi-account tracking) and a
 * receiptPhotoPath column on transactions (OCR receipt scanning). Both
 * additive, no data conversion needed.
 */
val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE account_nicknames (
                senderPattern TEXT NOT NULL,
                displayName TEXT NOT NULL,
                PRIMARY KEY(senderPattern)
            )
            """.trimIndent()
        )
        db.execSQL("ALTER TABLE transactions ADD COLUMN receiptPhotoPath TEXT")
    }
}

/**
 * Adds two Panic Button / Month-end celebration counters on app_settings.
 * Both additive, no data conversion needed.
 */
val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE app_settings ADD COLUMN impulsesAvoided INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE app_settings ADD COLUMN lastCelebratedBudgetId INTEGER NOT NULL DEFAULT 0")
    }
}

/** Adds the voice-roasts opt-in flag on app_settings. Additive, no data conversion needed. */
val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE app_settings ADD COLUMN voiceRoastsEnabled INTEGER NOT NULL DEFAULT 0")
    }
}

/** Adds the Split Groups feature — persisted shared-expense groups and their expenses. */
val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE split_groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                participants TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE split_expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                groupId INTEGER NOT NULL,
                description TEXT NOT NULL,
                amount REAL NOT NULL,
                paidBy TEXT NOT NULL,
                splitAmong TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

/** Adds the Bhai Call opt-in flag on app_settings. Additive, no data conversion needed. */
val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE app_settings ADD COLUMN bhaiCallEnabled INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        AppSettingsEntity::class,
        CategoryBudgetEntity::class,
        AccountNicknameEntity::class,
        SplitGroupEntity::class,
        SplitExpenseEntity::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun accountNicknameDao(): AccountNicknameDao
    abstract fun splitDao(): SplitDao

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
                    .addMigrations(
                        MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                        MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11
                    )
                    // Safety net only for schema jumps with no migration path
                    // (e.g. very old pre-release installs) — real installs from
                    // here on go through explicit migrations so data survives.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
