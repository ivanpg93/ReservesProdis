package cat.copernic.prodis.reservesprodis

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cat.copernic.prodis.reservesprodis.adapters.ReservaRecyclerAdapter
import cat.copernic.prodis.reservesprodis.databinding.FrgReservasBinding
import cat.copernic.prodis.reservesprodis.models.Reserva
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Reservas : Fragment() {
    //Binding
    private lateinit var binding: FrgReservasBinding

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

    // Adapter
    private val myAdapter: ReservaRecyclerAdapter = ReservaRecyclerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frg_reservas, container, false)

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
                if (result.get("nombre") == "") {
                    findNavController().navigate(ReservasDirections.actionReservasToPerfil())
                }
            }
        }

        setupRecyclerView()

        return binding.root
    }

    // Función que actualiza el RecyclerView
    private fun setupRecyclerView() {
        //Especifiquem que els fills del RV seran del mateix tamany i així optimitzem la seva creació
        binding.rvReservas.setHasFixedSize(true)

        //indiquem que el RV es mostrarà en format llista
        binding.rvReservas.layoutManager = LinearLayoutManager(activity)

        //generem el adapter
        myAdapter.ReservasRecyclerAdapter(getReservas(), requireActivity())

        //assignem el adapter al RV
        binding.rvReservas.adapter = myAdapter
    }

    private fun translateArr(word: String): Array<Boolean> {
        val replace = word.replace("[\\[\\]]".toRegex(), "")
        val newArr = listOf(replace.split(", "))

        val finalArr: Array<Boolean> = arrayOf(false, false, false, false, false, false, false)
        for (i in newArr[0].indices) {
            finalArr[i] = newArr[0][i].toBoolean()
        }
        return finalArr
    }

    // RecyclerView para las reservas
    @SuppressLint("NotifyDataSetChanged")
    private fun getReservas(): MutableList<Reserva> {
        val reservas: MutableList<Reserva> = arrayListOf()
        db.collection("users").document(utils.getEmail(auth))
            .collection("misreservas").get().addOnSuccessListener { result ->
                for (document in result) {
                    // Traducir el array para poderlo gestionar
                    reservas.add(
                        Reserva(
                            document.get("id").toString(),
                            document.get("tipo").toString(),
                            document.get("name").toString(),
                            document.get("fecha").toString(),
                            document.get("horaInicial").toString(),
                            document.get("horaFinal").toString(),
                            translateArr(document.get("horarios").toString())
                        )
                    )
                }
                myAdapter.notifyDataSetChanged()
            }
        return reservas
    }
}