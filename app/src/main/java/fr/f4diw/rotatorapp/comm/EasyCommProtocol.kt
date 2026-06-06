package fr.f4diw.rotatorapp.comm

object EasyCommProtocol {

    // ── Commandes → rotateur (Format Look4Sat pour ESP32 F4DIW) ────
    fun setAzEl(az: Float, el: Float) = "P ${az.format()} ${el.format()}\n"
    
    // Fallbacks pour compatibilité avec le reste du code
    fun setAzimuth(az: Float) = "P ${az.format()} 0.0\n"
    fun setElevation(el: Float) = "P 0.0 ${el.format()}\n"
    
    fun stopAz() = "SA\n"
    fun stopEl() = "SE\n"
    fun getStatus() = "GS\n"
    fun getError() = "GE\n"
    fun getPosition() = "IP\n"
    fun getVersion() = "VE\n"
    fun park() = "P 0.0 0.0\n"
    fun reboot() = "RB\n"
    
    // ── Jog & Calibration (F4DIW Specific) ────────────────────────
    fun jogAzLeft() = "ML\n"
    fun jogAzRight() = "MR\n"
    fun jogElUp() = "MU\n"
    fun jogElDown() = "MD\n"
    fun reset() = "RST\n"

    private fun Float.format() = String.format(java.util.Locale.US, "%.1f", this)

    // ── Réponses ← rotateur ───────────────────────────────────────
    sealed class Response {
        data class Position(val az: Float, val el: Float) : Response()
        data class Status(val code: Int) : Response()
        data class Error(val code: Int) : Response()
        data class Version(val text: String) : Response()
        object Unknown : Response()
    }

    fun parse(line: String): Response {
        val s = line.trim()
        return when {
            s.startsWith("AZ") && s.contains("EL") -> {
                val az = Regex("AZ([\\d.]+)").find(s)
                    ?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                val el = Regex("EL([\\d.]+)").find(s)
                    ?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                Response.Position(az, el)
            }
            s.startsWith("GS") -> Response.Status(
                s.removePrefix("GS").trim().toIntOrNull() ?: 0
            )
            s.startsWith("GE") -> Response.Error(
                s.removePrefix("GE").trim().toIntOrNull() ?: 0
            )
            s.startsWith("VE") -> Response.Version(
                s.removePrefix("VE").trim()
            )
            else -> Response.Unknown
        }
    }
}