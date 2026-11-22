package com.example.elearning.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

class ConnectivityNetworkMonitor(context: Context, scope: CoroutineScope) : NetworkMonitor {
    private val manager = context.getSystemService(ConnectivityManager::class.java)!!

    override val isOnline = callbackFlow {
        val lock = Any()
        fun publishCurrentNetwork() = synchronized(lock) {
            try {
                val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
                trySend(capabilities?.hasInternet() == true)
            } catch (error: SecurityException) {
                Log.w("NetworkMonitor", "Brak dostępu do stanu sieci", error)
                trySend(true)
            }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                publishCurrentNetwork()
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                synchronized(lock) { trySend(capabilities.hasInternet()) }
            }

            override fun onLost(network: Network) {
                publishCurrentNetwork()
            }
        }
        try {
            manager.registerDefaultNetworkCallback(callback)
        } catch (error: SecurityException) {
            Log.w("NetworkMonitor", "Nie można zarejestrować monitora sieci", error)
            trySend(true)
            close()
            return@callbackFlow
        }
        publishCurrentNetwork()
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.buffer(Channel.CONFLATED).distinctUntilChanged().stateIn(scope, SharingStarted.WhileSubscribed(5_000), true)

    private fun NetworkCapabilities.hasInternet() =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
