package cat.copernic.prodis.reservesprodis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.prodis.reservesprodis.databinding.ItemUsersListBinding
import cat.copernic.prodis.reservesprodis.models.Usuario
import android.app.AlertDialog;
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import cat.copernic.prodis.reservesprodis.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UsuarioRecyclerAdapter : RecyclerView.Adapter<UsuarioRecyclerAdapter.ViewHolder>() {
    var usuarios: MutableList<Usuario> = ArrayList()
    lateinit var context: Context

    // Base de datos
    private val db = Firebase.firestore

    //constructor de la classe on es passa la font de dades i el context sobre el que es mostrarà
    fun UsuariosReclyclerAdapter(usersList: MutableList<Usuario>, contxt: Context) {
        this.usuarios = usersList
        this.context = contxt
    }

    //és l'encarregat de retornar el ViewHolder ja configurat
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemUsersListBinding.inflate(
                layoutInflater, parent, false
            )
        )
    }

    //Aquest mètode s'encarrega de passar els objectes, un a un al ViewHolder personalitzat
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(usuarios.get(position)) {
                binding.emailUser.text = this.userEmail
                binding.checkBoxAdmin.isChecked = usuarios.get(position).admin
                binding.checkboxEnabled.isChecked = usuarios.get(position).enabled
            }
        }

        fun adminUser(index: Int) {
            db.collection("users").document(usuarios.get(index).email).update(
                mapOf("admin" to true)
            )
            holder.binding.checkBoxAdmin.isChecked
        }

        fun currentUser(index: Int) {
            db.collection("users").document(usuarios.get(index).email).update(
                mapOf("admin" to false)
            )
            !holder.binding.checkBoxAdmin.isChecked
        }

        // Alerta de confirmación antes de habilitar un usuario
        fun alertaAdminUser() {
            val mensaje = context.resources.getString(R.string.alertAdmin) + usuarios.get(position).email + "?"
            AlertDialog.Builder(context) // NombreDeTuActividad.this, o getActivity() si es dentro de un fragmento
                .setCancelable(false)
                .setPositiveButton(R.string.accept) { dialog, which ->
                    // Click en el botón positivo, así que la acción está confirmada
                    adminUser(position)
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                    // Click en el botón negativo, así que la acción está cancelada
                    // Mantenemos el estado anterior del checkbox
                    holder.binding.checkBoxAdmin.isChecked = usuarios.get(position).enabled

                }
                .setTitle(R.string.alertTitle) // El título
                .setMessage(mensaje) // El mensaje
                .create() // No olvides llamar a Create, ¡pues eso crea el AlertDialog!
                .show() // Mostramos el dialogo
        }

        // Alerta de confirmación antes de deshabilitar un usuario
        fun alertaNoAdminUser() {
            val mensaje = context.resources.getString(R.string.alertNoAdmin) + usuarios.get(position).email + "?"
            AlertDialog.Builder(context) // NombreDeTuActividad.this, o getActivity() si es dentro de un fragmento
                .setCancelable(false)
                .setPositiveButton(R.string.accept) { dialog, which ->
                    // Click en el botón positivo, así que la acción está confirmada
                    currentUser(position)
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                    // Click en el botón negativo, así que la acción está cancelada
                    // Mantenemos el estado anterior del checkbox
                    holder.binding.checkBoxAdmin.isChecked = usuarios.get(position).enabled

                }
                .setTitle(R.string.alertTitle) // El título
                .setMessage(mensaje) // El mensaje
                .create() // No olvides llamar a Create, ¡pues eso crea el AlertDialog!
                .show() // Mostramos el dialogo
        }

        fun enabledUser(index: Int) {
            db.collection("users").document(usuarios.get(index).email).update(
                mapOf("enabled" to true)
            )
            holder.binding.checkboxEnabled.isChecked
        }

        fun disabledUser(index: Int) {
            db.collection("users").document(usuarios.get(index).email).update(
                mapOf("enabled" to false)
            )
            !holder.binding.checkboxEnabled.isChecked
        }

        // Alerta de confirmación antes de habilitar un usuario
        fun alertaEnabledUser() {
            val mensaje = context.resources.getString(R.string.alertHabilitar) + usuarios.get(position).email + "?"
            AlertDialog.Builder(context) // NombreDeTuActividad.this, o getActivity() si es dentro de un fragmento
                .setCancelable(false)
                .setPositiveButton(R.string.accept) { dialog, which ->
                    // Click en el botón positivo, así que la acción está confirmada
                    enabledUser(position)
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                    // Click en el botón negativo, así que la acción está cancelada
                    // Mantenemos el estado anterior del checkbox
                    holder.binding.checkboxEnabled.isChecked = usuarios.get(position).enabled

                }
                .setTitle(R.string.alertTitle) // El título
                .setMessage(mensaje) // El mensaje
                .create() // No olvides llamar a Create, ¡pues eso crea el AlertDialog!
                .show() // Mostramos el dialogo
        }

        // Alerta de confirmación antes de deshabilitar un usuario
        fun alertaDisabledUser() {
            val mensaje = context.resources.getString(R.string.alertDeshabilitar) + usuarios.get(position).email + "?"
            AlertDialog.Builder(context) // NombreDeTuActividad.this, o getActivity() si es dentro de un fragmento
                .setCancelable(false)
                .setPositiveButton(R.string.accept) { dialog, which ->
                    // Click en el botón positivo, así que la acción está confirmada
                    disabledUser(position)
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                    // Click en el botón negativo, así que la acción está cancelada
                    // Mantenemos el estado anterior del checkbox
                    holder.binding.checkboxEnabled.isChecked = usuarios.get(position).enabled

                }
                .setTitle(R.string.alertTitle) // El título
                .setMessage(mensaje) // El mensaje
                .create() // No olvides llamar a Create, ¡pues eso crea el AlertDialog!
                .show() // Mostramos el dialogo
        }

        val item = usuarios.get(position)
        holder.bind(item)

        //estamblim un listener
        holder.itemView.setOnClickListener {
            Toast.makeText(context, usuarios.get(position).userName, Toast.LENGTH_SHORT).show()
        }

        holder.binding.checkBoxAdmin.setOnClickListener {
            if (holder.binding.checkBoxAdmin.isChecked) {
                alertaAdminUser()
            } else {
                alertaNoAdminUser()
            }
        }

        holder.binding.checkboxEnabled.setOnClickListener {
            if (holder.binding.checkboxEnabled.isChecked) {
                alertaEnabledUser()
            } else {
                alertaDisabledUser()
            }
        }
    }

    override fun getItemCount(): Int {
        return usuarios.size
    }

    class ViewHolder(val binding: ItemUsersListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(usuario: Usuario) {
        }
    }
}