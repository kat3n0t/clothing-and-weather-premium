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
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if ((cm.activeNetworkInfo == null) or (!cm.activeNetworkInfo.isConnected)) {
                return
            }

            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val city = sharedPref.getString("city", "Kemerovo, RU")!!
            val weatherDegree = sharedPref.getString("degree", "0").toInt()

            object : Thread() {
                override fun run() {
                    try {
                        val weatherApi = WeatherApi(city)
                        val json: JSONObject = weatherApi.data(1)
                            ?: throw Exception(context.getString(R.string.weather_data_not_found))

                        handler.post {
                            val dayJSON = DataModel.weatherDay(json, 1)
                                ?: throw Exception(context.getString(R.string.weather_data_not_found))

                            val mainDataObject = dayJSON.getJSONObject("main")
                            val windDataObject = dayJSON.getJSONObject("wind")

                            val weather = Weather(
                                mainDataObject.getDouble("temp"),
                                windDataObject.getDouble("speed"),
                                mainDataObject.getDouble("humidity")
                            )

                            // переход на главную страницу приложения по клику
                            val intent = Intent(context, MainActivity::class.java)
                            val pendingIntent =
                                PendingIntent.getActivity(context, 0, intent, 0)

                            val views =
                                RemoteViews(context.packageName, R.layout.app_widget)
                            views.setTextViewText(
                                R.id.appwidget_text_weather,
                                DataModel.title(weatherDegree, weather)
                            )
                            views.setTextViewText(R.id.appwidget_text_city, city)
                            views.setViewVisibility(
                                R.id.appwidget_text_city,
                                View.VISIBLE
                            )
                            views.setTextViewText(
                                R.id.appwidget_text_speed,
                                DataModel.infoWindSpeed(context, weather.windSpeed)
                            )
                            views.setTextViewText(
                                R.id.appwidget_text_humidity,
                                DataModel.infoHumidity(context, weather.humidity)
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
                        e.printStackTrace()
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