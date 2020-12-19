package devcom.premium.clothingandweather.common.storage

import android.content.Context

/**
 * Реализация хранилища SharedPreferences
 */
internal class PreferencesStorage(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)

    /**
     * Возвращает строковое значение или по умолчанию
     *
     * @param key ключ
     * @param defaultValue значение по умолчанию
     */
    fun value(key: String, defaultValue: String?) =
        sharedPreferences.getString(key, defaultValue)

    /**
     * Сохраняет строковое значение
     *
     * @param key ключ
     * @param value значение
     */
    fun putString(key: String, value: String?) =
        sharedPreferences.edit().putString(key, value).apply()

    /**
     * Возвращает целочисленное значение или по умолчанию
     *
     * @param key ключ
     * @param defaultValue значение по умолчанию
     */
    fun value(key: String, defaultValue: Int) =
        sharedPreferences.getInt(key, defaultValue)

    companion object {
        private const val APP_PREFERENCES = "caw_preferences"
    }
}