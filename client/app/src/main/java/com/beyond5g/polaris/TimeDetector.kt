package com.beyond5g.polaris

import java.text.SimpleDateFormat
import java.util.*

class DateTimeHelper {
    companion object {
        private const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy"
        private const val DEFAULT_TIME_FORMAT = "HH:mm:ss"
        private const val DEFAULT_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss"

        fun getCurrentDate(pattern: String = DEFAULT_DATE_FORMAT): String {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            return formatter.format(Date())
        }


        fun getCurrentTime(pattern: String = DEFAULT_TIME_FORMAT): String {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            return formatter.format(Date())
        }


        fun getCurrentDateTime(pattern: String = DEFAULT_DATE_TIME_FORMAT): String {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            return formatter.format(Date())
        }

        fun getCurrentTimestamp(): Long {
            return System.currentTimeMillis()
        }

        fun formatTimestamp(timestamp: Long, pattern: String = DEFAULT_DATE_TIME_FORMAT): String {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            return formatter.format(Date(timestamp))
        }

        fun getCurrentDayName(): String {
            return SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        }


        fun getCurrentMonthName(): String {
            return SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
        }
    }
}