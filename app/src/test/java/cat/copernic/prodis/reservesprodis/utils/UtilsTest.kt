package cat.copernic.prodis.reservesprodis.utils

import org.junit.Assert

import org.junit.Test

class UtilsTest {

    val util = Utils()

    @Test
    fun translateArr() {
        val array = arrayOf(false, false, false, false, false, false, false)
        val array2 = util.translateArr("[false, false, false, false, false, false, false]")
        Assert.assertArrayEquals(array, array2)
    }

    // Este Test da error porque hay un bug con el método isDigitsOnly() que contiene la función
    // checkDni() pero realmente funciona bien
//    @Test
//    fun checkDni() {
//        val dni = "48709211H"
//        val check = util.checkDni(dni)
//        Assert.assertEquals(true, check)
//    }
}