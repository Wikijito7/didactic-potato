package es.wokis.didacticpotato.di

import androidx.room.Room
import es.wokis.didacticpotato.data.api.ApiClient
import es.wokis.didacticpotato.data.api.AuthApi
import es.wokis.didacticpotato.data.api.SensorApi
import es.wokis.didacticpotato.data.api.TwoFactorAuthManager
import es.wokis.didacticpotato.data.api.UserApi
import es.wokis.didacticpotato.data.auth.TokenProvider
import es.wokis.didacticpotato.data.local.AppDatabase
import es.wokis.didacticpotato.data.local.SettingsRepository
import es.wokis.didacticpotato.data.local.datasource.SensorLocalDataSource
import es.wokis.didacticpotato.data.local.datasource.UserLocalDataSource
import es.wokis.didacticpotato.data.remote.datasource.AuthRemoteDataSource
import es.wokis.didacticpotato.data.remote.datasource.SensorRemoteDataSource
import es.wokis.didacticpotato.data.repository.AuthRepository
import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.data.repository.UserRepository
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    // Token Provider - single (holds token state)
    singleOf(::TokenProvider)

    // API Layer
    singleOf(::ApiClient)
    single<HttpClient> { get<ApiClient>().client }
    singleOf(::AuthApi)
    singleOf(::SensorApi)
    singleOf(::UserApi)

    // Remote Data Sources - single (maintain connections)
    singleOf(::AuthRemoteDataSource)
    singleOf(::SensorRemoteDataSource)

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "didactic_potato_db"
        ).build()
    }

    // Room DAOs
    single { get<AppDatabase>().sensorDao() }
    single { get<AppDatabase>().userDao() }

    // Local Data Sources - single (maintain state/connections)
    singleOf(::SensorLocalDataSource)
    singleOf(::UserLocalDataSource)

    // Two Factor Auth Manager - single (singleton state)
    singleOf(::TwoFactorAuthManager)

    // Settings Repository - single (singleton state)
    singleOf(::SettingsRepository)

    // Repositories
    // AuthRepository & SensorRepository: factory (stateless)
    factoryOf(::AuthRepository)
    factoryOf(::SensorRepository)
    // UserRepository: single (holds local data source reference + 2FA state)
    singleOf(::UserRepository)
}
