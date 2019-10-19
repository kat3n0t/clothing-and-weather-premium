package devcom.premium.clothingandweather.mvp.main.presenter

interface IMainPresenter {
    fun onStart()
    fun updateAPIConnection()
    fun onLaunchLocation()
    fun onLaunchPreferences()
}