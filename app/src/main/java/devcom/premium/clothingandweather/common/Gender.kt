package devcom.premium.clothingandweather.common

annotation class Gender {
    companion object {
        const val MAN = 0
        const val WOMAN = 1

        val all = setOf(MAN, WOMAN)
    }
}