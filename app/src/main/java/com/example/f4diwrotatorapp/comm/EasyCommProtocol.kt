package com.example.f4diwrotatorapp.comm

object EasyCommProtocol {

    // ── Commandes → rotateur ──────────────────────────────────────
    fun setAzimuth(az: Float) = "AZ${az.format()}\r\n"
    fun setElevation(el: Float) = "EL${el.format()}\r\n"
    fun setAzEl(az: Float, el: Float) = "AZ${az.format()} EL${el.format()}\r\n"
    fun stopAz() = "SA\r\n"
    fun stopEl() = "SE\r\n"
    fun getStatus() = "GS\r\n"
    fun getError() = "GE\r\n"
    fun getPosition() = "IP\r\n"
    fun getVersion() = "VE\r\n"
    fun park() = "PK\r\n"
    fun reboot() = "RB\r\n"

    private fun Float.format() = "%.1f".format(this)

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