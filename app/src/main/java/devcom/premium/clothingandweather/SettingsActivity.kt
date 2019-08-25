package devcom.premium.clothingandweather

import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.core.app.NavUtils

class SettingsActivity : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) fragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            NavUtils.navigateUpFromSameTask(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}