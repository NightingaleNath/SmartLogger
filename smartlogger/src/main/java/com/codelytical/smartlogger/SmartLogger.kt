package com.codelytical.smartlogger

import android.util.Log
import androidx.annotation.IntDef
import com.google.firebase.database.DatabaseReference

object SmartLogger {
	private const val TAG = "SmartLogger"
	private var firebaseEnabled: Boolean = false
	private var firebaseReference: DatabaseReference? = null

	@Retention(AnnotationRetention.SOURCE)
	@IntDef(Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, Log.ASSERT)
	annotation class LogLevel

	@LogLevel
	var logLevel: Int = Log.DEBUG

	/**
	 * Enables Firebase logging.
	 * @param firebaseReference The DatabaseReference to the "logs" node in the user's Firebase project.
	 */
	fun enableFirebaseLogging(firebaseReference: DatabaseReference) {
		this.firebaseReference = firebaseReference
		firebaseEnabled = true
	}

	/**
	 * Disables Firebase logging.
	 */
	fun disableFirebaseLogging() {
		firebaseEnabled = false
	}

	/**
	 * Logs a verbose message.
	 * @param message The log message.
	 * @param tag The log tag.
	 */
	fun v(message: String, tag: String? = null) {
		log(Log.VERBOSE, tag ?: TAG, message)
	}

	/**
	 * Logs a debug message.
	 * @param message The log message.
	 * @param tag The log tag.
	 */
	fun d(message: String, tag: String? = null) {
		log(Log.DEBUG, tag ?: TAG, message)
	}

	/**
	 * Logs an info message.
	 * @param message The log message.
	 * @param tag The log tag.
	 */
	fun i(message: String, tag: String? = null) {
		log(Log.INFO, tag ?: TAG, message)
	}

	/**
	 * Logs a warning message.
	 * @param message The log message.
	 * @param tag The log tag.
	 */
	fun w(message: String, tag: String? = null) {
		log(Log.WARN, tag ?: TAG, message)
	}

	/**
	 * Logs an error message.
	 * @param message The log message.
	 * @param tag The log tag.
	 */
	fun e(message: String, tag: String? = null) {
		log(Log.ERROR, tag ?: TAG, message)
	}

	/**
	 * Logs a message based on the current log level.
	 * If the log level is DEBUG, it calls log.d; otherwise, it calls log.i.
	 * @param message The log message.
	 * @param tag The log tag.
	 */
	fun logMessage(message: String, tag: String? = null) {
		when (logLevel) {
			Log.DEBUG -> log(Log.DEBUG, tag ?: TAG, message)
			else -> log(Log.INFO, tag ?: TAG, message)
		}
	}

	private fun log(@LogLevel level: Int, tag: String, message: String) {
		if (level >= logLevel) {
			val logMessage = if (tag != TAG) "[$tag] $message" else message
			Log.println(level, tag, getMessageWithLogDetails(logMessage))

			if (firebaseEnabled) {
				writeLogToFirebase(level, tag, message)
			}
		}
	}

	private fun writeLogToFirebase(level: Int, tag: String, message: String) {
		val logReference = firebaseReference?.push()
		val logData = hashMapOf(
			"level" to level,
			"tag" to tag,
			"message" to message,
			"timestamp" to System.currentTimeMillis()
		)
		logReference?.setValue(logData) { databaseError, _ ->
			if (databaseError != null) {
				Log.e(TAG, "Failed to write log to Firebase: ${databaseError.message}")
			}
		}
	}

	private fun getMessageWithLogDetails(message: String): String {
		val stackTraceElement = Thread.currentThread().stackTrace[5]
		val methodName = stackTraceElement.methodName
		val lineNumber = stackTraceElement.lineNumber
		return "[$methodName():$lineNumber] $message"
	}
}
