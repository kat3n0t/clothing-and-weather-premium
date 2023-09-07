package devcom.premium.clothingandweather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import devcom.premium.clothingandweather.common.ConnectionStateMonitor
import devcom.premium.clothingandweather.common.DataNotFoundException
import devcom.premium.clothingandweather.common.IntExtensions
import devcom.premium.clothingandweather.data.DataModel
import devcom.premium.clothingandweather.data.ModelRepository
import devcom.premium.clothingandweather.data.rest.WeatherApi
import devcom.premium.clothingandweather.data.storage.ConstStorage
import devcom.premium.clothingandweather.data.storage.PreferencesStorage
import devcom.premium.clothingandweather.domain.WeatherType
import devcom.premium.clothingandweather.mvp.main.view.MainActivity
import kotlin.concurrent.thread

class AppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        if (newOptions != null) {
            val views = RemoteViews(context?.packageName, R.layout.app_widget)
            if ((newOptions.getInt(OPTION_APPWIDGET_MIN_WIDTH) > 250) and (hasLoadedData)) {
                views.setViewVisibility(R.id.widget_layout_weather_data, View.VISIBLE)
                views.setViewVisibility(R.id.widget_layout_line, View.VISIBLE)
                views.setViewVisibility(R.id.appwidget_text_speed, View.VISIBLE)
                views.setViewVisibility(R.id.appwidget_text_humidity, View.VISIBLE)
            } else {
                if (hasLoadedData) views.setViewVisibility(R.id.appwidget_text_city, View.VISIBLE)
                else views.setViewVisibility(R.id.appwidget_text_city, View.GONE)
                views.setViewVisibility(R.id.widget_layout_weather_data, View.GONE)
                views.setViewVisibility(R.id.widget_layout_line, View.GONE)
                views.setViewVisibility(R.id.appwidget_text_speed, View.GONE)
                views.setViewVisibility(R.id.appwidget_text_humidity, View.GONE)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        var hasLoadedData: Boolean = false

        private val handler = Handler(Looper.getMainLooper())
        private val repository = ModelRepository(WeatherApi())

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val connectionStateMonitor = ConnectionStateMonitor(context)
            if (!connectionStateMonitor.networkAvailable()) {
                return
            }

            val storage = PreferencesStorage(context)
            val city = storage.value(ConstStorage.TITLE_CITY, ConstStorage.DEFAULT_CITY)!!

            val weatherDegreePref =
                storage.value(ConstStorage.TITLE_DEGREE, ConstStorage.DEFAULT_VALUE)!!.toInt()
            val weatherDegree = IntExtensions.toDegree(weatherDegreePref) ?: return

            thread {
                try {
                    val weather =
                        repository.weatherData(WeatherType.FORECAST_TODAY, city)?.weather
                            ?: throw DataNotFoundException(context)

                    handler.post {
                        val views = RemoteViews(context.packageName, R.layout.app_widget)
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
                        val visibility = if (bundle.getInt(OPTION_APPWIDGET_MIN_WIDTH) > 250) {
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
                            context, 0, Intent(context, MainActivity::class.java),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                PendingIntent.FLAG_IMMUTABLE
                            else 0
                        )
                        views.setOnClickPendingIntent(R.id.layout_widget, pendingIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        hasLoadedData = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}