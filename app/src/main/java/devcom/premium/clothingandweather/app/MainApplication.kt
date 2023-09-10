package devcom.premium.clothingandweather.app

import android.app.Application
import devcom.premium.clothingandweather.di.appModule
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}