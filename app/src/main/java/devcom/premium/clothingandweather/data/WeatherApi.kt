package devcom.premium.clothingandweather.data

import android.net.Uri
import devcom.premium.clothingandweather.common.WeatherType
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private const val OWM_LINK = "http://api.openweathermap.org/"
private const val OWN_DATA = "data/2.5/"
private const val OWN_IMG = "img/w/"
private const val OWM_TYPE_WEATHER = "weather"
private const val OWM_TYPE_FORECAST = "forecast"
private const val OWM_APP_ID = "a504db88a71fcc06534c4a94f415d98a"

/**
 * Вспомогательный класс для работы с API openweathermap.org
 */
class WeatherApi(private val city: String) {

    fun jsonObject(@WeatherType type: Int) =
        if (type in WeatherType.all)
            getWeatherJSON(weatherUrl(type))
        else
            null

    private fun getWeatherJSON(url: URL?): JSONObject? {
        if (url != null) {
            val connection: HttpURLConnection? = urlConnection(url)
            if (connection != null) {
                try {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))

                    val json = StringBuffer(1024)
                    var tmp: String?
                    do {
                        tmp = reader.readLine()
                        if (tmp == null)
                            break
                        json.append(tmp).append("\n")
                    } while (true)
                    reader.close()

                    val data = JSONObject(json.toString())
                    return if (isCorrectData(data)) {
                        return data
                    } else null
                } catch (e: Exception) {
                    return null
                } finally {
                    connection.disconnect()
                }
            } else return null
        } else return null
    }

    private fun weatherUrl(@WeatherType type: Int): URL? {
        return if (type in WeatherType.all) {
            val linkPart =
                if (type in WeatherType.allForecast)
                    OWM_TYPE_FORECAST
                else
                    OWM_TYPE_WEATHER

            URL("$OWM_LINK$OWN_DATA$linkPart?q=$city&units=metric&APPID=$OWM_APP_ID")
        } else
            null
    }

    fun iconUrl(imgId: String) : Uri = Uri.parse("$OWM_LINK$OWN_IMG$imgId.png")

    private fun urlConnection(url: URL): HttpURLConnection? {
        return try {
            val connection: HttpURLConnection? = url.openConnection() as HttpURLConnection
            connection?.requestMethod = "GET"
            connection?.connectTimeout = 10000
            connection?.readTimeout = 10000
            connection
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Проверка полученных данных
     * @param data объект данных
     * @return true, если данные не повреждены
     */
    private fun isCorrectData(data: JSONObject) = data.getInt("cod") == 200
}