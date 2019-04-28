package devcom.premium.clothingandweather

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import kotlinx.android.synthetic.main.activity_location.*

class LocationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        setupActionBar()
    }

    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        showCurrentCity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCurrentCity() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val city = sharedPref.getString("city", "Kemerovo, RU")
        textViewActiveCity.text = city
    }

    // Обрабатываем нажатие на кнопку сохранения
    fun onSaveLocation(@Suppress("UNUSED_PARAMETER") view: View) {
        textViewCheck.visibility = View.GONE
        val text = editTextCity.text.toString()
        if (text != "") {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPref.edit().putString("city", transformTextFirstCharToUpperCase(text)).apply()
            showCurrentCity()
        } else textViewCheck.visibility = View.VISIBLE
    }

    private fun transformTextFirstCharToUpperCase(text: String) = if (text != "") {
        text.substring(0, 1).toUpperCase() + text.substring(1)
    } else ""
}