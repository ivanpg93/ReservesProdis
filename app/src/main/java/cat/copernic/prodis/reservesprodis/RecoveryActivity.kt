package cat.copernic.prodis.reservesprodis

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import cat.copernic.prodis.reservesprodis.ViewModel.LoginActivityVM
import cat.copernic.prodis.reservesprodis.databinding.ActivityRecoveryBinding
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecoveryActivity : AppCompatActivity() {
    // Binding
    private lateinit var binding: ActivityRecoveryBinding

    // Base de datos
    private val db = Firebase.firestore

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Utils
    private val utils = Utils()

    // ViewModel
    private lateinit var viewModel: LoginActivityVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery)

        binding = DataBindingUtil.setContentView<ActivityRecoveryBinding>(
            this,
            R.layout.activity_recovery
        )
        viewModel = ViewModelProvider(this).get(LoginActivityVM::class.java)

        // Settear el botón de atras en la ActionBar
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.acceptButton.setOnClickListener {

            viewModel.setDni(binding.dniTextInput.text.toString().uppercase())
            viewModel.setEmail(binding.emailTextInput.text.toString().lowercase())

            if (binding.dniTextInput.text!!.isNotEmpty() && binding.emailTextInput.text!!.isNotEmpty()) {
                if (!utils.checkDni(viewModel.getDni())) {
                    // Comprobar que el DNI es váido
                    utils.showAlert(R.string.errorGenericTitle, R.string.errorDniInvalid, this)
                } else {
                    db.collection("users").document(viewModel.getEmail()).get()
                        .addOnSuccessListener { document ->
                            if (document.get("dni") == viewModel.getDni()) {
                                // Enviar un email al correo del DNI
                                sendEmail()
                                println("he entrado")
                                println(document.get("dni"))
                                println("compare with " + viewModel.getDni())
                            }
                            utils.showAlert(
                                R.string.errorRecoveryEmailSentTitle,
                                R.string.errorRecoveryEmailSent,
                                this
                            )
                        }
                }
            } else {
                // Hay algún campo vacio
                utils.showAlert(R.string.errorGenericTitle, R.string.errorEmailPassEmpty, this)
            }
        }
    }

    private fun sendEmail() {
        auth.useAppLanguage()
        auth.sendPasswordResetEmail(viewModel.getEmail())
        println("email funcion" + viewModel.getEmail())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}