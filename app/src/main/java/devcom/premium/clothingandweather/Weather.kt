package devcom.premium.clothingandweather

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
class Weather(private val city: String) {

    /**
     * weatherType:
     * 0 - WEATHER; 1 - FORECAST
     */
    fun jsonObject(weatherType: Int) = getWeatherJSON(weatherUrl(weatherType))

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

    private fun weatherUrl(weatherType: Int) = when (weatherType) {
        0 -> URL("$OWM_LINK$OWN_DATA$OWM_TYPE_WEATHER?q=$city&units=metric&APPID=$OWM_APP_ID")
        1, 2 -> URL("$OWM_LINK$OWN_DATA$OWM_TYPE_FORECAST?q=$city&units=metric&APPID=$OWM_APP_ID")
        else -> null
    }

    fun iconUrl(imgId: String) = URL("$OWM_LINK$OWN_IMG$imgId.png")

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

    // код 200 возвращается в случае, если данные не повреждены
    private fun isCorrectData(data: JSONObject) = data.getInt("cod") == 200

    // Корректируем погоду по восприятию
    fun coldWindIndex(temperature: Double, speed: Double, humidity: Double): Double {
        // ветро-холодовый индекс, согласно формуле
        if ((temperature <= 10) && (speed > 4.8)) {
            var windColdIndex = 13.2 + 0.6215 * temperature - 11.37 * Math.pow(speed, 0.16) +
                    0.3965 * temperature * Math.pow(speed, 0.16)
            if (humidity > 60)
                windColdIndex -= 2
            windColdIndex = Math.rint(10 * windColdIndex) / 10
            return windColdIndex
        } else if (temperature >= 20) {
            var heatLoad = temperature
            if (humidity < 30)
                heatLoad -= 2
            else if (humidity > 50)
                heatLoad += 2
            if ((temperature > 30) && (humidity > 70))
                heatLoad += 2
            heatLoad = Math.rint(10 * heatLoad) / 10
            return heatLoad
        } else return temperature
    }

    fun convertCelsiusToFahrenheit(tempWithCelsius: Double = 0.0) = tempWithCelsius * 1.8 + 32
}