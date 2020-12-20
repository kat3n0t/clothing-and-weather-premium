package devcom.premium.clothingandweather

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

/**
 * Фрагмент настроек
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
