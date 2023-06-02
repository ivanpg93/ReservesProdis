package cat.copernic.prodis.reservesprodis

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import cat.copernic.prodis.reservesprodis.databinding.FrgReservaVehiculoBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ReservaVehiculo : Fragment() {
    // Binding
    private lateinit var binding: FrgReservaVehiculoBinding

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

    // Arguments
    private lateinit var args: ReservaVehiculoArgs

    // Checkbox de horarios y textView de horas
    private lateinit var horarios: List<CheckBox>
    private lateinit var horas: List<TextView>
    private var enabledColor: ColorStateList? = null
    private var whiteColor: ColorStateList? = null

    // Datos Reserva
    private var idReserva: String = ""
    private val tipoReserva: String = "vehiculo"
    private var nombreReserva: String = ""
    private var nombreReservaOriginal: String = ""
    private var horaInicial: String = ""
    private var horaFinal: String = ""
    private var finalArr = mutableListOf(false, false, false, false, false, false, false)

    // Obtener datos de fechas
    @RequiresApi(Build.VERSION_CODES.O)
    private var date = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    var dia = date.format(DateTimeFormatter.ofPattern("dd"))

    @RequiresApi(Build.VERSION_CODES.O)
    var mes = date.format(DateTimeFormatter.ofPattern("MM"))

    @RequiresApi(Build.VERSION_CODES.O)
    var anyo = date.format(DateTimeFormatter.ofPattern("yyyy"))

    // Obtener fecha y hora actual
    private var dateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    // Obtener sólo la hora y minuto actual
    private var horaMinuto: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    // Juntar fecha actual
    private var fechaReserva: String = "$dia/$mes/$anyo"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        println(fechaReserva)
        // Binding
        binding = FrgReservaVehiculoBinding.inflate(layoutInflater)

        // Usar distinto color según esté activado el modo noche
        enabledColor = context?.let { ContextCompat.getColorStateList(it, R.color.btn_rojo) }
        whiteColor = context?.let { ContextCompat.getColorStateList(it, R.color.white) }
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                enabledColor =
                    context?.let { ContextCompat.getColorStateList(it, R.color.dt_btn_rojo) }
                whiteColor = context?.let { ContextCompat.getColorStateList(it, R.color.dt_white) }
            }
        }

        // Importar las variables de los arguments
        args = ReservaVehiculoArgs.fromBundle(requireArguments())
        nombreReserva = args.argVehiculoName
        nombreReservaOriginal = args.argVehiculo

        // Obtener la id de la reserva
        idReserva = "$nombreReservaOriginal$dia$mes$anyo$horaMinuto"

        // Horarios
        horarios = listOf(
            binding.checkbox09To10,
            binding.checkbox10to11,
            binding.checkbox11to12,
            binding.checkbox12to13,
            binding.checkbox14to15,
            binding.checkbox15to16,
            binding.checkbox16to17
        )

        // Horas
        horas = listOf(
            binding.textView09to10,
            binding.textView10to11,
            binding.textView11to12,
            binding.textView12to13,
            binding.textView14to15,
            binding.textView15to16,
            binding.textView16to17
        )

        // Si el usuario existe pero nunca ha rellenado su perfil
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email!!
            db.collection("users").document(email).get().addOnSuccessListener { result ->
                // Comprobar que el usuario no está deshabilitado
                if (!(result.get("enabled") as Boolean)) {
                    utils.notificationUserDisabled(requireContext(), resources)
                    auth.signOut()
                    requireActivity().finish()
                }
            }
        }

        // Deshabilitar que no se pueda escoger una fecha anterior al día de hoy
        binding.calendarView.minDate = System.currentTimeMillis()

        limpiarReservas()
        checkReservas()
        checkReservasToday()

        binding.reservarButton.setOnClickListener {
            checkHorario()
        }
        return binding.root
    }

    // Borrar las reservas anteriores al día actual
    private fun limpiarReservas() {
        // Asignamos el dia, mes y año de la fecha de hoy para luego compararlos por separado
        val dia = dia
        val mes = mes
        val anyo = anyo

        // Borrar reserva del global
        db.collection("reservas").document("vehiculos").collection(nombreReservaOriginal)
            .get().addOnSuccessListener { result ->
                for (document in result) {
                    // Asignamos el dia, mes y año de la fecha de la reserva para compararlos por separado
                    val day = document.get("fecha").toString().substring(0, 2).toInt()
                    val month = document.get("fecha").toString().substring(3, 5).toInt()
                    val year = document.get("fecha").toString().substring(6).toInt()

                    if (year < Integer.parseInt(anyo)) {
                        document.reference.delete()
                    } else {
                        if (month < Integer.parseInt(mes)) {
                            document.reference.delete()
                        } else {
                            if (day < Integer.parseInt(dia)) {
                                document.reference.delete()
                            }
                        }
                    }
                }
            }

        // Borrar reserva del usuario
        db.collection("users").document(utils.getEmail(auth)).collection("misreservas")
            .get().addOnSuccessListener { result ->
                for (document in result) {
                    if (document.get("tipo") == "vehiculo") {
                        // Asignamos el dia, mes y año de la fecha de la reserva para compararlos por separado
                        val day = document.get("fecha").toString().substring(0, 2).toInt()
                        val month = document.get("fecha").toString().substring(3, 5).toInt()
                        val year = document.get("fecha").toString().substring(6).toInt()
                        if (year < Integer.parseInt(anyo)) {
                            document.reference.delete()
                        } else {
                            if (month < Integer.parseInt(mes)) {
                                document.reference.delete()
                            } else {
                                if (day < Integer.parseInt(dia)) {
                                    document.reference.delete()
                                }
                            }
                        }
                    }
                }
            }
    }

    // Vaciar todos los checkbox y poner el color de la fuente en negro
    private fun habilitarCheckbox() {
        horarios.forEachIndexed { index, checkBox ->
            checkBox.isEnabled = true
            checkBox.isChecked = false
            horas[index].setTextColor(context?.let {
                whiteColor
            })
            horarios[index].buttonTintList = whiteColor
        }
    }

    // Función que asigna las horas marcadas para la reserva
    private fun checkHorario() {
        var contador = 0
        var horasReservadas = 0

        horarios.forEachIndexed { index, checkBox ->
            if (contador in 0..6) {
                if (horasReservadas == 0) {
                    if (checkBox.isChecked) {
                        finalArr[index] = true
                        horaInicial = horas[index].text as String
                        horaFinal = horas[index].text as String
                        horasReservadas++
                    }
                } else {
                    if (checkBox.isChecked) {
                        finalArr[index] = true
                        horaFinal = horas[index].text as String
                    }
                }
                contador++
            } else {
                return@checkHorario
            }
        }
        if (horasReservadas == 0) {
            context?.let { utils.showAlert(R.string.errorGenericTitle, R.string.alertReserva, it) }
        } else {
            horaInicial = horaInicial.substring(0, 2) + ":00"
            horaFinal = horaFinal.substring(horaFinal.length - 3, horaFinal.length - 1) + ":00"
            // Guardamos la reserva en la BBDD de Reservas
            db.collection("reservas").document("vehiculos")
                .collection(nombreReservaOriginal).document(idReserva)
                .set(
                    hashMapOf(
                        "id" to idReserva,
                        "tipo" to tipoReserva,
                        "name" to nombreReserva,
                        "fecha" to fechaReserva,
                        "horaInicial" to horaInicial,
                        "horaFinal" to horaFinal,
                        "user" to utils.getEmail(auth),
                        "horarios" to finalArr
                    )
                )

            // Guardamos la reserva en la BBDD del User
            db.collection("users").document(utils.getEmail(auth))
                .collection("misreservas").document(idReserva)
                .set(
                    hashMapOf(
                        "id" to idReserva,
                        "tipo" to tipoReserva,
                        "name" to nombreReserva,
                        "fecha" to fechaReserva,
                        "horaInicial" to horaInicial,
                        "horaFinal" to horaFinal,
                        "horarios" to finalArr
                    )
                )

            // Navegamos a Mis Reservas
            findNavController().navigate(ReservaVehiculoDirections.actionReservaVehiculoToMisReservas())
        }
    }

    // Checkea las reservas de la fecha seleccionada
    @SuppressLint("SimpleDateFormat")
    private fun checkReservas() {
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            habilitarCheckbox()
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            val dia = SimpleDateFormat("dd").format(calendar.time)
            val mes = SimpleDateFormat("MM").format(calendar.time)
            anyo = year.toString()

            fechaReserva = "$dia/$mes/$anyo"
            // idReserva = "$nombreReservaOriginal$dia$mes$anyo$horaMinuto"
            db.collection("reservas").document("vehiculos").collection(nombreReservaOriginal)
                .get().addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.get("fecha") == fechaReserva) {
                            val arrBool = utils.translateArr(document.get("horarios").toString())
                            arrBool.forEachIndexed { index, item ->
                                if (item) {
                                    horarios[index].isChecked
                                    horarios[index].isEnabled = false
                                    horarios[index].buttonTintList = enabledColor
                                    horas[index].setTextColor(context?.let {
                                        enabledColor
                                    })
                                } else {
                                    horarios[index].isChecked = false
                                    horarios[index].isEnabled
                                    horas[index].setTextColor(context?.let {
                                        whiteColor
                                    })
                                }
                            }
                        }
                    }
                }
        }
    }

    // Checkea antes de elegir día del Calendario, las reservas del día de hoy
    private fun checkReservasToday() {
        db.collection("reservas").document("vehiculos").collection(nombreReservaOriginal).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.get("fecha") == fechaReserva) {
                        val arrBool = utils.translateArr(document.get("horarios").toString())
                        arrBool.forEachIndexed { index, item ->
                            if (item) {
                                horarios[index].buttonTintList = enabledColor
                                horarios[index].isChecked
                                horarios[index].isEnabled = false
                                horas[index].setTextColor(context?.let {
                                    enabledColor
                                })
                            }
                        }
                    }
                }
            }
    }
}