package es.wokis.didacticpotato.di

import es.wokis.didacticpotato.ui.auth.LoginViewModel
import es.wokis.didacticpotato.ui.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::LoginViewModel)
}
