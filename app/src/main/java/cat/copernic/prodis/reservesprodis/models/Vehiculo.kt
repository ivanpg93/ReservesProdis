package cat.copernic.prodis.reservesprodis.models

class Vehiculo(val originalName: String, val title: String, val capacity: String, val image: String, val desc: String, val icons: Array<Boolean>, val enabled: Boolean, val matricula: String) {
    var vehiculoOriginalName: String? = null
    var vehiculoTitle: String? = null
    var vehiculoCapacity: String? = null
    var vehiculoImage: String? = null
    var vehiculoDesc: String? = null
    var vehiculoMatricula: String? = null
    var vehiculoEnabled: Boolean? = null

    var iconsList: Array<Boolean>? = null

    init {
        this.vehiculoOriginalName = originalName
        this.vehiculoTitle = title
        this.vehiculoCapacity = capacity
        this.vehiculoImage = image
        this.vehiculoDesc = desc
        this.iconsList = icons
        this.vehiculoMatricula = matricula
        this.vehiculoEnabled = enabled
    }
}
