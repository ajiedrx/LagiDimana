package com.project.lagidimana.di

import com.project.lagidimana.data.AppDataStore
import com.project.lagidimana.data.AppDatabase
import com.project.lagidimana.data.AppRepository
import com.project.lagidimana.presentation.DashboardViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModules = module {
    single { AppDatabase.getDatabase(get()).getLocationLogDao() }
    single<AppRepository> { AppDataStore(get()) }
    viewModel { DashboardViewModel(androidApplication(), get()) }
}

