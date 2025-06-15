package de.ju.wss

/**
 * Estimates the current Wi-Fi signal strength as a normalized percentage (0–100%),
 * based on system-specific command output for macOS, Linux, and Windows.
 *
 * The estimator parses platform-dependent system commands to retrieve the signal strength.
 * - On **Windows**, it uses `netsh wlan show interfaces`.
 * - On **Linux**, it uses `nmcli dev wifi`.
 * - On **macOS**, it parses RSSI from `airport -I` and converts it to a percentage.
 *
 * Methods:
 * - getSignalStrength(): Detects the current OS and delegates to the appropriate platform method.
 * - getWifiSignalStrengthWindows(): Parses the signal percentage from Windows command output.
 * - getWifiSignalStrengthLinux(): Extracts signal strength from `nmcli` output.
 * - getWifiSignalStrengthMac(): Reads RSSI and converts it to a signal percentage.
 * - estimateSignalPercentFromRssi(rssi: Int): Maps an RSSI value (typically -100 to -50 dBm) to a percentage.
 */
class WifiSignalEstimator {
    fun getSignalStrength(): Float? {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("windows") -> getWifiSignalStrengthWindows()
            os.contains("linux") -> getWifiSignalStrengthLinux()
            os.contains("mac") -> getWifiSignalStrengthMac()
            else -> null
        }
    }

    private fun getWifiSignalStrengthLinux(): Float? {
        val process = ProcessBuilder("nmcli", "-f", "active,ssid,signal", "dev", "wifi").start()
        val output = process.inputStream.bufferedReader().readText()
        val line = output.lines().firstOrNull { it.contains("yes") } ?: return null
        val signalStr = line.trim().split(Regex("\\s+")).lastOrNull() ?: return null
        return signalStr.toFloatOrNull()
    }

    private fun getWifiSignalStrengthWindows(): Float? {
        val process = ProcessBuilder("cmd", "/c", "netsh wlan show interfaces").start()
        val result = process.inputStream.bufferedReader().readText()
        val match = Regex("Signal\\s*:\\s*(\\d+)%").find(result) ?: return null
        return match.groupValues[1].toFloatOrNull()
    }

    private fun getWifiSignalStrengthMac(): Float? {
        val process = ProcessBuilder(
            "/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport",
            "-I"
        ).start()
        val output = process.inputStream.bufferedReader().readText()
        val match = Regex("agrCtlRSSI:\\s*(-?\\d+)").find(output) ?: return null
        val rssi = match.groupValues[1].toIntOrNull() ?: return null
        return estimateSignalPercentFromRssi(rssi)
    }

    private fun estimateSignalPercentFromRssi(rssi: Int): Float {
        return when {
            rssi <= -100 -> 0f
            rssi >= -50 -> 100f
            else -> 2f * (rssi + 100)
        }
    }
}