package devcom.premium.clothingandweather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.widget.RemoteViews
import devcom.premium.clothingandweather.common.IntExtensions
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.common.WeatherType
import devcom.premium.clothingandweather.data.WeatherApi
import devcom.premium.clothingandweather.mvp.main.view.MainActivity
import devcom.premium.clothingandweather.mvp.model.DataModel
import org.json.JSONObject

class AppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        var hasLoadedData: Boolean = false
        private var handler: Handler = Handler()

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            if (!internetAvailable(context)) {
                return
            }

            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val city = sharedPref.getString("city", "Kemerovo, RU")!!

            val weatherDegreePref = sharedPref.getString("degree", "0")!!.toInt()
            val weatherDegree = IntExtensions.toDegree(weatherDegreePref) ?: return

            object : Thread() {
                override fun run() {
                    try {
                        val weatherApi = WeatherApi(city)
                        val json: JSONObject = weatherApi.data(WeatherType.FORECAST_TODAY)
                            ?: throw Exception(context.getString(R.string.weather_data_not_found))

                        handler.post {
                            val dayJSON = DataModel.weatherDay(json, WeatherType.FORECAST_TODAY)
                                ?: throw Exception(context.getString(R.string.weather_data_not_found))

                            val mainDataObject = dayJSON.getJSONObject("main")
                            val windDataObject = dayJSON.getJSONObject("wind")

                            val weather = Weather(
                                mainDataObject.getDouble("temp"),
                                windDataObject.getDouble("speed"),
                                mainDataObject.getDouble("humidity")
                            )

                            val views =
                                RemoteViews(context.packageName, R.layout.app_widget)
                            views.setTextViewText(
                                R.id.appwidget_text_weather,
                                DataModel.title(weatherDegree, weather)
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
                            val intent = Intent(context, MainActivity::class.java)
                            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                            views.setOnClickPendingIntent(R.id.layout_widget, pendingIntent)

                            appWidgetManager.updateAppWidget(appWidgetId, views)
                            hasLoadedData = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }

        /**
         * Проверяет доступность интернет-соединения
         *
         * @param context [Context]
         */
        private fun internetAvailable(context: Context): Boolean {
            var result = false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                connectivityManager.run {
                    @Suppress("DEPRECATION")
                    connectivityManager.activeNetworkInfo?.run {
                        result = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            ConnectivityManager.TYPE_ETHERNET -> true
                            else -> false
                        }

                    }
                }
            }
            return result
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
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
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)
        }
    }
}