package es.wokis.didacticpotato.di

import es.wokis.didacticpotato.domain.usecase.GetLastSensorsUseCase
import es.wokis.didacticpotato.domain.usecase.GetUserUseCase
import es.wokis.didacticpotato.domain.usecase.LoginUseCase
import es.wokis.didacticpotato.domain.usecase.RegisterUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    // UseCases - all factories (stateless)
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::GetLastSensorsUseCase)
    factoryOf(::GetUserUseCase)
}
