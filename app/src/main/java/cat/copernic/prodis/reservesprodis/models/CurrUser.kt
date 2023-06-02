package cat.copernic.prodis.reservesprodis.models

data class CurrUser(val email:String, val admin:Boolean, val enabled:Boolean, val name: String, val avatar:String, val dni: String, val servicio: String, val tlf: String){
    private var userEmail: String = email
    private var userAdmin: Boolean = admin
    private var userEnabled: Boolean = enabled
    private var userName: String = name
    private var userAvatar: String = avatar
    private var userDni: String = dni
    private var userServicio: String = servicio
    private var userTlf: String = tlf
    private var test : String = tlf

    fun getUserEmail(): String {
        return userEmail
    }

    fun getUserEnabled(): Boolean {
        return userEnabled
    }

    fun getUserName(): String {
        return userName
    }

    fun getUserAvatar() : String {
        return userAvatar
    }

    fun getUserDni() : String {
        return userDni
    }

    fun getUserServicio() : String {
        return userServicio
    }

    fun getUserTlf() : String {
        return userTlf
    }
}