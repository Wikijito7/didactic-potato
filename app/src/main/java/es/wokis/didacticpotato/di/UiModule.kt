package es.wokis.didacticpotato.di

import es.wokis.didacticpotato.ui.auth.LoginViewModel
import es.wokis.didacticpotato.ui.auth.RegisterViewModel
import es.wokis.didacticpotato.ui.home.HomeViewModel
import es.wokis.didacticpotato.ui.profile.ProfileViewModel
import es.wokis.didacticpotato.ui.profile.edit.EditProfileViewModel
import es.wokis.didacticpotato.ui.profile.options.OptionsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    // ViewModels - viewModelOf automatically handles lifecycle
    viewModelOf(::HomeViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { EditProfileViewModel(androidContext(), get(), get()) }
    viewModelOf(::OptionsViewModel)
}
