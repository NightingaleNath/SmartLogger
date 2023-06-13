package com.codelytical.smartlogger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

	private lateinit var firebaseDatabase: FirebaseDatabase

	private lateinit var logsReference: DatabaseReference

	private lateinit var editTextTitle: EditText
	private lateinit var editTextDescription: EditText
	private lateinit var buttonSave: Button
	private lateinit var listViewTodo: ListView

	private lateinit var toDoManager: ToDoManager
	private lateinit var toDoAdapter: ArrayAdapter<ToDoItem>

	private var selectedItemId: Long = -1

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// Initialize Firebase in the user's project
		firebaseDatabase = FirebaseDatabase.getInstance()

		toDoManager = ToDoManager(this)

		editTextTitle = findViewById(R.id.editTextTitle)
		editTextDescription = findViewById(R.id.editTextDescription)
		buttonSave = findViewById(R.id.buttonSave)
		listViewTodo = findViewById(R.id.listViewTodo)

		// Set log level (optional)
		if (!BuildConfig.DEBUG) {
			SmartLogger.logLevel = Log.DEBUG // Set log level to DEBUG for debug builds
			logsReference = firebaseDatabase.getReference("debug_logs")
		} else {
			SmartLogger.logLevel = Log.INFO // Set log level to INFO or the desired level for release builds
			logsReference = firebaseDatabase.getReference("logs")
		}

		// Enable Firebase logging in the SmartLogger library
		SmartLogger.enableFirebaseLogging(logsReference)

		SmartLogger.logMessage("ToDo app started")

		buttonSave.setOnClickListener {
			val title = editTextTitle.text.toString()
			val description = editTextDescription.text.toString()

			if (selectedItemId != -1L) {
				// Edit existing item
				val toDoItem = ToDoItem(id = selectedItemId, title = title, description = description)
				toDoManager.updateToDoItem(toDoItem)
				selectedItemId = -1
			} else {
				// Add new item
				val toDoItem = ToDoItem(title = title, description = description)
				toDoManager.saveToDoItem(toDoItem)
			}

			updateToDoList()
			clearInputFields()
		}

		toDoAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
		listViewTodo.adapter = toDoAdapter

		listViewTodo.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			val toDoItem = toDoAdapter.getItem(position)
			if (toDoItem != null) {
				selectedItemId = toDoItem.id
				editTextTitle.setText(toDoItem.title)
				editTextDescription.setText(toDoItem.description)
				buttonSave.text = "Update"
			}
		}

		listViewTodo.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
			val toDoItem = toDoAdapter.getItem(position)
			if (toDoItem != null) {
				toDoManager.deleteToDoItem(toDoItem.id)
				updateToDoList()
			}
			true
		}

		updateToDoList()

	}

	private fun updateToDoList() {
		val toDoItems = toDoManager.getToDoItems()
		toDoAdapter.clear()
		toDoAdapter.addAll(toDoItems)
		toDoAdapter.notifyDataSetChanged()
	}

	private fun clearInputFields() {
		editTextTitle.text.clear()
		editTextDescription.text.clear()
		buttonSave.text = "Save"
	}

	override fun onDestroy() {
		super.onDestroy()
		// Disable Firebase logging
		SmartLogger.disableFirebaseLogging()
	}
}