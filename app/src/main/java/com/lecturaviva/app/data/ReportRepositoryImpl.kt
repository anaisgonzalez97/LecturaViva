package com.lecturaviva.app.data

import com.lecturaviva.app.domain.model.Report
import com.lecturaviva.app.domain.repository.ReportRepository
import com.lecturaviva.app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor() : ReportRepository {

    companion object {
        private const val EMAILJS_URL    = "https://api.emailjs.com/api/v1.0/email/send"
        private const val SERVICE_ID     = "service_ze70wno"
        private const val TEMPLATE_ID    = "template_esi3hbh"
        private const val PUBLIC_KEY     = "ulkaSnfjDiw7fMs0-"
    }

    override suspend fun sendReport(report: Report): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Construir el JSON para EmailJS
            val templateParams = JSONObject().apply {
                put("user_email",   report.userEmail)
                put("report_type",  report.type)
                put("message",      report.message)
                put("name",         report.userEmail)
                put("email",        report.userEmail)
            }

            val body = JSONObject().apply {
                put("service_id",   SERVICE_ID)
                put("template_id",  TEMPLATE_ID)
                put("user_id",      PUBLIC_KEY)
                put("template_params", templateParams)
            }

            // Llamada HTTP a EmailJS
            val conn = URL(EMAILJS_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("origin", "http://localhost")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val responseCode = conn.responseCode
            if (responseCode != 200) {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: "Error desconocido"
                throw Exception("Error EmailJS ($responseCode): $error")
            }

        }.fold(
            { Result.Success(Unit) },
            { Result.Error(it.message ?: "Error al enviar el reporte") }
        )
    }
}