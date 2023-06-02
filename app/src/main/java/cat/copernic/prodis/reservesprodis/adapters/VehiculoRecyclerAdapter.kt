package cat.copernic.prodis.reservesprodis.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import cat.copernic.prodis.reservesprodis.R
import cat.copernic.prodis.reservesprodis.VehiculosDirections
import cat.copernic.prodis.reservesprodis.databinding.ItemVehiculosListBinding
import cat.copernic.prodis.reservesprodis.models.Vehiculo
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.graphics.ColorMatrixColorFilter
import android.graphics.ColorMatrix
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import java.util.*
import kotlin.collections.ArrayList

class VehiculoRecyclerAdapter : RecyclerView.Adapter<VehiculoRecyclerAdapter.ViewHolder>() {
    var vehiculos: MutableList<Vehiculo> = ArrayList()

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos Firebase
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

    // Storage Firebase
    private val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()

    lateinit var context: Context

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.rotate_open_anim_full
        )
    }

    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            context,
            R.anim.rotate_close_anim_full
        )
    }

    private fun onButtonClicked(holder: ViewHolder, clicked: Boolean) {
        setAnimation(clicked, holder)
    }

    private fun setAnimation(clicked: Boolean, holder: ViewHolder) {
        if (clicked) holder.binding.collapsableIcon.startAnimation(rotateOpen) else holder.binding.collapsableIcon.startAnimation(
            rotateClose)
    }

    fun vehiculosReclyclerAdapter(vehiculosList: MutableList<Vehiculo>, contxt: Context) {
        this.vehiculos = vehiculosList
        this.context = contxt
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemVehiculosListBinding.inflate(
                layoutInflater, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Animaciones para el botón de abrir cerrar
        var clicked = false

        with(holder) {
            with(vehiculos.get(position)) {
                binding.titleVehiculos.setText(this.vehiculoTitle)  // Titulo
                binding.plazasTextView.setText(this.capacity)       // Capacidad
                binding.descTextView.setText(this.desc)             // Descripción
                binding.titleMatricula.setText(this.matricula)      // Matricula
                binding.enabledCheckbox.isChecked = (this.enabled)  // Habilitado

                // Cambiar los colores de cada icono
                val disabledColor =
                    ContextCompat.getColorStateList(context, R.color.iconTransparency)

                // Usar blanco o gris según esté activado el modo noche
                var enabledColor = ContextCompat.getColorStateList(context, R.color.white)
                when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        enabledColor = ContextCompat.getColorStateList(context, R.color.dt_white)
                    }
                }

                // Habilitar la edicion de inputs para los administradores
                switchEditable(holder, false)
                if (auth.currentUser != null) {
                    holder.binding.reservarButton.isEnabled = true
                    db.collection("users").document(utils.getEmail(auth)).get()
                        .addOnSuccessListener { result ->
                            if (result.get("admin") as Boolean) {
                                switchEditable(holder, true)

                                clickableIcons(
                                    holder,
                                    vehiculos.get(position),
                                    disabledColor,
                                    enabledColor
                                )
                            }
                        }
                    // Si has iniciado como invitado, oculta el botón de Reservar
                } else {
                    holder.binding.reservarButton.visibility = View.GONE
                }

                if (!this.icons[0])
                    binding.iconWheelchair.backgroundTintList = disabledColor

                // Cambiar la imagen de la sala/vehiculo
                if (this.image != "") {
                    Glide.with(context)
                        .load(this.image)
                        .centerCrop()
                        .into(binding.imagenVehiculo)
                }

                // Botón guardar cambios
                holder.binding.saveButton.setOnClickListener() {
                    this.iconsList?.let { it1 ->
                        saveItemData(
                            holder, vehiculos.get(position).originalName,
                            it1
                        )
                    }
                }

                // Comprobar si la sala está reservada en este momento
                val cal: Calendar = Calendar.getInstance()
                val dayOfMonth: Int = cal.get(Calendar.DAY_OF_MONTH)

                val currDay = dayOfMonth.toString()
                val currHour: Int = cal.get(Calendar.HOUR_OF_DAY)

                db.collection("reservas").document("vehiculos")
                    .collection(vehiculos.get(position).originalName).get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val day = document.get("fecha").toString().substring(0, 2).toInt()
                            val arrBool = utils.translateArr(document.get("horarios").toString())

                            if (day == currDay.toInt() && arrBool[0] && currHour == 9)
                                recolorCardviewRed(holder)

                            else if (day == currDay.toInt() && arrBool[1] && currHour == 10)
                                recolorCardviewRed(holder)

                            else if (day == currDay.toInt() && arrBool[2] && currHour == 11)
                                recolorCardviewRed(holder)

                            else if (day == currDay.toInt() && arrBool[3] && currHour == 12)
                                recolorCardviewRed(holder)

                            else if (day == currDay.toInt() && arrBool[4] && currHour == 14)
                                recolorCardviewRed(holder)

                            else if (day == currDay.toInt() && arrBool[5] && currHour == 15)
                                recolorCardviewRed(holder)

                            else if (day == currDay.toInt() && arrBool[6] && currHour == 16)
                                recolorCardviewRed(holder)
                        }
                    }

                // Comprobar si el item está habilitado
                if (!this.enabled)
                    disableItem(holder)
            }
        }
        val item = vehiculos.get(position)
        holder.bind(item)

        // Función que muestra/oculta el div de la sala y cambia el icono de la flecha según convenga
        fun onCollapsableClicked() {
            if (!clicked) {
                holder.binding.div.visibility = View.VISIBLE
                holder.binding.collapsableIcon.setBackgroundResource(R.drawable.ic_arrow)
            } else {
                holder.binding.div.visibility = View.GONE
                holder.binding.collapsableIcon.setBackgroundResource(R.drawable.bg_spinner)
            }
            onButtonClicked(holder, clicked)
        }

        holder.binding.collapsableIcon.setOnClickListener {
            onCollapsableClicked()
            clicked = !clicked
        }

        // Eliminar
        holder.binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setPositiveButton(R.string.accept) { dialog, which ->
                    val imageStorage =
                        storageRef.child("vehiculos/" + vehiculos.get(position).originalName + "_image")
                    imageStorage.delete()
                    // Borrar datos de la DDBB
                    db.collection("vehiculos").document(vehiculos.get(position).originalName)
                        .delete()
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->

                }
                .setTitle(R.string.reservaAlertTitle) // El título
                .setMessage(R.string.reservaAlertDesc)
                .create()
                .show()
        }

        holder.binding.reservarButton.setOnClickListener { view ->
            view.findNavController().navigate(VehiculosDirections.actionVehiculosToReservaVehiculo
                (vehiculos.get(position).originalName, vehiculos.get(position).vehiculoTitle!!)
            )
        }
    }

    private fun clickableIcons(
        holder: ViewHolder,
        get: Vehiculo,
        disabledColor: ColorStateList?,
        enabledColor: ColorStateList?,
    ) {
        // Iconos clicables solo para los admins
        holder.binding.iconWheelchair.setOnClickListener {
            if (get.icons[0]) {
                get.icons!![0] = false
                holder.binding.iconWheelchair.backgroundTintList = disabledColor
            } else {
                get.icons!![0] = true
                holder.binding.iconWheelchair.backgroundTintList = enabledColor
            }
        }
    }

    class ViewHolder(val binding: ItemVehiculosListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(vehiculo: Vehiculo) {
        }
    }

    override fun getItemCount(): Int {
        return vehiculos.size
    }

    private fun saveItemData(
        holder: ViewHolder,
        nombre: String,
        iconList: Array<Boolean>,
    ) {
        if (holder.binding.titleVehiculos.text!!.isNotEmpty() && holder.binding.titleMatricula.text!!.isNotEmpty() && holder.binding.descTextView.text!!.isNotEmpty() && holder.binding.plazasTextView.text!!.isNotEmpty()) {
            db.collection("vehiculos").document(nombre).update(
                mapOf(
                    "name" to holder.binding.titleVehiculos.text.toString().trim(),
                    "matricula" to holder.binding.titleMatricula.text.toString().trim(),
                    "desc" to holder.binding.descTextView.text.toString().trim(),
                    "capacity" to holder.binding.plazasTextView.text.toString().trim(),
                    "enabled" to holder.binding.enabledCheckbox.isChecked,
                    "icons" to iconList.asList()
                )
            )
            utils.showAlert(
                R.string.profileSaveCorrectTitle,
                R.string.profileSaveCorrect,
                context
            )
        } else {
            // Hay algún campo vacio
            utils.showAlert(R.string.errorGenericTitle, R.string.errorEmailPassEmpty, context)
        }
    }

    private fun switchEditable(holder: ViewHolder, switch: Boolean) {
        holder.binding.linearLayoutButtons.visibility = if (switch) View.VISIBLE else View.GONE
        holder.binding.separadorAdmin.visibility = if (switch) View.VISIBLE else View.GONE
        holder.binding.titleVehiculos.isEnabled = switch
        holder.binding.plazasTextView.isEnabled = switch
        holder.binding.descTextView.isEnabled = switch
        holder.binding.titleMatricula.isEnabled = switch
    }

    private fun disableItem(holder: ViewHolder) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f) // Desaturar la imagen
        holder.binding.reservarButton.text = context.resources.getString(R.string.reserveDisabled)
        holder.binding.reservarButton.setTextColor(ContextCompat.getColor(context, R.color.white))
        holder.binding.reservarButton.isEnabled = false
        holder.binding.reservarButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.grayDarkerAlt)
        holder.binding.mainCardView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.grayDarker)
        holder.binding.imagenVehiculo.colorFilter = ColorMatrixColorFilter(matrix)
    }

    private fun recolorCardviewRed(holder: ViewHolder) {
        holder.binding.mainCardView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.btn_rojo)
        holder.binding.reservarButton.text = context.resources.getString(R.string.reserveForAnotherMoment)
        holder.binding.reservarButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.btn_rojo_alt)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        this.notifyDataSetChanged()
    }
}