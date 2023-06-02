package cat.copernic.prodis.reservesprodis.models

class Reserva(val id: String, val tipo: String, val nombre: String, val fecha: String, val horaInicial: String, val horaFinal: String, val horarios: Array<Boolean>) {
    var reservaId: String? = null
    var reservaTipo: String? = null
    var reservaNombre: String? = null
    var reservaFecha: String? = null
    var reservaHoraInicio: String? = null
    var reservaHoraFinal: String? = null

    var arrList: Array<Boolean>? = null

    init {
        this.reservaId = id
        this.reservaTipo = tipo
        this.reservaNombre = nombre
        this.reservaFecha = fecha
        this.reservaHoraInicio = horaInicial
        this.reservaHoraFinal = horaFinal
        this.arrList = horarios
    }
}