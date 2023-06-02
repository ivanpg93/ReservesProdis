package cat.copernic.prodis.reservesprodis

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cat.copernic.prodis.reservesprodis.databinding.FrgConfiguracionBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.IllegalStateException

class Configuracion : Fragment() {
    //Binding
    private lateinit var binding: FrgConfiguracionBinding

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos
    private val db = Firebase.firestore

    //Storage
    val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.frg_configuracion, container, false)

        // Obtener la versión de la app y ponerla en el textView
        binding.numVersionText.text = BuildConfig.VERSION_NAME

        // Si el usuario existe pero nunca ha rellenado su perfil
        if (auth.currentUser != null) {
            val email = auth.currentUser!!.email!!
            db.collection("users").document(email).get().addOnSuccessListener { result ->
                if (result.get("nombre") == "") {
                    findNavController().navigate(ConfiguracionDirections.actionConfiguracionToPerfil())
                }
            }
        }

        // Fix que evita que la app crashee si el requiredContext() está vacío
        val context = context
            ?: throw IllegalStateException("Fragment $this not attached to a context.")

        // Colocar el logo del Nicolau Copèrnic
        // Primero elegir la imagen según el si está activado el modo noche o no
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> {
                storageRef.child("/app/NC_Logo.png").downloadUrl.addOnSuccessListener { url ->
                    Glide.with(context)
                        .load(url.toString())
                        .centerCrop()
                        .into(binding.copernicLogo)
                }
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                storageRef.child("/app/NC_Logo_Night.png").downloadUrl.addOnSuccessListener { url ->
                    Glide.with(context)
                        .load(url.toString())
                        .centerCrop()
                        .into(binding.copernicLogo)
                }
            }
        }
        return binding.root
    }
}