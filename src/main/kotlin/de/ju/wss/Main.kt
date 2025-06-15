package de.ju.wss

fun main() {
    val wifiSignalMonitor = WifiSignalMonitor()
    wifiSignalMonitor.start(WifiSignalEstimator())
}
