package cat.copernic.prodis.reservesprodis.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cat.copernic.prodis.reservesprodis.models.CurrUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class PerfilVM() : ViewModel() {
    private var _currUser = MutableLiveData<CurrUser>()
    val currUser: MutableLiveData<CurrUser>
        get() = _currUser

    private var auth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        val email = auth.currentUser!!.email!!
        db.collection("users")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                _currUser.value = CurrUser(
                    email,
                    admin = document.get("admin") as Boolean,
                    enabled = document.get("enabled") as Boolean,
                    document.get("nombre").toString(),
                    document.get("avatar").toString(),
                    document.get("dni").toString(),
                    document.get("servicio").toString(),
                    document.get("telefono").toString()
                )
            }
    }

    fun setUserName(name: String) {
        db.collection("users").document(auth.currentUser!!.email!!).update(mapOf("nombre" to name))
    }

    fun setUserTlf(tlf: String) {
        db.collection("users").document(auth.currentUser!!.email!!).update(mapOf("telefono" to tlf))
    }

    fun setUserServicio(servicio: Int) {
        db.collection("users").document(auth.currentUser!!.email!!).update(mapOf("servicio" to servicio))
    }

    fun setUserAvatar(url: String) {
        db.collection("users").document(auth.currentUser!!.email!!).update(mapOf("avatar" to url))
    }
}
