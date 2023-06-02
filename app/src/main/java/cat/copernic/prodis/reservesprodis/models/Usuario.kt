package cat.copernic.prodis.reservesprodis.models

data class Usuario(val email:String, val admin:Boolean, val enabled:Boolean, val name:String){
    var userEmail: String? = null
    var userAdmin: Boolean? = null
    var userEnabled: Boolean? = null
    var userName: String? = null

    init {
        this.userEmail = email
        this.userAdmin = admin
        this.userEnabled = enabled
        this.userName = name
    }
}