package cat.copernic.prodis.reservesprodis

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cat.copernic.prodis.reservesprodis.adapters.SalaRecyclerAdapter
import cat.copernic.prodis.reservesprodis.databinding.DialogAddItemBinding
import cat.copernic.prodis.reservesprodis.databinding.FrgRecyclerviewBinding
import cat.copernic.prodis.reservesprodis.models.Sala
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class Salas : Fragment() {
    // Binding
    private lateinit var binding: FrgRecyclerviewBinding

    // Binding dialog
    private lateinit var dialogBinding: DialogAddItemBinding

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

    // Storage Firebase
    private val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()

    // Adapter
    private val myAdapter: SalaRecyclerAdapter = SalaRecyclerAdapter()

    // Animaciones para el FAB
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            activity,
            R.anim.rotate_open_anim
        )
    }

    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            activity,
            R.anim.rotate_close_anim
        )
    }

    private var clicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate<FrgRecyclerviewBinding>(
            inflater,
            R.layout.frg_recyclerview, container, false
        )
        dialogBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_add_item, container, false)
        // Si el usuario existe pero nunca ha rellenado su perfil
        if (auth.currentUser != null) {
            db.collection("users").document(utils.getEmail(auth)).get()
                .addOnSuccessListener { result ->
                    if (result.get("nombre") == "") {
                        findNavController().navigate(SalasDirections.actionSalasToPerfil())
                    }
                    // Comprobar que el usuario no está deshabilitado
                    if (!(result.get("enabled") as Boolean)) {
                        utils.notificationUserDisabled(requireContext(), resources)
                        auth.signOut()
                        requireActivity().finish()
                    }
                    // Mostrar el FAB si el usuario es admin
                    if (result.get("admin") as Boolean) {
                        binding.fab.visibility = View.VISIBLE
                    } else {
                        binding.fab.visibility = View.GONE
                    }
                }
        }

        setupRecyclerView()

        binding.fab.setOnClickListener {
            showdialog()
            onFabClicked()
        }

        return binding.root
    }

    private fun onFabClicked() {
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (clicked) binding.fab.startAnimation(rotateClose) else binding.fab.startAnimation(
            rotateOpen)
    }

    // Función que actualiza el RecyclerView
    private fun setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        myAdapter.salasRecyclerAdapter(getSalas(), requireActivity())
        binding.recyclerView.adapter = myAdapter
    }

    // RecyclerView para las salas
    @SuppressLint("NotifyDataSetChanged")
    private fun getSalas(): MutableList<Sala> {
        val salas: MutableList<Sala> = arrayListOf()
        db.collection("salas").get().addOnSuccessListener { result ->
            for (document in result) {
                // Traducir el array para poderlo gestionar
                salas.add(
                    Sala(
                        document.get("originalName").toString(),
                        document.get("name").toString(),
                        document.get("capacity").toString(),
                        document.get("image").toString(),
                        document.get("desc").toString(),
                        document.get("enabled") as Boolean,
                        utils.translateArr(document.get("icons").toString())
                    )
                )
            }
            myAdapter.notifyDataSetChanged()
        }
        return salas
    }

    fun addNewRoom(newSalaName: String) {
        val iconList: List<Boolean> = listOf(false, false, false, false, false, false, false)
        db.collection("salas").document(newSalaName)
            .set(
                hashMapOf(
                    "originalName" to newSalaName,
                    "name" to newSalaName,
                    "capacity" to "1",
                    "desc" to "Desc",
                    "image" to "",
                    "icons" to iconList,
                    "enabled" to true
                )
            )
    }

    fun showdialog() {
        // Eliminar el parent si ya se ha inflado el menú
        if (dialogBinding.root.parent != null) (dialogBinding.root.parent as ViewGroup).removeView(
            dialogBinding.root)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.popupNewItemTitle)
        builder.setCancelable(false)

        // Input nombre sala/vehiculo
        val editText = dialogBinding.inputEditText
        // Input imagen sala/vehiculo
        dialogBinding.imageView.setImageResource(R.drawable.ic_door)
        dialogBinding.imageView.imageTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.dt_grey)

        builder.setView(dialogBinding.root)

        // Botón Imagen
        dialogBinding.imageView.setOnClickListener {
            val image = Intent(Intent.ACTION_PICK)
            image.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(image, 1)
        }

        // Set up the buttons
        builder.setPositiveButton(
            R.string.accept,
            DialogInterface.OnClickListener { dialog, which ->
                // Here you get get input text from the Edittext
                val newSalaName = editText.text.toString()
                if (newSalaName != "" && dialogBinding.imageView.drawable !is VectorDrawable) {
                    addNewRoom(newSalaName)
                    uploadImage(editText.text.toString(), dialogBinding.imageView)
                    // Workaround cutrísimo para actualizar la lista de salas
                    findNavController().navigate(SalasDirections.actionSalasSelf())
                } else {
                    utils.showAlert(
                        R.string.errorGenericTitle,
                        R.string.popupErrorNewItemTitle,
                        requireContext()
                    )
                }
                clicked = false
                binding.fab.startAnimation(rotateClose)
            })
        builder.setNegativeButton(
            R.string.cancel,
            DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
                clicked = false
                binding.fab.startAnimation(rotateClose)
            })
        builder.show()
    }

    // Guardar imagen
    fun uploadImage(name: String, imageView: ImageView) {
        // Nombre temporal
        val tempName = name + "_image"
        // Ruta
        val pathReference = storageRef.child("salas/$tempName")
        // Coger la imagen del ImageView
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
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
                db.collection("salas").document(name)
                    .update(
                        mapOf("image" to url.toString())
                    )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            dialogBinding.imageView.imageTintList = null
            dialogBinding.imageView.setImageURI(data?.data)
        }
    }
}