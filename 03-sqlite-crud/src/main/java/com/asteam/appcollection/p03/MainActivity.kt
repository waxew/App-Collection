package com.asteam.appcollection.p03

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.asteam.appcollection.sharedui.BaseDemoActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rebuilt Kotlin application #03.
 * Original Dropbox source: جعبه ابزار - دیتابیس - همه کارها.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Sqlite Crud"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "مدیریت داده محلی SQLite با CRUD"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "جعبه ابزار - دیتابیس - همه کارها.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val nameInput = input("نام را وارد کنید")
        val output = label("هنوز رکوردی نمایش داده نشده است.")
        val helper = PeopleDb(this)

        container.addView(nameInput)
        container.addView(button("افزودن") {
            val value = nameInput.text.toString().trim()
            if (value.isNotEmpty()) {
                helper.insertName(value)
            }
            output.text = helper.readNames().joinToString(
                prefix = "رکوردها:\n",
                separator = "\n"
            )
            nameInput.text.clear()
        })

        container.addView(button("تغییر اولین رکورد به مقدار واردشده") {
            val value = nameInput.text.toString().trim()
            if (value.isNotEmpty()) {
                helper.updateFirst(value)
            }
            output.text = helper.readNames().joinToString(
                prefix = "رکوردها:\n",
                separator = "\n"
            )
        })

        container.addView(button("حذف همه") {
            helper.clearAll()
            output.text = "دیتابیس خالی شد."
        })
        container.addView(output)
    }

    /** Minimal SQLiteOpenHelper keeping the original CRUD idea but fixing resource handling. */
    private class PeopleDb(context: Context) : SQLiteOpenHelper(context, "people.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE people(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL" +
                    ")"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

        fun insertName(name: String) {
            writableDatabase.execSQL(
                "INSERT INTO people(name) VALUES(?)",
                arrayOf(name)
            )
        }

        fun updateFirst(name: String) {
            writableDatabase.execSQL(
                "UPDATE people SET name=? " +
                    "WHERE id=(SELECT id FROM people ORDER BY id LIMIT 1)",
                arrayOf(name)
            )
        }

        fun clearAll() {
            writableDatabase.execSQL("DELETE FROM people")
        }

        fun readNames(): List<String> {
            val result = mutableListOf<String>()
            readableDatabase.rawQuery(
                "SELECT name FROM people ORDER BY id",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    result += cursor.getString(0)
                }
            }
            return result
        }
    }
}
