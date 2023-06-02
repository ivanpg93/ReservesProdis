package cat.copernic.prodis.reservesprodis.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.reservesprodis.R
import cat.copernic.prodis.reservesprodis.SalasDirections
import cat.copernic.prodis.reservesprodis.databinding.ItemSalasListBinding
import cat.copernic.prodis.reservesprodis.models.Sala
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList

class SalaRecyclerAdapter() : RecyclerView.Adapter<SalaRecyclerAdapter.ViewHolder>() {
    var salas: MutableList<Sala> = ArrayList()

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

    fun salasRecyclerAdapter(salasList: MutableList<Sala>, contxt: Context) {
        this.salas = salasList
        this.context = contxt
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return ViewHolder(
            ItemSalasListBinding.inflate(
                layoutInflater, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Animaciones para el botón de abrir cerrar
        var clicked = false

        with(holder) {
            with(salas.get(position)) {
                binding.titleSalas.setText(this.salaTitle)     // Titulo
                binding.titleSalasNum.setText(this.capacity)    // Capacidad
                binding.descTextView.setText(this.desc)         // Descripción
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
                                    salas.get(position),
                                    disabledColor,
                                    enabledColor
                                )
                            }
                        }
                    // Si has iniciado como invitado, oculta el botón de Reservar
                } else {
                    holder.binding.reservarButton.visibility = View.GONE
                }

                // Settear el color en deshabilitado para cada icono
                val iconListColor: List<ImageView> = listOf(
                    holder.binding.iconMouse,
                    holder.binding.iconWifi,
                    holder.binding.iconCoffe,
                    holder.binding.iconBook,
                    holder.binding.iconWc,
                    holder.binding.iconTools,
                    holder.binding.iconProjector
                )
                iconListColor.forEachIndexed { index, element ->
                    if (!this.icons[index]) element.backgroundTintList = disabledColor
                }

                // Cambiar la imagen de la sala/vehiculo
                if (this.image != "") {
                    Glide.with(context)
                        .load(this.image)
                        .centerCrop()
                        .into(binding.imagenSala)
                }

                // Botón guardar cambios
                holder.binding.saveButton.setOnClickListener() {
                    this.iconsList?.let { it1 ->
                        saveItemData(
                            holder, salas.get(position).originalName, it1
                        )
                    }
                }

                // Comprobar si la sala está reservada en este momento
                val cal: Calendar = Calendar.getInstance()
                val dayOfMonth: Int = cal.get(Calendar.DAY_OF_MONTH)

                val currDay = dayOfMonth.toString()
                val currHour: Int = cal.get(Calendar.HOUR_OF_DAY)

                db.collection("reservas").document("salas")
                    .collection(salas.get(position).originalName).get()
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
        val item = salas.get(position)
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
                        storageRef.child("salas/" + salas.get(position).originalName + "_image")
                    imageStorage.delete()
                    // Borrar datos de la DDBB
                    db.collection("salas").document(salas.get(position).originalName)
                        .delete()
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                }
                .setTitle(R.string.reservaAlertTitle) // El título
                .setMessage(R.string.reservaAlertDesc)
                .create()
                .show()
            update()
        }

        holder.binding.reservarButton.setOnClickListener { view ->
            view.findNavController().navigate(
                SalasDirections.actionSalasToReservaSala
                    (salas.get(position).originalName, salas.get(position).salaTitle!!)
            )
        }
    }

    private fun clickableIcons(
        holder: ViewHolder,
        get: Sala,
        disabledColor: ColorStateList?,
        enabledColor: ColorStateList?,
    ) {
        val iconList: List<ImageView> = listOf(
            holder.binding.iconMouse,
            holder.binding.iconWifi,
            holder.binding.iconCoffe,
            holder.binding.iconBook,
            holder.binding.iconWc,
            holder.binding.iconTools,
            holder.binding.iconProjector
        )
        // Iconos clicables solo para los admins
        iconList.forEachIndexed { index, element ->
            element.setOnClickListener {
                if (get.icons[index]) {
                    get.icons[index] = false
                    it.backgroundTintList = disabledColor
                } else {
                    get.icons[index] = true
                    it.backgroundTintList = enabledColor
                }
            }
        }
    }

    class ViewHolder(val binding: ItemSalasListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sala: Sala) {
        }
    }

    override fun getItemCount(): Int {
        return salas.size
    }

    private fun saveItemData(
        holder: ViewHolder,
        nombre: String,
        iconList: Array<Boolean>,
    ) {
        if (holder.binding.titleSalas.text!!.isNotEmpty() && holder.binding.titleSalasNum.text!!.isNotEmpty() && holder.binding.descTextView.text!!.isNotEmpty()) {
            println(holder.binding.titleSalas.text!!.isNotEmpty())
            println(holder.binding.titleSalasNum.text!!.isNotEmpty())
            println(holder.binding.descTextView.text!!.isNotEmpty())
            db.collection("salas").document(nombre).update(
                mapOf(
                    "name" to holder.binding.titleSalas.text.toString().trim(),
                    "capacity" to holder.binding.titleSalasNum.text.toString().trim(),
                    "desc" to holder.binding.descTextView.text.toString().trim(),
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
        holder.binding.titleSalas.isEnabled = switch
        holder.binding.titleSalasNum.isEnabled = switch
        holder.binding.descTextView.isEnabled = switch
    }

    private fun disableItem(holder: ViewHolder) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f) // Desaturar la imagen

        holder.binding.reservarButton.isEnabled = false
        holder.binding.reservarButton.text = context.resources.getString(R.string.reserveDisabled)
        holder.binding.reservarButton.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.grayDarkerAlt)
        holder.binding.mainCardView.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.grayDarker)
        holder.binding.imagenSala.colorFilter = ColorMatrixColorFilter(matrix)
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