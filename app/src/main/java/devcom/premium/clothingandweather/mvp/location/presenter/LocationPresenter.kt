package devcom.premium.clothingandweather.mvp.location.presenter

import devcom.premium.clothingandweather.data.storage.ConstStorage
import devcom.premium.clothingandweather.data.storage.PreferencesStorage
import devcom.premium.clothingandweather.mvp.location.view.ILocationView
import moxy.InjectViewState
import moxy.MvpPresenter
import java.util.*

@InjectViewState
class LocationPresenter(private val storage: PreferencesStorage) : MvpPresenter<ILocationView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showCity(storage.value(ConstStorage.TITLE_CITY, ConstStorage.DEFAULT_CITY))
    }

    /**
     * Обрабатывает нажатие на кнопку сохранения
     */
    fun onSaveLocation(city: String) {
        val trimmedCity = city.trim()
        if (trimmedCity.isBlank()) {
            viewState.switchCityValidationInfoVisibility(true)
            return
        }

        viewState.switchCityValidationInfoVisibility(false)
        trimmedCity.transformFirstCharToUpperCase().also {
            storage.putString(ConstStorage.TITLE_CITY, it)
            viewState.showCity(it)
        }
    }

    /**
     * Делает первую букву текста заглавной
     */
    private fun String.transformFirstCharToUpperCase(): String {
        if (isBlank()) {
            return this
        }

        return substring(0, 1).uppercase(Locale.ROOT) + substring(1)
    }
}