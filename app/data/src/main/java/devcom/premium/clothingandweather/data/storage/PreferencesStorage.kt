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
     * Возвращает список строк или по умолчанию
     *
     * @param key ключ
     */
    fun value(key: String): List<String> =
        sharedPreferences.getString(key, "")?.split(LIST_DIVIDER) ?: listOf()

    /**
     * Сохраняет строковое значение
     *
     * @param key ключ
     * @param value значение
     */
    fun putString(key: String, value: String?) =
        sharedPreferences.edit { putString(key, value) }

    /**
     * Сохраняет список строк
     *
     * @param key ключ
     * @param value список
     */
    fun putStringList(key: String, value: List<String>) = sharedPreferences.edit {
        putString(key, value.joinToString(LIST_DIVIDER))
    }

    companion object {
        private const val LIST_DIVIDER = ";;StrangeDividerCaWPremium;;"
    }
}