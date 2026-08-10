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

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class, AppSettingsEntity::class],
    version = 4,
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    // Safety net only for schema jumps with no migration path
                    // (e.g. very old pre-release installs) — real installs from
                    // here on go through explicit migrations so data survives.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
