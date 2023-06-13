package com.codelytical.smartlogger

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ToDoManager(context: Context) {

	companion object {
		private const val DB_NAME = "todo.db"
		private const val DB_VERSION = 1
		private const val TABLE_NAME = "todos"
		private const val COLUMN_ID = "id"
		private const val COLUMN_TITLE = "title"
		private const val COLUMN_DESCRIPTION = "description"
	}

	private val dbHelper: DatabaseHelper = DatabaseHelper(context)

	fun saveToDoItem(toDoItem: ToDoItem) {
		val db = dbHelper.writableDatabase
		val values = ContentValues().apply {
			put(COLUMN_TITLE, toDoItem.title)
			put(COLUMN_DESCRIPTION, toDoItem.description)
		}

		val newRowId = db.insert(TABLE_NAME, null, values)
		if (newRowId != -1L) {
			SmartLogger.logMessage("ToDo item saved with ID: $newRowId")
		} else {
			SmartLogger.e("Failed to save ToDo item")
		}

		db.close()
	}

	fun updateToDoItem(toDoItem: ToDoItem) {
		val db = dbHelper.writableDatabase
		val values = ContentValues().apply {
			put(COLUMN_TITLE, toDoItem.title)
			put(COLUMN_DESCRIPTION, toDoItem.description)
		}

		val selection = "$COLUMN_ID = ?"
		val selectionArgs = arrayOf(toDoItem.id.toString())

		val rowsUpdated = db.update(TABLE_NAME, values, selection, selectionArgs)
		if (rowsUpdated > 0) {
			SmartLogger.logMessage("ToDo item updated with ID: ${toDoItem.id}")
		} else {
			SmartLogger.e("Failed to update ToDo item with ID: ${toDoItem.id}")
		}

		db.close()
	}

	fun deleteToDoItem(itemId: Long) {
		val db = dbHelper.writableDatabase

		val selection = "$COLUMN_ID = ?"
		val selectionArgs = arrayOf(itemId.toString())

		val rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs)
		if (rowsDeleted > 0) {
			SmartLogger.logMessage("ToDo item deleted with ID: $itemId")
		} else {
			SmartLogger.e("Failed to delete ToDo item with ID: $itemId")
		}

		db.close()
	}

	fun getToDoItems(): List<ToDoItem> {
		val db = dbHelper.readableDatabase
		val projection = arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION)

		val cursor: Cursor = db.query(TABLE_NAME, projection, null, null, null, null, null)

		val toDoItems = mutableListOf<ToDoItem>()

		with(cursor) {
			while (moveToNext()) {
				val id = getLong(getColumnIndexOrThrow(COLUMN_ID))
				val title = getString(getColumnIndexOrThrow(COLUMN_TITLE))
				val description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION))
				toDoItems.add(ToDoItem(id, title, description))
			}
		}

		cursor.close()
		db.close()

		return toDoItems
	}

	private class DatabaseHelper(context: Context) :
		SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

		override fun onCreate(db: SQLiteDatabase) {
			val createTableSQL = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT, $COLUMN_DESCRIPTION TEXT)"
			db.execSQL(createTableSQL)
		}

		override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
			val dropTableSQL = "DROP TABLE IF EXISTS $TABLE_NAME"
			db.execSQL(dropTableSQL)
			onCreate(db)
		}
	}
}
