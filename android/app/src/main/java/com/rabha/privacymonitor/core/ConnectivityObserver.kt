package com.rabha.privacymonitor.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

data class NetworkStatus(
    val online: Boolean,
    val type: String,
    val wifi: Boolean,
    val mobile: Boolean
)

@Singleton
class ConnectivityObserver @Inject constructor(private val context: Context) {

    private val manager: ConnectivityManager? = context.getSystemService()

    fun currentStatus(): NetworkStatus {
        val cm = manager ?: return NetworkStatus(false, "Unknown", false, false)
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val wifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val mobile = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val type = when {
            wifi -> "Wi-Fi"
            mobile -> "Mobile data"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> "VPN"
            else -> "Offline"
        }
        return NetworkStatus(hasInternet, type, wifi, mobile)
    }

    fun observe(): Flow<NetworkStatus> = callbackFlow {
        val cm = manager
        if (cm == null) {
            trySend(NetworkStatus(false, "Unknown", false, false))
            awaitClose { }
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentStatus())
            }

            override fun onLost(network: Network) {
                trySend(currentStatus())
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(currentStatus())
            }
        }
        cm.registerDefaultNetworkCallback(callback)
        trySend(currentStatus())
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
