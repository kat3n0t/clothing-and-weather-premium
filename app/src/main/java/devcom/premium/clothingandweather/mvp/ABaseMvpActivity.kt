package devcom.premium.clothingandweather.mvp

import android.app.Activity
import android.content.Intent
import moxy.MvpAppCompatActivity

abstract class ABaseMvpActivity : MvpAppCompatActivity() {
    protected inline fun <reified T : Activity> startActivity(
        flags: List<Int>? = null,
        extra: Map<String, String>? = null
    ) = startActivity(Intent(this, T::class.java).apply {
        flags?.forEach { addFlags(it) }
        extra?.forEach { putExtra(it.key, it.value) }
    })
}
