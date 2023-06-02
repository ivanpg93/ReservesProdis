package cat.copernic.prodis.reservesprodis.models

class Sala(val originalName: String, val title: String, val capacity: String, val image: String, val desc: String, val enabled : Boolean, val icons: Array<Boolean>) {
    var salaOriginalName: String? = null
    var salaTitle: String? = null
    var salaCapacity: String? = null
    var salaImage: String? = null
    var salaDesc: String? = null
    var salaEnabled: Boolean? = null

    var iconsList: Array<Boolean>? = null

    init {
        this.salaOriginalName = originalName
        this.salaTitle = title
        this.salaCapacity = capacity
        this.salaImage = image
        this.salaDesc = desc
        this.salaEnabled = enabled
        this.iconsList = icons
    }
}
