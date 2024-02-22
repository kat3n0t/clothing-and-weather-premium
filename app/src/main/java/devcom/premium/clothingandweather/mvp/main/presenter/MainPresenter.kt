package devcom.premium.clothingandweather.mvp.main.presenter

import android.content.Context
import android.os.Handler
import android.os.Looper
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.common.Clothes
import devcom.premium.clothingandweather.common.DataNotFoundException
import devcom.premium.clothingandweather.data.ClothingConfig
import devcom.premium.clothingandweather.data.DataModel
import devcom.premium.clothingandweather.data.ModelRepository
import devcom.premium.clothingandweather.data.WeatherConfig
import devcom.premium.clothingandweather.data.storage.ConstStorage
import devcom.premium.clothingandweather.data.storage.PreferencesStorage
import devcom.premium.clothingandweather.domain.Degree
import devcom.premium.clothingandweather.domain.Gender
import devcom.premium.clothingandweather.domain.Style
import devcom.premium.clothingandweather.domain.WeatherType
import devcom.premium.clothingandweather.mvp.main.view.IMainView
import moxy.InjectViewState
import moxy.MvpPresenter
import kotlin.concurrent.thread

@InjectViewState
class MainPresenter(
    private val repository: ModelRepository, private val storage: PreferencesStorage
) : MvpPresenter<IMainView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        showDefaultModel()
    }

    override fun detachView(view: IMainView?) {
        handler.removeCallbacksAndMessages(null)
        super.detachView(view)
    }

    fun updateAPIConnection(context: Context) {
        val genderPref =
            storage.value(ConstStorage.TITLE_GENDER, ConstStorage.DEFAULT_VALUE).toInt()
        val gender = Gender.values().firstOrNull { it.ordinal == genderPref } ?: return

        val stylePref = storage.value(ConstStorage.TITLE_STYLE, ConstStorage.DEFAULT_VALUE).toInt()
        val style = Style.values().firstOrNull { it.ordinal == stylePref } ?: return

        val weatherDegreePref =
            storage.value(ConstStorage.TITLE_DEGREE, ConstStorage.DEFAULT_VALUE).toInt()
        val weatherDegree =
            Degree.values().firstOrNull { it.ordinal == weatherDegreePref } ?: return

        val weatherTypePref =
            storage.value(ConstStorage.TITLE_DATE, ConstStorage.DEFAULT_VALUE).toInt()
        val weatherType =
            WeatherType.values().firstOrNull { it.ordinal == weatherTypePref } ?: return

        val clothingConfig = ClothingConfig(gender, style)
        val weatherConfig = WeatherConfig(weatherDegree, weatherType)

        val city = storage.value(ConstStorage.TITLE_CITY, ConstStorage.DEFAULT_CITY)


        viewState.switchInfoVisibility(false)
        viewState.switchLoadingVisibility(true)
        viewState.title(context.getString(R.string.loading))

        handler.removeCallbacksAndMessages(null)
        val clothes = Clothes(clothingConfig)
        thread {
            try {
                val weatherData =
                    repository.weatherData(weatherConfig.type, city) ?: throw DataNotFoundException(
                        context
                    )

                val weather = weatherData.weather
                val iconUri = weatherData.iconUri

                val perceivedTemp = weather.temperatureCelsiusPerception()

                handler.post {
                    with(viewState) {
                        title(DataModel.title(weatherConfig.degree, weather))
                        setTextInfo(weather)
                        loadModel(clothes.clothesId(perceivedTemp))
                        loadIcon(iconUri)

                        switchLoadingVisibility(false)
                        switchInfoVisibility(true)
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    e.message?.let {
                        if (e is DataNotFoundException) {
                            viewState.title(it)
                        }
                    }
                    with(viewState) {
                        loadModel(clothes.clothesId(Clothes.INVALID_TEMPERATURE))
                        switchLoadingVisibility(false)
                        switchInfoVisibility(false)
                    }
                }
            }
        }
    }

    private fun showDefaultModel() {
        val genderPref =
            storage.value(ConstStorage.TITLE_GENDER, ConstStorage.DEFAULT_VALUE).toInt()
        val gender = Gender.values()[genderPref]
        viewState.loadModel(if (gender == Gender.MAN) R.drawable.man_default else R.drawable.woman_default)
    }

    companion object {
        private val handler = Handler(Looper.getMainLooper())
    }
}