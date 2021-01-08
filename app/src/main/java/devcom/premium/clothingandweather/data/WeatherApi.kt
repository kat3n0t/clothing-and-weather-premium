package devcom.premium.clothingandweather.data

import android.net.Uri
import devcom.premium.clothingandweather.common.WeatherType
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Вспомогательный класс для работы с API openweathermap.org
 */
class WeatherApi(private val city: String) {

    /**
     * Возвращает ссылку на иконку по идентификатору
     *
     * @param imgId идентификатор иконки
     * @return ссылка на иконку
     */
    fun iconUrl(imgId: String): Uri = Uri.parse("$OWM_LINK$OWN_IMG$imgId.png")

    /**
     * Возвращает [JSONObject] по типу погоды, если есть
     *
     * @param type тип погоды
     * @return [JSONObject]
     */
    fun data(type: WeatherType): JSONObject? {
        val url = dataUrl(type)
        return jsonObject(url)
    }

    /**
     * Возвращает [URL] по типу погоды
     *
     * @param type тип погоды
     * @return [URL]
     */
    private fun dataUrl(type: WeatherType): URL {
        val linkPart = if (type == WeatherType.WEATHER) OWM_TYPE_WEATHER else OWM_TYPE_FORECAST
        return URL("$OWM_LINK$OWN_DATA$linkPart?q=$city&units=metric&APPID=$OWM_APP_ID")
    }

    /**
     * Возвращает [JSONObject] по ссылке, если есть
     *
     * @param url ссылка
     * @return [JSONObject]
     */
    private fun jsonObject(url: URL): JSONObject? {
        val connection: HttpURLConnection = urlConnection(url) ?: return null
        var data: JSONObject? = null
        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val json = StringBuffer()
            reader.forEachLine {
                json.append(it).append("\n")
            }
            reader.close()

            data = JSONObject(json.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
            return if (data == null || !isCorrectData(data)) null else data
        }
    }

    /**
     * Возвращает соединение по ссылке, если есть
     *
     * @param url ссылка
     * @return [HttpURLConnection]
     */
    private fun urlConnection(url: URL): HttpURLConnection? {
        return try {
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Проверяет получаемые данные
     *
     * @param data объект данных
     * @return true, если данные не повреждены
     */
    private fun isCorrectData(data: JSONObject) = data.getInt("cod") == 200

    companion object {
        private const val OWM_LINK = "https://api.openweathermap.org/"
        private const val OWN_DATA = "data/2.5/"
        private const val OWN_IMG = "img/w/"
        private const val OWM_TYPE_WEATHER = "weather"
        private const val OWM_TYPE_FORECAST = "forecast"
        private const val OWM_APP_ID = "a504db88a71fcc06534c4a94f415d98a"
    }
}