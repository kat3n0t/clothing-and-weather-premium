package devcom.premium.clothingandweather.mvp.location.view

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import devcom.premium.clothingandweather.databinding.ActivityLocationBinding
import devcom.premium.clothingandweather.mvp.ABaseMvpActivity
import devcom.premium.clothingandweather.mvp.location.presenter.LocationPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.koin.android.ext.android.get

class LocationActivity : ABaseMvpActivity(), ILocationView {

    @InjectPresenter
    internal lateinit var presenter: LocationPresenter

    @ProvidePresenter
    fun providePresenter(): LocationPresenter = get()

    private lateinit var binding: ActivityLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        binding.btnCheck.setOnClickListener {
            presenter.onSaveLocation(binding.etCity.text.toString())
        }
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

    override fun showCity(city: String) {
        binding.tvActiveCity.text = city
    }

    override fun switchCityValidationInfoVisibility(visible: Boolean) {
        binding.tvCheck.isVisible = visible
    }
}