package es.wokis.didacticpotato.di

import es.wokis.didacticpotato.domain.usecase.GetLastSensorsUseCase
import es.wokis.didacticpotato.domain.usecase.LoginUseCase
import es.wokis.didacticpotato.domain.usecase.RegisterUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory<GetLastSensorsUseCase> { GetLastSensorsUseCase(get()) }
}