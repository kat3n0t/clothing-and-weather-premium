package devcom.premium.clothingandweather.mvp.location.presenter

import devcom.premium.clothingandweather.data.storage.ConstStorage
import devcom.premium.clothingandweather.data.storage.PreferencesStorage
import devcom.premium.clothingandweather.mvp.location.view.ILocationView
import moxy.InjectViewState
import moxy.MvpPresenter

@InjectViewState
class LocationPresenter(private val storage: PreferencesStorage) : MvpPresenter<ILocationView>() {

    private var currentLocation: String? = null

    private val locationsList
        get() = storage.value(ConstStorage.TITLE_PREV_LOCATIONS)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        currentLocation = storage.value(ConstStorage.TITLE_CITY, ConstStorage.DEFAULT_CITY).also {
            viewState.showCurrentLocation(it)
        }
        viewState.showPreviousLocations(locationsList)
    }

    /**
     * Обрабатывает нажатие на кнопку сохранения
     */
    fun onSaveLocation(location: String) {
        if (location.isBlank()) {
            viewState.switchCityValidationInfoVisibility(true)
            return
        }

        viewState.switchCityValidationInfoVisibility(false)

        val validLocation = location.trim().replaceFirstChar(Char::titlecase)

        val previousLocationsList = locationsList
        currentLocation?.let {
            if (it == validLocation || previousLocationsList.contains(it))
                return@let

            previousLocationsList.toMutableList()
                .addNewElementWithLimit(it)
                .also { changedList ->
                    storage.putStringList(ConstStorage.TITLE_PREV_LOCATIONS, changedList)
                    viewState.showPreviousLocations(changedList)
                }
        }

        currentLocation = validLocation.also {
            storage.putString(ConstStorage.TITLE_CITY, it)
            viewState.showCurrentLocation(it)
        }
    }

    fun onRemoveLocation(location: String) = locationsList.toMutableList().let { list ->
        list.removeAll { it == location }

        storage.putStringList(ConstStorage.TITLE_PREV_LOCATIONS, list)
        viewState.showPreviousLocations(list)
    }

    companion object {
        private const val PREVIOUS_LOCATIONS_MAX_COUNT = 3

        private fun <E, T : MutableList<E>> T.addNewElementWithLimit(element: E): T {
            add(0, element)
            if (size > PREVIOUS_LOCATIONS_MAX_COUNT)
                removeLastOrNull()
            return this
        }
    }
}