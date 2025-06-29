package com.beyond5g.polaris

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.*
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

class CellDetector(private val context: Context) {

    var gen: String? = null
    var tech: String? = null
    var plmn: String? = null
    var cid: Long? = null
    var lac: Int? = null
    var rac: Int? = null
    var tac: Int? = null
    var type: String = ""
    var arfcn: Int? = null
    var band: String? = null
    var frequencyMHz: Double? = null
    var rsrp : Double? = null
    var rsrq : Double? = null
    var rscp : Double? = null
    var ecn0 : Double? = null
    var rxlev : Double? = null



    @RequiresPermission(anyOf = [Manifest.permission.READ_BASIC_PHONE_STATE, Manifest.permission.READ_PHONE_STATE])
    fun getNetworkTech(): String? {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            tech = "PERMISSION_NOT_GRANTED"
            return tech
        }

        val networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.dataNetworkType
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.networkType
        }

        tech = when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
            TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO rev. 0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO rev. A"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO rev. B"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD-SCDMA"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            30 -> "LTE-Advanced"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
            TelephonyManager.NETWORK_TYPE_NR -> "NR (5G)"
            20 -> "5G NSA"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            else -> "UNKNOWN ($networkType)"
        }

        return tech
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getNetworkGen(): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: run {
            gen = "Not Connected"
            return gen
        }

        gen = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "not found"
                }

                val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    telephonyManager.dataNetworkType
                } else {
                    @Suppress("DEPRECATION")
                    telephonyManager.networkType
                }

                when (type) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN,
                    TelephonyManager.NETWORK_TYPE_GSM -> "2G"

                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"

                    TelephonyManager.NETWORK_TYPE_LTE,
                    TelephonyManager.NETWORK_TYPE_IWLAN,
                    19 -> "4G"

                    TelephonyManager.NETWORK_TYPE_NR -> "5G"

                    else -> "CELLULAR"
                }
            }
            else -> "UNKNOWN"
        }
        return gen
    }

    fun getGsmFrequencyFromARFCN(arfcn: Int): Double {
        return when (arfcn) {
            in 0..124 -> 935.0 + 0.2 * (arfcn - 1)  // GSM 900
            in 975..1023 -> 925.2 + 0.2 * (arfcn - 975)  // GSM 900 extended
            in 512..885 -> 1930.0 + 0.2 * (arfcn - 512)  // PCS 1900
            else -> -1.0
        }
    }

    fun getGsmBandFromARFCN(arfcn: Int): String {
        return when (arfcn) {
            in 0..124 -> "GSM 900"
            in 975..1023 -> "GSM 900 Extended"
            in 512..885 -> "GSM 1900"
            else -> "Unknown"
        }
    }

    fun getWcdmaFrequencyFromUARFCN(uarfcn: Int): Double {
        return when {
            uarfcn in 10562..10838 -> 2112.4 + 0.2 * (uarfcn - 10562) // Band 1 (2100 MHz)
            uarfcn in 9662..9938 -> 1932.4 + 0.2 * (uarfcn - 9662)    // Band 2 (1900 MHz)
            uarfcn in 1162..1513 -> 1807.4 + 0.2 * (uarfcn - 1162)    // Band 3 (1800 MHz)
            uarfcn in 1537..1738 -> 2112.4 + 0.2 * (uarfcn - 1537)    // Band 4 (1700/2100 MHz)
            uarfcn in 4357..4458 -> 873.4 + 0.2 * (uarfcn - 4357)     // Band 5 (850 MHz)
            uarfcn in 2237..2563 -> 1852.4 + 0.2 * (uarfcn - 2237)   // Band 6 (800 MHz)
            uarfcn in 2012..2338 -> 1747.4 + 0.2 * (uarfcn - 2012)    // Band 9 (1700 MHz)
            uarfcn in 2937..3088 -> 1842.4 + 0.2 * (uarfcn - 2937)    // Band 10 (1700 MHz)
            uarfcn in 3712..3787 -> 704.4 + 0.2 * (uarfcn - 3712)     // Band 11 (1500 MHz)
            uarfcn in 3842..3903 -> 729.4 + 0.2 * (uarfcn - 3842)     // Band 12 (700 MHz)
            uarfcn in 4017..4043 -> 746.4 + 0.2 * (uarfcn - 4017)     // Band 13 (700 MHz)
            uarfcn in 4117..4143 -> 758.4 + 0.2 * (uarfcn - 4117)     // Band 14 (700 MHz)
            uarfcn in 4387..4413 -> 882.4 + 0.2 * (uarfcn - 4387)     // Band 8 (900 MHz)
            uarfcn in 712..763 -> 1712.4 + 0.2 * (uarfcn - 712)       // Band 19 (800 MHz)
            else -> -1.0
        }
    }

    fun getWcdmaBandFromUarfcn(uarfcn: Int): String {
        return when {
            uarfcn in 10562..10838 -> "Band 1"
            uarfcn in 9662..9938 -> "Band 2"
            uarfcn in 1162..1513 -> "Band 3"
            uarfcn in 1537..1738 -> "Band 4"
            uarfcn in 4357..4458 -> "Band 5"
            uarfcn in 2237..2563 -> "Band 6 "
            uarfcn in 2012..2338 -> "Band 9 (1700 MHz)"
            uarfcn in 2937..3088 -> "Band 10 (1700 MHz)"
            uarfcn in 3712..3787 -> "Band 11 (1500 MHz)"
            uarfcn in 3842..3903 -> "Band 12 (700 MHz)"
            uarfcn in 4017..4043 -> "Band 13 (700 MHz)"
            uarfcn in 4117..4143 -> "Band 14 (700 MHz)"
            uarfcn in 4387..4413 -> "Band 8 (900 MHz)"
            uarfcn in 712..763 -> "Band 19 (800 MHz)"
            else -> "Unknown"
        }
    }


    fun getWcdmaBandFromUARFCN(uarfcn: Int): String {
        return when (uarfcn) {
            in 10562..10838 -> "Band 1 (2100 MHz)"
            in 9662..9938 -> "Band 2 (1900 MHz)"
            in 1162..1513 -> "Band 3 (1800 MHz)"
            else -> "Unknown"
        }
    }

    fun getNrFrequencyFromNRARFCN(nrarfcn: Int): Double {
        return if (nrarfcn in 0..3279165) 0.005 * nrarfcn else -1.0
    }

    fun getNrBandFromNRARFCN(nrarfcn: Int): String {
        return when (nrarfcn) {
            in 620000..653333 -> "n78 (3500 MHz)"
            in 693334..733333 -> "n77 (3700 MHz)"
            in 2054166..2104165 -> "n260 (39 GHz)"
            else -> "Unknown"
        }
    }

    fun getLteFrequencyFromEARFCN(earfcn: Int): Double {
        return when (earfcn) {
            in 0..599 -> 2110 + 0.1 * (earfcn - 0)
            in 600..1199 -> 1930 + 0.1 * (earfcn - 600)
            in 1200..1949 -> 1805 + 0.1 * (earfcn - 1200)
            in 1950..2399 -> 2110 + 0.1 * (earfcn - 1950)
            in 2400..2649 -> 869 + 0.1 * (earfcn - 2400)
            in 2750..3449 -> 2620 + 0.1 * (earfcn - 2750)
            in 3450..3799 -> 925 + 0.1 * (earfcn - 3450)
            else -> -1.0
        }
    }

    fun getLteBandFromArfcn(earfcn: Int): String {
        return when (earfcn) {
            in 0..599 -> "Band 1"
            in 600..1199 -> "Band 2"
            in 1200..1949 -> "Band 3"
            in 1950..2399 -> "Band 4"
            in 2400..2649 -> "Band 5"
            in 2750..3449 -> "Band 7"
            in 3450..3799 -> "Band 8"
            else -> "Unknown"
        }
    }


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @RequiresApi(Build.VERSION_CODES.R)
    fun updateCellDetails() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cellInfoList = telephonyManager.allCellInfo ?: return

        // Collect all registered cells
        val registeredCells = cellInfoList.filter { it.isRegistered }

        // Prefer WCDMA > LTE > GSM
        val preferredCell = registeredCells.firstOrNull { it is CellInfoGsm }
            ?: registeredCells.firstOrNull { it is CellInfoWcdma }
            ?: registeredCells.firstOrNull { it is CellInfoLte }
            ?: return

        when (preferredCell) {
            is CellInfoGsm -> {
                Log.d("DEBUG", "2G detected")
                val identity = preferredCell.cellIdentity
                val signal = preferredCell.cellSignalStrength

                plmn = "${identity.mcc}${identity.mnc}"
                cid = identity.cid.toLong()
                lac = identity.lac
                tac = null
                type = "GSM"
                arfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) identity.arfcn else null
                rxlev = signal.dbm.toDouble()

                arfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) identity.arfcn else null
                band = arfcn?.let { getGsmBandFromARFCN(it) }
                frequencyMHz = arfcn?.let { getGsmFrequencyFromARFCN(it) }
                rsrp = null
                rsrq = null
                ecn0 = null
                rscp = null
            }

            is CellInfoWcdma -> {

                val identity = preferredCell.cellIdentity
                val signal = preferredCell.cellSignalStrength
                Log.d("Signal Class", signal::class.java.name)

                plmn = "${identity.mcc}${identity.mnc}"
                cid = identity.cid.toLong()
                lac = identity.lac
                rac = null
                tac = null
                type = "WCDMA"
                arfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) identity.uarfcn else null
                ecn0 = signal.ecNo.toDouble()
//                rscp = signal.rscp
                rsrp = null
                rsrq = null
                rxlev = null
                band = arfcn?.let { getWcdmaBandFromUarfcn(it) }
                frequencyMHz = arfcn?.let { getWcdmaFrequencyFromUARFCN(it) }

                Log.d("3G Signal", "ECN0 = ${signal.ecNo} dB, DBM = ${signal.dbm}")
            }

            is CellInfoLte -> {
                val identity = preferredCell.cellIdentity
                val signal = preferredCell.cellSignalStrength

                plmn = "${identity.mcc}${identity.mnc}"
                cid = identity.ci.toLong()
                tac = identity.tac
                lac = null
                rac = null
                type = "LTE"
                arfcn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) identity.earfcn else null
                band = arfcn?.let { getLteBandFromArfcn(it) }
                rsrp = signal.rsrp.toDouble()
                rsrq = signal.rsrq.toDouble()
                ecn0 = null
                rxlev = null
                rscp = null
                frequencyMHz = arfcn?.let { getLteFrequencyFromEARFCN(it) }
            }
        }
    }
}

