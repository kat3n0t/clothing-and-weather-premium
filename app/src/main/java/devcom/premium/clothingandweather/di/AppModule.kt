package devcom.premium.clothingandweather.di

import devcom.premium.clothingandweather.data.ModelRepository
import devcom.premium.clothingandweather.data.rest.WeatherApi
import devcom.premium.clothingandweather.mvp.main.presenter.MainPresenter
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::ModelRepository)
    singleOf(::WeatherApi)
    factoryOf(::MainPresenter)
}