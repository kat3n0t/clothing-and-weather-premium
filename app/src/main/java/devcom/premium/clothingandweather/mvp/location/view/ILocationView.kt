package devcom.premium.clothingandweather.mvp.location.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface ILocationView : MvpView {
    fun showCity(city: String)
    fun switchCityValidationInfoVisibility(visible: Boolean)
}