package cat.copernic.prodis.reservesprodis.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.reservesprodis.R
import cat.copernic.prodis.reservesprodis.databinding.ItemReservasListBinding
import cat.copernic.prodis.reservesprodis.models.Reserva
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReservaRecyclerAdapter : RecyclerView.Adapter<ReservaRecyclerAdapter.ViewHolder>() {
    var reservas: MutableList<Reserva> = ArrayList()
    lateinit var context: Context

    // Auth de Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

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

    fun ReservasRecyclerAdapter(reservasList: MutableList<Reserva>, contxt: Context) {
        this.reservas = reservasList
        this.context = contxt
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemReservasListBinding.inflate(
                layoutInflater, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Animaciones para el botón de abrir cerrar
        var clicked = false

        with(holder) {
            with(reservas.get(position)) {


                // Poner el CardView azul si la reserva es un vehículo
                if (this.tipo == "vehiculo") {
                    binding.cardView.backgroundTintList = ContextCompat.getColorStateList(context, R.color.azul)
                }

                // Nombre de la sala/vehiculo
                if (this.reservaNombre!!.length > 17) {
                    val newName = this.reservaNombre!!.substring(0, 17) + "..."
                    binding.titleReserva.text = newName
                } else {
                    binding.titleReserva.text = this.reservaNombre
                }
                binding.titleFecha.text = this.fecha              // Fecha de la reserva
                binding.horaInicio.text = this.horaInicial        // Hora inicial de la reserva
                binding.horaFinal.text = this.horaFinal           // Hora final de la reserva
            }
        }
        val item = reservas.get(position)
        holder.bind(item)

        // Función que muestra/oculta el div de la sala y cambia el icono de la flecha según convenga
        fun onCollapsableClicked(clicked: Boolean) {
            if (!clicked) {
                holder.binding.div.visibility = View.VISIBLE
                holder.binding.collapsableIcon.setBackgroundResource(R.drawable.ic_arrow)
            } else {
                holder.binding.div.visibility = View.GONE
                holder.binding.collapsableIcon.setBackgroundResource(R.drawable.bg_spinner)

            }
            onButtonClicked(holder, clicked)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun cancelarReserva(index: Int) {
            db.collection("users").document(utils.getEmail(auth)).collection("misreservas")
                .document(reservas.get(position).id).delete()
            if (reservas.get(position).tipo == "sala") {
                db.collection("reservas").document("salas")
                    .collection(reservas.get(position).nombre).document(reservas.get(position).id)
                    .delete()
            } else {
                db.collection("reservas").document("vehiculos")
                    .collection(reservas.get(position).nombre).document(reservas.get(position).id)
                    .delete()
            }
            reservas.removeAt(index)
            notifyDataSetChanged()
        }

        // Alerta de confirmación antes de habilitar un usuario
        fun alertaCancelarReserva() {
            val mensaje =
                context.resources.getString(R.string.dialog_cancelReserve) + reservas.get(position).nombre + " " + reservas.get(
                    position).fecha + "?"
            AlertDialog.Builder(context) // NombreDeTuActividad.this, o getActivity() si es dentro de un fragmento
                .setPositiveButton(R.string.accept) { dialog, which ->
                    // Click en el botón positivo, así que la acción está confirmada
                    cancelarReserva(position)
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                    // Click en el botón negativo, así que la acción está cancelada
                    dialog.dismiss()

                }
                .setTitle(R.string.alertTitle) // El título
                .setMessage(mensaje) // El mensaje
                .create() // No olvides llamar a Create, ¡pues eso crea el AlertDialog!
                .show() // Mostramos el dialogo
        }

        holder.binding.collapsableIcon.setOnClickListener {
            onCollapsableClicked(clicked)
            clicked = !clicked
        }

        holder.binding.reservaCancelButton.setOnClickListener {
            alertaCancelarReserva()
        }

        // Estamblim un listener
        holder.itemView.setOnClickListener {
            onCollapsableClicked(clicked)
            clicked = !clicked
        }
    }

    class ViewHolder(val binding: ItemReservasListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reserva: Reserva) {
        }
    }

    override fun getItemCount(): Int {
        return reservas.size
    }
}