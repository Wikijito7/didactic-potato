package es.wokis.didacticpotato.di

import es.wokis.didacticpotato.data.api.ApiClient
import es.wokis.didacticpotato.data.api.AuthApi
import es.wokis.didacticpotato.data.api.SensorApi
import es.wokis.didacticpotato.data.auth.AuthDataSource
import es.wokis.didacticpotato.data.auth.TokenProvider
import es.wokis.didacticpotato.data.repository.AuthRepository
import es.wokis.didacticpotato.data.repository.SensorRepository
import es.wokis.didacticpotato.data.sensor.SensorDataSource
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { TokenProvider(androidContext()) }
    single { ApiClient(get()) }
    single<HttpClient> { get<ApiClient>().client }
    single { AuthApi(get()) }
    single { SensorApi(get()) }
    single { AuthDataSource(get()) }
    single { SensorDataSource(get()) }
    single<SensorRepository> { SensorRepository(get()) }
    single { AuthRepository(get(), get()) }
}
