package devcom.premium.clothingandweather.mvp.location.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ILocationView : MvpView {
    fun showCurrentLocation(location: String)
    fun showPreviousLocations(locations: List<String>)
    fun switchCityValidationInfoVisibility(visible: Boolean)
}