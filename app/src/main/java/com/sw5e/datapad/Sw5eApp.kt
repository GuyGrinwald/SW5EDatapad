package com.sw5e.datapad

import android.app.Application
import android.content.Intent
import android.os.Process
import kotlin.system.exitProcess

class Sw5eApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        // This acts as the ultimate global net for ANY uncaught crash in the app
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Extract the full crash details
                val errorDetails = buildString {
                    append("Thread: ${thread.name}\n")
                    append("Error: ${throwable.javaClass.name}: ${throwable.localizedMessage}\n\n")
                    append(throwable.stackTraceToString())
                }

                // Launch a dedicated Global Error Activity to show the modal
                val intent = Intent(this, GlobalErrorActivity::class.java).apply {
                    putExtra("CRASH_DETAILS", errorDetails)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)

                // Kill the background process cleanly after launching the modal activity
                Process.killProcess(Process.myPid())
                exitProcess(10)
            } catch (e: Exception) {
                // Fallback to default behavior if anything goes wrong in our catcher
                previousHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}