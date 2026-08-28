package com.personal.thesystem.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.personal.thesystem.model.AssignmentPriority
import com.personal.thesystem.model.DailyRecord
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.DietViolationReason
import com.personal.thesystem.model.ExperimentFeedback
import com.personal.thesystem.model.MoneyCategory
import com.personal.thesystem.model.MoneyCommitment
import com.personal.thesystem.model.MoneyTransaction
import com.personal.thesystem.model.SleepViolationPart
import com.personal.thesystem.model.StudyAssignment
import com.personal.thesystem.model.ViolationReason
import java.time.LocalDate

internal class SystemDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE daily_records (
                date TEXT PRIMARY KEY,
                sleep TEXT, morning TEXT, light TEXT, diet TEXT, water TEXT,
                water_quarters INTEGER, sleep_reason TEXT, morning_reason TEXT, diet_reason TEXT,
                sleep_part TEXT, morning_reps INTEGER
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE assignments (
                id INTEGER PRIMARY KEY, title TEXT NOT NULL, subject TEXT NOT NULL,
                due_date TEXT NOT NULL, priority TEXT NOT NULL, completed INTEGER NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE money_transactions (
                id INTEGER PRIMARY KEY, date TEXT NOT NULL, amount INTEGER NOT NULL,
                category TEXT NOT NULL, planned INTEGER NOT NULL
            )""".trimIndent()
        )
        db.execSQL("CREATE TABLE money_transfers (period_start TEXT PRIMARY KEY)")
        db.execSQL("CREATE TABLE experiment_feedback (week_start TEXT PRIMARY KEY, feedback TEXT NOT NULL)")
        db.execSQL(
            """CREATE TABLE money_commitments (
                id INTEGER PRIMARY KEY, title TEXT NOT NULL, amount INTEGER NOT NULL, category TEXT NOT NULL
            )""".trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun loadRecords(): Map<LocalDate, DailyRecord> = readableDatabase.query(
        "daily_records", null, null, null, null, null, "date ASC"
    ).use { cursor ->
        buildMap {
            while (cursor.moveToNext()) {
                runCatching {
                    val date = LocalDate.parse(cursor.string("date"))
                    put(
                        date,
                        DailyRecord(
                            date = date,
                            sleep = cursor.enumOrNull<DecisionStatus>("sleep"),
                            morning = cursor.enumOrNull<DecisionStatus>("morning"),
                            light = cursor.enumOrNull<DecisionStatus>("light"),
                            diet = cursor.enumOrNull<DecisionStatus>("diet"),
                            water = cursor.enumOrNull<DecisionStatus>("water"),
                            waterQuarterLiters = cursor.intOrNull("water_quarters"),
                            sleepReason = ViolationReason.fromId(cursor.stringOrNull("sleep_reason")),
                            morningReason = ViolationReason.fromId(cursor.stringOrNull("morning_reason")),
                            dietReason = DietViolationReason.fromId(cursor.stringOrNull("diet_reason")),
                            sleepViolationPart = cursor.enumOrNull<SleepViolationPart>("sleep_part"),
                            morningRepetitions = cursor.intOrNull("morning_reps"),
                        )
                    )
                }
            }
        }
    }

    fun saveRecord(record: DailyRecord) {
        writableDatabase.insertWithOnConflict(
            "daily_records",
            null,
            ContentValues().apply {
                put("date", record.date.toString())
                putNullable("sleep", record.sleep?.name)
                putNullable("morning", record.morning?.name)
                putNullable("light", record.light?.name)
                putNullable("diet", record.diet?.name)
                putNullable("water", record.water?.name)
                putNullable("water_quarters", record.waterQuarterLiters)
                putNullable("sleep_reason", record.sleepReason?.id)
                putNullable("morning_reason", record.morningReason?.id)
                putNullable("diet_reason", record.dietReason?.id)
                putNullable("sleep_part", record.sleepViolationPart?.name)
                putNullable("morning_reps", record.morningRepetitions)
            },
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun deleteRecord(date: LocalDate) {
        writableDatabase.delete("daily_records", "date = ?", arrayOf(date.toString()))
    }

    fun loadAssignments(): List<StudyAssignment> = readableDatabase.query(
        "assignments", null, null, null, null, null, "completed ASC, due_date ASC, priority DESC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) runCatching {
                add(
                    StudyAssignment(
                        id = cursor.long("id"),
                        title = cursor.string("title"),
                        subject = cursor.string("subject"),
                        dueDate = LocalDate.parse(cursor.string("due_date")),
                        priority = cursor.enumOrNull<AssignmentPriority>("priority") ?: AssignmentPriority.NORMAL,
                        completed = cursor.int("completed") == 1,
                    )
                )
            }
        }
    }

    fun saveAssignment(assignment: StudyAssignment) {
        writableDatabase.insertWithOnConflict(
            "assignments", null, ContentValues().apply {
                put("id", assignment.id)
                put("title", assignment.title)
                put("subject", assignment.subject)
                put("due_date", assignment.dueDate.toString())
                put("priority", assignment.priority.name)
                put("completed", if (assignment.completed) 1 else 0)
            }, SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun deleteAssignment(id: Long) {
        writableDatabase.delete("assignments", "id = ?", arrayOf(id.toString()))
    }

    fun loadMoneyTransactions(): List<MoneyTransaction> = readableDatabase.query(
        "money_transactions", null, null, null, null, null, "date DESC, id DESC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) runCatching {
                add(
                    MoneyTransaction(
                        id = cursor.long("id"),
                        date = LocalDate.parse(cursor.string("date")),
                        amountRubles = cursor.long("amount"),
                        category = MoneyCategory.fromId(cursor.string("category")),
                        planned = cursor.int("planned") == 1,
                    )
                )
            }
        }
    }

    fun saveMoneyTransaction(transaction: MoneyTransaction) {
        writableDatabase.insertWithOnConflict(
            "money_transactions", null, ContentValues().apply {
                put("id", transaction.id)
                put("date", transaction.date.toString())
                put("amount", transaction.amountRubles)
                put("category", transaction.category.id)
                put("planned", if (transaction.planned) 1 else 0)
            }, SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun deleteMoneyTransaction(id: Long) {
        writableDatabase.delete("money_transactions", "id = ?", arrayOf(id.toString()))
    }

    fun loadMoneyTransfers(): Set<LocalDate> = readableDatabase.query(
        "money_transfers", arrayOf("period_start"), null, null, null, null, null
    ).use { cursor ->
        buildSet {
            while (cursor.moveToNext()) runCatching { add(LocalDate.parse(cursor.string("period_start"))) }
        }
    }

    fun setMoneyTransfer(periodStart: LocalDate, received: Boolean) {
        if (received) {
            writableDatabase.insertWithOnConflict(
                "money_transfers", null, ContentValues().apply { put("period_start", periodStart.toString()) },
                SQLiteDatabase.CONFLICT_IGNORE,
            )
        } else {
            writableDatabase.delete("money_transfers", "period_start = ?", arrayOf(periodStart.toString()))
        }
    }

    fun loadExperimentFeedback(): Map<LocalDate, ExperimentFeedback> = readableDatabase.query(
        "experiment_feedback", null, null, null, null, null, null
    ).use { cursor ->
        buildMap {
            while (cursor.moveToNext()) runCatching {
                put(LocalDate.parse(cursor.string("week_start")), ExperimentFeedback.valueOf(cursor.string("feedback")))
            }
        }
    }

    fun saveExperimentFeedback(weekStart: LocalDate, feedback: ExperimentFeedback) {
        writableDatabase.insertWithOnConflict(
            "experiment_feedback", null, ContentValues().apply {
                put("week_start", weekStart.toString())
                put("feedback", feedback.name)
            }, SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun loadMoneyCommitments(): List<MoneyCommitment> = readableDatabase.query(
        "money_commitments", null, null, null, null, null, "amount DESC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) runCatching {
                add(
                    MoneyCommitment(
                        id = cursor.long("id"),
                        title = cursor.string("title"),
                        amountRubles = cursor.long("amount"),
                        category = MoneyCategory.fromId(cursor.string("category")),
                    )
                )
            }
        }
    }

    fun saveMoneyCommitment(commitment: MoneyCommitment) {
        writableDatabase.insertWithOnConflict(
            "money_commitments", null, ContentValues().apply {
                put("id", commitment.id)
                put("title", commitment.title)
                put("amount", commitment.amountRubles)
                put("category", commitment.category.id)
            }, SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun deleteMoneyCommitment(id: Long) {
        writableDatabase.delete("money_commitments", "id = ?", arrayOf(id.toString()))
    }

    fun clearUserData() {
        writableDatabase.beginTransaction()
        try {
            listOf(
                "daily_records", "assignments", "money_transactions", "money_transfers",
                "experiment_feedback", "money_commitments",
            ).forEach { writableDatabase.delete(it, null, null) }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    private fun android.database.Cursor.string(name: String): String = getString(getColumnIndexOrThrow(name))
    private fun android.database.Cursor.stringOrNull(name: String): String? =
        getColumnIndexOrThrow(name).let { index -> if (isNull(index)) null else getString(index) }
    private fun android.database.Cursor.int(name: String): Int = getInt(getColumnIndexOrThrow(name))
    private fun android.database.Cursor.long(name: String): Long = getLong(getColumnIndexOrThrow(name))
    private fun android.database.Cursor.intOrNull(name: String): Int? =
        getColumnIndexOrThrow(name).let { index -> if (isNull(index)) null else getInt(index) }
    private inline fun <reified T : Enum<T>> android.database.Cursor.enumOrNull(name: String): T? =
        stringOrNull(name)?.let { value -> enumValues<T>().firstOrNull { it.name == value } }

    private fun ContentValues.putNullable(key: String, value: String?) {
        if (value == null) putNull(key) else put(key, value)
    }
    private fun ContentValues.putNullable(key: String, value: Int?) {
        if (value == null) putNull(key) else put(key, value)
    }

    private companion object {
        const val DATABASE_NAME = "the_system.db"
        const val DATABASE_VERSION = 1
    }
}
