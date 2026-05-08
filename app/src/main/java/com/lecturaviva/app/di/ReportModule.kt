package com.lecturaviva.app.di

import com.lecturaviva.app.data.ReportRepositoryImpl
import com.lecturaviva.app.domain.repository.ReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportModule {

    @Binds
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
}
