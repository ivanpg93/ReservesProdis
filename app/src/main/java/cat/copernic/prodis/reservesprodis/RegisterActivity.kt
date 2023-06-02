package cat.copernic.prodis.reservesprodis

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cat.copernic.prodis.reservesprodis.databinding.ActivityRegisterBinding
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity: AppCompatActivity()  {
    // Binding
    private lateinit var binding: ActivityRegisterBinding
    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()
    // Base de datos
    private val db = Firebase.firestore
    // Utils
    private val utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Binding
        binding = DataBindingUtil.setContentView<ActivityRegisterBinding>(this, R.layout.activity_register)

        // Settear el botón de atras en la ActionBar
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Botón registro
        binding.registerButton.setOnClickListener { view: View ->
            val dni = binding.dniTextInput.text.toString().uppercase()
            val email = binding.emailTextInput.text.toString().lowercase()
            var passwd = binding.passwordTextInput.text.toString()

            // Comprobar que todos los campos están rellenados
            if (binding.dniTextInput.text!!.isNotEmpty() && binding.emailTextInput.text!!.isNotEmpty()
                && binding.passwordTextInput.text!!.isNotEmpty()) {
                if (!utils.checkDni(dni)) {
                    // Comprobar que el DNI es váido
                    utils.showAlert(R.string.errorGenericTitle, R.string.errorDniInvalid, this)
                } else if (!utils.checkLength(passwd, 4)) {
                    // Comprobar que la contraseña es de 4 dígitos
                    utils.showAlert(R.string.errorGenericTitle, R.string.errorPasswdInvalid, this)
                } else {
                    // Modificar la contraseña para que Firebase le guste y esté contento
                    passwd += "prodis"
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        // Añadir correo y contraseña al auth de Firebase
                        email, passwd
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            addUserToDDBB(dni, email)
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            utils.showAlert(R.string.errorGenericTitle, R.string.errorEmail, this)
                        }
                    }
                }
            } else {
                // Hay algún campo vacio
                utils.showAlert(R.string.errorGenericTitle, R.string.errorEmailPassEmpty, this)
            }
        }
    }

    fun addUserToDDBB(dni: String, email: String){
        // Añadir correo y DNI a la ddbb Firestore
        db.collection("users").document(email)
            .set(
                hashMapOf(
                    "dni" to dni,
                    "nombre" to "",
                    "telefono" to "",
                    "servicio" to 0,
                    "avatar" to "",
                    "admin" to false,
                    "enabled" to true
                )
            )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}