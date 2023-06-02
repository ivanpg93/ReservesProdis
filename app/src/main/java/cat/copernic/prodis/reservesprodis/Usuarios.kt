package cat.copernic.prodis.reservesprodis

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import cat.copernic.prodis.reservesprodis.adapters.UsuarioRecyclerAdapter
import cat.copernic.prodis.reservesprodis.databinding.FrgUsuariosBinding
import cat.copernic.prodis.reservesprodis.models.Usuario
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Usuarios : Fragment() {
    // Binding
    private lateinit var binding: FrgUsuariosBinding

    // Autentificacion Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

    // Adapter
    private val myAdapter: UsuarioRecyclerAdapter = UsuarioRecyclerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frg_usuarios, container, false)

        setupRecyclerView()

        return binding.root
    }

    // Función que actualiza el RecyclerView
    private fun setupRecyclerView() {
        //Especifiquem que els fills del RV seran del mateix tamany i així optimitzem la seva creació
        binding.rvUsuarios.setHasFixedSize(true)

        //indiquem que el RV es mostrarà en format llista
        binding.rvUsuarios.layoutManager = LinearLayoutManager(activity)

        //generem el adapter
        myAdapter.UsuariosReclyclerAdapter(getUsuarios(), requireActivity())

        //assignem el adapter al RV
        binding.rvUsuarios.adapter = myAdapter
    }

    // Función que accede a todos los usuarios de la APP y los muestra en una MutableList
    @SuppressLint("NotifyDataSetChanged")
    private fun getUsuarios(): MutableList<Usuario> {
        val usuarios: MutableList<Usuario> = arrayListOf()
        GlobalScope.launch(Dispatchers.IO) {
            delay(500L)
            db.collection("users").document(utils.getEmail(auth)).get()
                .addOnSuccessListener { document ->
                    usuarios.add(
                        Usuario(
                            utils.getEmail(auth),
                            document.get("admin") as Boolean,
                            document.get("enabled") as Boolean,
                            document.get("nombre").toString()
                        )
                    )
                }
            db.collection("users").get().addOnSuccessListener { result ->
                for (document in result) {
                    if (document.id != auth.currentUser!!.email!!) {
                        usuarios.add(
                            Usuario(
                                document.id,
                                document.get("admin") as Boolean,
                                document.get("enabled") as Boolean,
                                document.get("nombre").toString()
                            )
                        )
                    }
                }
                myAdapter.notifyDataSetChanged()
            }
        }
        return usuarios
    }
}