package com.lecturaviva.app.domain.repository

import com.lecturaviva.app.domain.model.Report

interface ReportRepository {
    suspend fun sendReport(report: Report): com.lecturaviva.app.util.Result<Unit>
}
