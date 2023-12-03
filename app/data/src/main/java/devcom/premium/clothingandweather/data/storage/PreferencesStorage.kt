package devcom.premium.clothingandweather.data.storage

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * Реализация хранилища SharedPreferences
 */
class PreferencesStorage(context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Возвращает строковое значение или по умолчанию
     *
     * @param key ключ
     * @param defaultValue значение по умолчанию
     */
    fun value(key: String, defaultValue: String): String =
        sharedPreferences.getString(key, defaultValue) ?: defaultValue

    /**
     * Сохраняет строковое значение
     *
     * @param key ключ
     * @param value значение
     */
    fun putString(key: String, value: String?) =
        sharedPreferences.edit { putString(key, value) }
}