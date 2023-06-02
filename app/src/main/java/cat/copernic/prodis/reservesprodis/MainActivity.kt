package cat.copernic.prodis.reservesprodis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import cat.copernic.prodis.reservesprodis.databinding.ActivityMainBinding
import cat.copernic.prodis.reservesprodis.utils.Utils
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    // Drawer
    private lateinit var drawerLayout: DrawerLayout

    // APP Bar
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Auth Firebase
    private var auth = FirebaseAuth.getInstance()

    // Base de datos Firebase
    private val db = Firebase.firestore

    // Utils
    private val utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Host fragment para el navigation
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val navController = findNavController(R.id.myNavHostFragment)

        // Añadir cada pantalla al menú
        drawerLayout = binding.drawerLayout

        // Aparecer la APP Bar en las pantallas indicadas
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.principal,
                R.id.salas,
                R.id.vehiculos,
                R.id.reservas,
                R.id.perfil,
                R.id.usuarios,
                R.id.configuracion,
                R.id.iniciarSesion
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Botón Cerrar Sesión del menú drawable para que haga el logOut y te redirija al login
        binding.navView.menu.getItem(7).setOnMenuItemClickListener {
            auth.signOut()
            this.finish()
            true
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }

        if (auth.currentUser != null) {
            // Que el avatar sea clicable solo si el usuario está identificado
            binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.navHeaderImage).setOnClickListener() {
                navController.navigate(R.id.perfil)
                drawerLayout.closeDrawers()
            }

            // Cambiar el email en el nav header
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textViewEmail).text = utils.getEmail(auth)
            // Settear el nombre y el avatar
            val avatar =
                binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.navHeaderImage)
            db.collection("users").document(utils.getEmail(auth)).get()
                .addOnSuccessListener { document ->
                    binding.navView.getHeaderView(0)
                        .findViewById<TextView>(R.id.textViewName).text =
                        document.get("nombre").toString()
                    val avatarUrl = document.get("avatar").toString()
                    if (avatarUrl != "") {
                        Glide.with(applicationContext)
                            .load(avatarUrl)
                            .centerCrop()
                            .into(avatar)
                    }
                    val admin = document.get("admin") as Boolean
                    if (!admin) {
                        binding.navView.menu.getItem(5).isVisible = false // Usuarios
                    }
                }
        } else {
            binding.navView.menu.getItem(3).isVisible = false // Mis reservas
            binding.navView.menu.getItem(4).isVisible = false // Perfil
            binding.navView.menu.getItem(5).isVisible = false // Usuarios
            binding.navView.menu.getItem(6).isVisible = false // Configuración
        }
    }

    // Habilitar Nav Drawer
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.myNavHostFragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}