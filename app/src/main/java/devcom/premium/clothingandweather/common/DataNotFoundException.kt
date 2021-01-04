package devcom.premium.clothingandweather.common

import android.content.Context
import devcom.premium.clothingandweather.R

class DataNotFoundException(context: Context) :
    RuntimeException(context.getString(R.string.weather_data_not_found))