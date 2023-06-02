package cat.copernic.prodis.reservesprodis

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import cat.copernic.prodis.reservesprodis.databinding.FrgPerfilBinding
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import cat.copernic.prodis.reservesprodis.ViewModel.PerfilVM
import cat.copernic.prodis.reservesprodis.databinding.DialogChangePasswdBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

class Perfil : Fragment(), AdapterView.OnItemSelectedListener {
    // Binding
    private lateinit var binding: FrgPerfilBinding

    // Binding dialog
    private lateinit var dialogBinding: DialogChangePasswdBinding

    // Autentificacion Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos Firebase
    private val db = Firebase.firestore

    // Storage Firebase
    val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()

    // ViewModel
    private lateinit var viewModel: PerfilVM

    // Utils
    private val utils = Utils()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Binding
        binding = DataBindingUtil.inflate(inflater, R.layout.frg_perfil, container, false)
        // Binding del dialog
        dialogBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_change_passwd, container, false)
        // ViewModel
        viewModel = ViewModelProvider(this).get(PerfilVM::class.java)

        // Deshabilitar la edicion del input del email
        binding.emailTextInput.isEnabled = false

        // Adapter para el Spinner (Añadir items directamente)
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.servicios,
            R.layout.spinner_item_custom
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom)
            binding.spinner.adapter = adapter
        }

        // Llena formularios con datos del usuario de la DDBB
        if (auth.currentUser != null) {
            // Corrutina
            runBlocking {
                launch {
                    delay(100L)
                    fillUserData()
                }
            }
        }

        // Spinner servicios
        binding.spinner.onItemSelectedListener = this

        val loadImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
        ) {
            binding.userImage.setImageURI(it)
        }

        // Botón Avatar
        binding.userImage.setOnClickListener {
            loadImage.launch("image/*")
        }

        // Botón cambiar contraseña
        binding.passwordButton.setOnClickListener {
            showdialog()
        }

        // Botón Guardar datos
        binding.guardarButton.setOnClickListener { view: View ->
            runBlocking {
                launch {
                    delay(100L)
                    saveUserData()
                }
            }
        }

        // Botón Cerrar sessión
        binding.logOutButton.setOnClickListener { view: View ->
            auth.signOut()
            requireActivity().finish()
            true
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            true
        }
        return binding.root
    }

    override fun onItemSelected(p0: AdapterView<*>, p1: View?, position: Int, p3: Long) {
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    // Guardar imagen
    fun uploadImage() {
        // Nombre temporal
        val tempName = utils.getEmail(auth) + "_image"
        // Ruta
        val pathReference = storageRef.child("avatars/$tempName")
        // Coger la imagen del ImageView
        val bitmap = (binding.userImage.drawable as BitmapDrawable).bitmap
        // Inicialitzem OutputStream
        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        pathReference.putBytes(data).addOnFailureListener {
            utils.showAlert(
                R.string.errorGenericTitle,
                R.string.profileImageError,
                requireContext()
            )
        }.addOnCompleteListener {
            // Si es correcto, subir la URL a la DDBB
            pathReference.downloadUrl.addOnSuccessListener { url ->
                viewModel.setUserAvatar(url.toString())
            }
        }
    }

    private fun checkInputs(): Boolean {
        return binding.emailTextInput.text!!.isNotEmpty() && binding.fullNameTextInput.text!!.isNotEmpty() && binding.tlfTextInput.text!!.isNotEmpty()
    }

    private fun fillUserData() {
        val avatar = binding.userImage
        viewModel.currUser.observe(viewLifecycleOwner, {
            // Comprobar que el usuario no está deshabilitado
            if (!it.getUserEnabled()) {
                utils.notificationUserDisabled(requireContext(), resources)
                auth.signOut()
                requireActivity().finish()
            } else {
                binding.fullNameTextInput.setText(it.getUserName())
                binding.emailTextInput.setText(it.getUserEmail())
                binding.titleDni.text = it.getUserDni()
                binding.tlfTextInput.setText(it.getUserTlf())
                binding.spinner.setSelection(
                    Integer.valueOf(
                        it.getUserServicio()
                    )
                )
                if (it.getUserAvatar() != "") {
                    Glide.with(requireContext())
                        .load(it.getUserAvatar())
                        .centerCrop()
                        .into(avatar)
                }
            }

            // Si el nombre y tlf está "vacío" desactiva la opción de cerrar sesión
            binding.logOutButton.isVisible = it.getUserName() != ""
            if (!checkInputs()) {
                utils.showAlert(
                    R.string.warningGenericTitle,
                    R.string.errorFillProfile,
                    requireContext()
                )
            }
        })
    }

    private fun saveUserData() {
        // viewModel.setUserName("nuevo nombre")
        if (checkInputs()) {
            if (auth.currentUser != null) {
                // Si el avatar no es un vector (imagen por defecto)
                if (binding.userImage.drawable !is VectorDrawable) {
                    uploadImage()
                }
                if (utils.checkLength(binding.tlfTextInput.text.toString(), 9)) {
                    // Comprobar que el teléfono es de 9 dígitos
                    viewModel.setUserName(binding.fullNameTextInput.text.toString().trim())
                    viewModel.setUserTlf(binding.tlfTextInput.text!!.toString().trim())
                    viewModel.setUserServicio(binding.spinner.selectedItemPosition)

                    // T0do correcto
                    utils.showAlert(
                        R.string.profileSaveCorrectTitle,
                        R.string.profileSaveCorrect,
                        requireContext()
                    )
                } else {
                    utils.showAlert(
                        R.string.errorGenericTitle,
                        R.string.errorPhoneNotCorrect,
                        requireContext()
                    )
                }
            }
        } else {
            // Hay algún campo vacio
            utils.showAlert(
                R.string.errorGenericTitle,
                R.string.errorEmailPassEmpty,
                requireContext()
            )
        }
    }

    fun showdialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.changePassword)

        val newPasswd = dialogBinding.newPasswdEditText
        val repeatNewPasswd = dialogBinding.repeatNewPasswdEditText

        builder.setView(dialogBinding.root)

        // Set up the buttons
        builder.setPositiveButton(
            R.string.accept,
            DialogInterface.OnClickListener { dialog, which ->
                // Comprobar que la contraseña es de 4 dígitos
                if (utils.checkLength(newPasswd.text.toString(), 4)) {
                    // Comprobar que la ha escrito correctamente
                    if (newPasswd.text.toString() == repeatNewPasswd.text.toString()) {
                        val user = FirebaseAuth.getInstance().currentUser
                        // Actualizar la contraseña con la nueva + el string prodis
                        user!!.updatePassword(newPasswd.text.toString() + "prodis")
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    utils.showAlert(
                                        R.string.profileSaveCorrectTitle,
                                        R.string.profileSaveCorrect,
                                        requireContext()
                                    )
                                } else {
                                    utils.showAlert(
                                        R.string.errorGenericTitle,
                                        R.string.dialog_genericErrorPasswd,
                                        requireContext()
                                    )
                                }
                            }
                    } else {
                        utils.showAlert(
                            R.string.errorGenericTitle,
                            R.string.dialog_errorPasswdDoesNotMatch,
                            requireContext()
                        )
                    }
                } else {
                    utils.showAlert(
                        R.string.errorGenericTitle,
                        R.string.errorPasswdInvalid,
                        requireContext()
                    )
                }
            })
        builder.setNegativeButton(
            R.string.cancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }
}
