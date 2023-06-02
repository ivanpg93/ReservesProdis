package cat.copernic.prodis.reservesprodis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import cat.copernic.prodis.reservesprodis.ViewModel.LoginActivityVM
import cat.copernic.prodis.reservesprodis.databinding.ActivityLoginBinding
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    // Binding
    private lateinit var binding: ActivityLoginBinding

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

    // ViewModel
    private lateinit var viewModel: LoginActivityVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Splash Screen
        installSplashScreen()

        // Si ya tenemos datos de sesión en la cache...
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            true
        } else {
            setContentView(R.layout.activity_login)
        }

        binding =
            DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this).get(LoginActivityVM::class.java)

        // Botón Conectar
        binding.connectButton.setOnClickListener {
            viewModel.setDni(binding.dniTextInput.text.toString().uppercase().trim())
            viewModel.setPasswd(binding.passwordTextInput.text.toString().lowercase().trim())

            if (binding.dniTextInput.text!!.isNotEmpty() && binding.passwordTextInput.text!!.isNotEmpty()) {
                if (!utils.checkDni(viewModel.getDni())) {
                    // Comprobar que el DNI es váido
                    utils.showAlert(R.string.errorGenericTitle, R.string.errorDniInvalid, this)
                } else if (!utils.checkLength(viewModel.getPasswd(), 4)) {
                    utils.showAlert(R.string.errorGenericTitle, R.string.errorPasswdInvalid, this)
                } else {
                    // Alargar la contraseña para que sea valida para el login/registro de google
                    viewModel.setPasswd(viewModel.getPasswd() + "prodis")
                    db.collection("users").get().addOnSuccessListener { result ->
                        // Recorrer t0do el documento buscando si coincide con algún DNI
                        var match: Boolean = false
                        for (document in result) {
                            // Comprobar que el DNI está en la DDBB
                            if (document.get("dni") == viewModel.getDni()) {
                                match = true
                                // Comprobar que el usuario no esté deshabilitado
                                if (document.get("enabled") as Boolean) {
                                    auth.signInWithEmailAndPassword(
                                        document.id, // Coger el email del json acorde al DNI
                                        viewModel.getPasswd(),
                                    ).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                            true
                                        } else {
                                            utils.showAlert(
                                                R.string.errorGenericTitle,
                                                R.string.errorDniPasswdWrong,
                                                this
                                            )
                                        }
                                    }
                                } else {
                                    utils.showAlert(
                                        R.string.errorGenericTitle,
                                        R.string.errorUserDisabled,
                                        this
                                    )
                                }
                            }
                        }
                        if (!match) {
                            utils.showAlert(
                                R.string.errorGenericTitle,
                                R.string.errorDniNotInDDBB,
                                this
                            )
                        }
                    }
                }
            } else {
                // Hay algún campo vacio
                utils.showAlert(R.string.errorGenericTitle, R.string.errorEmailPassEmpty, this)
            }
        }

        // Botón Registrarse
        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Botón Invitado
        binding.guestButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Botón Recuperar Contraseña
        binding.passwordRecoverButton.setOnClickListener {
            startActivity(Intent(this, RecoveryActivity::class.java))
        }
    }
}