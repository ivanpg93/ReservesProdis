package cat.copernic.prodis.reservesprodis

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import cat.copernic.prodis.reservesprodis.databinding.FrgHomeBinding
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Home : Fragment() {
    // Binding
    private lateinit var binding: FrgHomeBinding

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

    // Obtener datos de fechas
    @RequiresApi(Build.VERSION_CODES.O)
    private var date = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    var dia = date.format(DateTimeFormatter.ofPattern("dd"))

    @RequiresApi(Build.VERSION_CODES.O)
    var mes = date.format(DateTimeFormatter.ofPattern("MM"))

    @RequiresApi(Build.VERSION_CODES.O)
    var anyo = date.format(DateTimeFormatter.ofPattern("yyyy"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Binding
        binding = FrgHomeBinding.inflate(layoutInflater)
        if (auth.currentUser != null) {
            // Si el usuario existe pero nunca ha rellenado su perfil (nombre y tlf vacío)
            val email = auth.currentUser!!.email!!
            db.collection("users").document(email).get().addOnSuccessListener { result ->
                // Comprobar que el usuario no está deshabilitado
                if (!(result.get("enabled") as Boolean)) {
                    utils.notificationUserDisabled(requireContext(), resources)
                    auth.signOut()
                    requireActivity().finish()
                }
                if (result.get("nombre") == "" || result.get("telefono") == "") {
                    findNavController().navigate(HomeDirections.actionPrincipalToPerfil())
                }
            }
            limpiarReservas()
        } else {
            binding.misReservas.isVisible = false
        }

        // Botón Salas
        binding.salasCardView.setOnClickListener { view: View ->
            view.findNavController().navigate(HomeDirections.actionPrincipalToSalas())
        }
        // Botón Vehiculos
        binding.vehiculosCardView.setOnClickListener { view: View ->
            view.findNavController().navigate(HomeDirections.actionPrincipalToVehiculos())
        }
        // Botón Mis Reservas
        binding.misReservas.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_principal_to_misReservas)
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun limpiarReservas() {
        // Asignamos el dia, mes y año de la fecha de hoy para luego compararlos por separado
        val dia = dia
        val mes = mes
        val anyo = anyo

        // Borrar reserva del usuario
        db.collection("users").document(utils.getEmail(auth)).collection("misreservas")
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
    }
}