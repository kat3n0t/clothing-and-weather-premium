package devcom.premium.clothingandweather

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import devcom.premium.clothingandweather.common.storage.ConstStorage
import devcom.premium.clothingandweather.common.storage.PreferencesStorage
import devcom.premium.clothingandweather.databinding.ActivityLocationBinding
import java.util.Locale

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding

    private lateinit var storage: PreferencesStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        storage = PreferencesStorage(this)
    }

    override fun onStart() {
        super.onStart()
        binding.btnCheck.setOnClickListener { onSaveLocation() }
        showCurrentCity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
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
        binding.tvActiveCity.text = city
    }

    /**
     * Обрабатывает нажатие на кнопку сохранения
     */
    private fun onSaveLocation() {
        binding.apply {
            val text = etCity.text.toString().trim()
            if (text.isBlank()) {
                tvCheck.visibility = View.VISIBLE
                return
            }

            tvCheck.visibility = View.GONE
            storage.putString(ConstStorage.TITLE_CITY, transformTextFirstCharToUpperCase(text))
        }
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