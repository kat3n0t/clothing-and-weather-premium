package devcom.premium.clothingandweather

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import devcom.premium.clothingandweather.common.storage.ConstStorage
import devcom.premium.clothingandweather.common.storage.PreferencesStorage
import kotlinx.android.synthetic.main.activity_location.*
import java.util.Locale

class LocationActivity : AppCompatActivity() {

    private lateinit var storage: PreferencesStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        storage = PreferencesStorage(this)
    }

    override fun onStart() {
        super.onStart()
        btnCheck.setOnClickListener { onSaveLocation() }
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

    /**
     * Отображает текущий город
     */
    private fun showCurrentCity() {
        val city = storage.value(ConstStorage.TITLE_CITY, ConstStorage.DEFAULT_CITY)!!
        tvActiveCity.text = city
    }

    /**
     * Обрабатывает нажатие на кнопку сохранения
     */
    private fun onSaveLocation() {
        val text = etCity.text.toString().trim()
        if (text.isBlank()) {
            tvCheck.visibility = View.VISIBLE
            return
        }

        tvCheck.visibility = View.GONE
        storage.putString(ConstStorage.TITLE_CITY, transformTextFirstCharToUpperCase(text))
        showCurrentCity()
    }

    /**
     * Делает первую букву текста заглавной
     */
    private fun transformTextFirstCharToUpperCase(text: String): String {
        if (text.isBlank()) {
            return text
        }

        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1)
    }
}