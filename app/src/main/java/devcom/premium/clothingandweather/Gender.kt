package devcom.premium.clothingandweather

annotation class Gender {
    companion object {
        const val MAN = 0
        const val WOMAN = 1

        val all = setOf(MAN, WOMAN)
    }
}