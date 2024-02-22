package devcom.premium.clothingandweather.mvp.location.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.databinding.ActivityLocationBinding
import devcom.premium.clothingandweather.mvp.ABaseMvpActivity
import devcom.premium.clothingandweather.mvp.location.presenter.LocationPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.koin.android.ext.android.get

class LocationActivity : ABaseMvpActivity(), ILocationView {

    private var dialog: AlertDialog? = null

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
        binding.lvPreviousLocations.setOnItemClickListener { adapterView, _, i, _ ->
            presenter.onSaveLocation(adapterView.getItemAtPosition(i) as String)
        }
        binding.lvPreviousLocations.setOnItemLongClickListener { adapterView, _, i, _ ->
            dialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.remove_location)
                .setNeutralButton(android.R.string.cancel) { _, _ -> }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    presenter.onRemoveLocation(adapterView.getItemAtPosition(i) as String)
                }.create().also {
                    it.show()
                }
            true
        }
    }

    override fun onDestroy() {
        dialog?.dismiss()
        super.onDestroy()
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

    override fun showCurrentLocation(location: String) {
        binding.tvActiveCity.text = location
    }

    override fun showPreviousLocations(locations: List<String>) {
        binding.lvPreviousLocations.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, locations)
    }

    override fun switchCityValidationInfoVisibility(visible: Boolean) {
        binding.tvCheck.isVisible = visible
    }
}