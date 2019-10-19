package devcom.premium.clothingandweather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.widget.RemoteViews
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.data.WeatherApi
import devcom.premium.clothingandweather.mvp.main.view.MainActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

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
        private var weatherText = ""
        private var speedText = ""
        private var humidityText = ""

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val city = sharedPref.getString("city", "Kemerovo, RU")
            val weatherDegree = sharedPref.getString("degree", "0").toInt()

            object : Thread() {
                override fun run() {
                    val cm =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    if (cm.activeNetworkInfo.isConnected) {
                        try {
                            val weatherApi = WeatherApi(city)
                            val json: JSONObject = weatherApi.jsonObject(1)
                                ?: throw Exception(context.getString(R.string.weather_data_not_found))

                            handler.post {
                                var mainDataObject: JSONObject? = null
                                var windDataObject: JSONObject? = null

                                val list = json.getJSONArray("list")
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                val currentDate = dateFormat.format(Calendar.getInstance().time)
                                for (i in 1 until json.getInt("cnt") - 1) {
                                    if (list.getJSONObject(i).getString("dt_txt").contains("$currentDate 1") or
                                        list.getJSONObject(i).getString("dt_txt").contains("$currentDate 2")
                                    ) {
                                        val weatherDay = list.getJSONObject(i)
                                        mainDataObject = weatherDay.getJSONObject("main")
                                        windDataObject = weatherDay.getJSONObject("wind")
                                        break
                                    }
                                }
                                if ((mainDataObject == null) || (windDataObject == null))
                                    throw Exception(context.getString(R.string.weather_data_not_found))

                                val weather = Weather(
                                    mainDataObject.getDouble("temp"),
                                    windDataObject.getDouble("speed"),
                                    mainDataObject.getDouble("humidity")
                                )

                                speedText =
                                    context.getString(R.string.wind) + " = " + String.format(
                                        "%.1f",
                                        weather.windSpeed
                                    ).replace(
                                        ',', '.'
                                    ) + " " + context.getString(R.string.meter_sec)

                                humidityText =
                                    context.getString(R.string.humidity) + " = " + weather.humidity.toString() + "%"

                                weatherText = when (weatherDegree) {
                                    1 -> // celsius
                                        "${weather.temperatureCelsius} °C"
                                    2 -> // fahrenheit
                                        String.format(
                                            "%.1f",
                                            weather.temperatureFahrenheit
                                        ).replace(
                                            ',', '.'
                                        ) + " °F"
                                    else -> // celsius and fahrenheit
                                        "${weather.temperatureCelsius} °C / " + String.format(
                                            "%.1f",
                                            weather.temperatureFahrenheit
                                        ).replace(
                                            ',', '.'
                                        ) + " °F"
                                }

                                // переход на главную страницу приложения по клику
                                val intent = Intent(context, MainActivity::class.java)
                                val pendingIntent =
                                    PendingIntent.getActivity(context, 0, intent, 0)

                                val views =
                                    RemoteViews(context.packageName, R.layout.app_widget)
                                views.setTextViewText(
                                    R.id.appwidget_text_weather,
                                    weatherText
                                )
                                views.setTextViewText(R.id.appwidget_text_city, city)
                                views.setViewVisibility(
                                    R.id.appwidget_text_city,
                                    View.VISIBLE
                                )
                                views.setTextViewText(R.id.appwidget_text_speed, speedText)
                                views.setTextViewText(
                                    R.id.appwidget_text_humidity,
                                    humidityText
                                )
                                if (appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(
                                        OPTION_APPWIDGET_MIN_WIDTH
                                    ) > 250
                                ) {
                                    views.setViewVisibility(
                                        R.id.widget_layout_weather_data,
                                        View.VISIBLE
                                    )
                                    views.setViewVisibility(
                                        R.id.widget_layout_line,
                                        View.VISIBLE
                                    )
                                    views.setViewVisibility(
                                        R.id.appwidget_text_speed,
                                        View.VISIBLE
                                    )
                                    views.setViewVisibility(
                                        R.id.appwidget_text_humidity,
                                        View.VISIBLE
                                    )
                                } else {
                                    views.setViewVisibility(
                                        R.id.widget_layout_weather_data,
                                        View.GONE
                                    )
                                    views.setViewVisibility(
                                        R.id.widget_layout_line,
                                        View.GONE
                                    )
                                    views.setViewVisibility(
                                        R.id.appwidget_text_speed,
                                        View.GONE
                                    )
                                    views.setViewVisibility(
                                        R.id.appwidget_text_humidity,
                                        View.GONE
                                    )
                                }
                                views.setOnClickPendingIntent(
                                    R.id.layout_widget,
                                    pendingIntent
                                )
                                appWidgetManager.updateAppWidget(appWidgetId, views)
                                hasLoadedData = true
                            }
                        } catch (e: Exception) {

                        }
                    }
                }
            }.start()
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