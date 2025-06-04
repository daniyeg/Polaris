package com.beyond5g.polaris

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.*
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

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
    var rsrp : Int? = null
    var rsrq : Int? = null
    var rscp : Int? = null
    var ecn0 : Int? = null
    var rxlev : Int? = null



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

    fun updateCellDetails() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cellInfoList = telephonyManager.allCellInfo
        val cellInfo = cellInfoList.firstOrNull { it.isRegistered } ?: return

        when (cellInfo) {
            is CellInfoGsm -> {
                val identity = cellInfo.cellIdentity
                val arfcnValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) identity.arfcn else null

                plmn = "${identity.mcc}${identity.mnc}"
                cid = identity.cid.toLong()
                lac = identity.lac
                rac = null
                tac = null
                type = "GSM"
                arfcn = arfcnValue
                band = null
                frequencyMHz = null

                val signal = cellInfo.cellSignalStrength as CellSignalStrengthGsm
                rxlev = signal.dbm
            }

            is CellInfoWcdma -> {
                val identity = cellInfo.cellIdentity
                val uarfcnValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) identity.uarfcn else null

                plmn = "${identity.mcc}${identity.mnc}"
                cid = identity.cid.toLong()
                lac = identity.lac
                rac = null
                tac = null
                type = "WCDMA"
                arfcn = uarfcnValue
                band = null
                frequencyMHz = null

                val signal = cellInfo.cellSignalStrength as CellSignalStrengthWcdma
//                rscp = signal.rscp  // API 29+
//                ecn0 = signal.ecNo
            }

            is CellInfoLte -> {
                val identity = cellInfo.cellIdentity
                val earfcnValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) identity.earfcn else null

                plmn = "${identity.mcc}${identity.mnc}"
                cid = identity.ci.toLong()
                lac = null
                rac = null
                tac = identity.tac
                type = "LTE"
                arfcn = earfcnValue
                band = earfcnValue?.let { getLteBandFromArfcn(it) }
                frequencyMHz = earfcnValue?.let { getLteFrequencyFromEARFCN(it) }

                val signal = cellInfo.cellSignalStrength as CellSignalStrengthLte
                rsrp = signal.rsrp
                rsrq = signal.rsrq
            }

            else -> {
                // Do nothing or set default values
            }
        }
    }
}
