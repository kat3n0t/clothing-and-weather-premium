package devcom.premium.clothingandweather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import devcom.premium.clothingandweather.common.DataNotFoundException
import devcom.premium.clothingandweather.data.DataModel
import devcom.premium.clothingandweather.data.ModelRepository
import devcom.premium.clothingandweather.data.storage.ConstStorage
import devcom.premium.clothingandweather.data.storage.PreferencesStorage
import devcom.premium.clothingandweather.domain.Degree
import devcom.premium.clothingandweather.domain.WeatherType
import devcom.premium.clothingandweather.mvp.main.view.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.thread

class AppWidget : AppWidgetProvider(), KoinComponent {

    private val repository: ModelRepository by inject()

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds)
            updateAppWidget(repository, context, appWidgetManager, appWidgetId)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        val newWidth = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: return
        RemoteViews(context.packageName, R.layout.app_widget).apply {
            if ((newWidth > MIN_WIDTH_EXTENDED_WIDGET) and (hasLoadedData)) {
                setViewVisibility(R.id.widget_layout_weather_data, View.VISIBLE)
                setViewVisibility(R.id.widget_layout_line, View.VISIBLE)
                setViewVisibility(R.id.appwidget_text_speed, View.VISIBLE)
                setViewVisibility(R.id.appwidget_text_humidity, View.VISIBLE)
            } else {
                if (hasLoadedData) setViewVisibility(R.id.appwidget_text_city, View.VISIBLE)
                else setViewVisibility(R.id.appwidget_text_city, View.GONE)
                setViewVisibility(R.id.widget_layout_weather_data, View.GONE)
                setViewVisibility(R.id.widget_layout_line, View.GONE)
                setViewVisibility(R.id.appwidget_text_speed, View.GONE)
                setViewVisibility(R.id.appwidget_text_humidity, View.GONE)
            }
        }.also {
            appWidgetManager.updateAppWidget(appWidgetId, it)
        }
    }

    companion object {
        private const val REQUEST_CODE = 0
        private const val MIN_WIDTH_EXTENDED_WIDGET = 250

        private val handler = Handler(Looper.getMainLooper())

        private var hasLoadedData: Boolean = false

        private fun updateAppWidget(
            repository: ModelRepository,
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val storage = PreferencesStorage(context)
            val city = storage.value(ConstStorage.TITLE_CITY, ConstStorage.DEFAULT_CITY)

            val weatherDegreePref = storage
                .value(ConstStorage.TITLE_DEGREE, ConstStorage.DEFAULT_VALUE).toInt()
            val weatherDegree = Degree.values().firstOrNull { it.ordinal == weatherDegreePref }
                ?: return

            val views = RemoteViews(context.packageName, R.layout.app_widget)
            thread {
                try {
                    val weather = repository.weatherData(WeatherType.FORECAST_TODAY, city)?.weather
                        ?: throw DataNotFoundException(context)

                    handler.post {
                        views.setTextViewText(
                            R.id.appwidget_text_weather, DataModel.title(weatherDegree, weather)
                        )
                        views.setTextViewText(R.id.appwidget_text_city, city)
                        views.setViewVisibility(R.id.appwidget_text_city, View.VISIBLE)
                        views.setTextViewText(
                            R.id.appwidget_text_speed,
                            DataModel.infoWindSpeed(context, weather.windSpeed)
                        )
                        views.setTextViewText(
                            R.id.appwidget_text_humidity,
                            DataModel.infoHumidity(context, weather.humidity)
                        )

                        val bundle = appWidgetManager.getAppWidgetOptions(appWidgetId)
                        val visibility =
                            if (bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) > MIN_WIDTH_EXTENDED_WIDGET) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                        views.setViewVisibility(R.id.widget_layout_weather_data, visibility)
                        views.setViewVisibility(R.id.widget_layout_line, visibility)
                        views.setViewVisibility(R.id.appwidget_text_speed, visibility)
                        views.setViewVisibility(R.id.appwidget_text_humidity, visibility)

                        // переход на главную страницу приложения по клику
                        val pendingIntent = PendingIntent.getActivity(
                            context, REQUEST_CODE, Intent(context, MainActivity::class.java),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            else
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        views.setOnClickPendingIntent(android.R.id.background, pendingIntent)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        hasLoadedData = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                    views.apply {
                        setOnClickPendingIntent(
                            android.R.id.background, PendingIntent.getBroadcast(
                                context, REQUEST_CODE,
                                Intent(context, AppWidget::class.java).apply {
                                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                    putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId)
                                    )
                                },
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                else PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}