package es.wokis.didacticpotato.di

import androidx.room.Room
import es.wokis.didacticpotato.data.api.ApiClient
import es.wokis.didacticpotato.data.api.AuthApi
import es.wokis.didacticpotato.data.api.SensorApi
import es.wokis.didacticpotato.data.api.UserApi
import es.wokis.didacticpotato.data.remote.datasource.AuthRemoteDataSource
import es.wokis.didacticpotato.data.auth.TokenProvider
import es.wokis.didacticpotato.data.local.AppDatabase
import es.wokis.didacticpotato.data.local.datasource.SensorLocalDataSource
import es.wokis.didacticpotato.data.local.datasource.UserLocalDataSource
import es.wokis.didacticpotato.data.repository.AuthRepository
import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.data.repository.UserRepository
import es.wokis.didacticpotato.data.remote.datasource.SensorRemoteDataSource
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    // Token Provider
    single { TokenProvider(androidContext()) }

    // API Layer
    single { ApiClient(get()) }
    single<HttpClient> { get<ApiClient>().client }
    single { AuthApi(get()) }
    single { SensorApi(get()) }
    single { UserApi(get()) }

    // Remote Data Sources
    single { AuthRemoteDataSource(get()) }
    single { SensorRemoteDataSource(get()) }

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

    // Local Data Sources
    single { SensorLocalDataSource(get()) }
    single { UserLocalDataSource(get()) }

    // Repositories
    single<SensorRepository> { SensorRepository(get(), get()) }
    single { AuthRepository(get(), get()) }
    single { UserRepository(get(), get()) }
}
