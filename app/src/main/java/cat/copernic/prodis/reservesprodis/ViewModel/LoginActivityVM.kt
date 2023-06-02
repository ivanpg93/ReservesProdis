package cat.copernic.prodis.reservesprodis.ViewModel

import androidx.lifecycle.ViewModel

class LoginActivityVM : ViewModel() {
    private var dni : String = ""
    private var passwd : String = ""
    private var email : String = ""

    fun getDni() : String {
        return dni
    }

    fun setDni(dni: String) {
        this.dni = dni
    }

    fun getPasswd() : String {
        return passwd
    }

    fun setPasswd(passwd: String) {
        this.passwd = passwd
    }

    fun getEmail() : String {
        return email
    }

    fun setEmail(email: String) {
        this.email = email
    }
}

