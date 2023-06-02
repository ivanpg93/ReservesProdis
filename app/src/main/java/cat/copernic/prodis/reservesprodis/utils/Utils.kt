package cat.copernic.prodis.reservesprodis.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.isDigitsOnly
import cat.copernic.prodis.reservesprodis.R
import com.google.firebase.auth.FirebaseAuth

class Utils {
    fun showAlert(title: Int, msg: Int, act: Context) {
        val builder = AlertDialog.Builder(act)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(R.string.accept, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Comprueba que el DNI introducido sea válido
    fun checkDni(dni: String): Boolean {
        // Recoge los números de DNI en una variable
        val dniNum = dni.substring(0, dni.length - 1)
        // Revisa que la longitud sea de 9 caracteres y no sea sólo números
        if (dni.length < 9 || !dniNum.isDigitsOnly()) {
            return false
        }
        // Recogemos la letra del DNI en una variable...
        val dniLetra = dni.substring(dni.length - 1).uppercase()
        val letrasDni = "TRWAGMYFPDXBNJZSQVHLCKE"
        // ... y la comparamos con este algoritmo
        return dniLetra == letrasDni[dniNum.toInt() % 23].toString()
    }

    fun checkLength(input: String, length: Int): Boolean {
        return input.length == length
    }

    // Get DNI del auth de Firebase
    fun getEmail(auth: FirebaseAuth): String {
        return auth.currentUser!!.email!!
    }

    private fun channelNotification(act: Context, resources: Resources) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //>=26 version Oreo i superiors
            val title = resources.getString(R.string.accDisabledTitle)
            val channel = NotificationChannel(
                "channel_user_disabled",
                title,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = resources.getString(R.string.accDisabledText)
            val notificationManager =
                act!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notificationUserDisabled(act: Context, resources: Resources) {
        channelNotification(act, resources)

        // Build notificación
        val mBuilder = NotificationCompat.Builder(act, "channel_user_disabled")
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle(resources.getString(R.string.accDisabledTitle))
            .setContentText(resources.getString(R.string.accDisabledText))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(resources.getString(R.string.accDisabledTextLong))
            )

        // Enviar notificación
        val notificationManager = NotificationManagerCompat.from(act)
        notificationManager.notify(1, mBuilder.build())
    }

    // Traduce un string de booleano de Firebase a un array de Booleanos
    fun translateArr(word: String): Array<Boolean> {
        // Elimina los corchetes del String
        val replace = word.replace("[\\[\\]]".toRegex(), "")
        // Pasa el String a un arrayList
        val newArr = listOf(replace.split(", "))

        // Pasa el arrayList a un array
        val finalArr: Array<Boolean> = arrayOf(false, false, false, false, false, false, false)
        for (i in newArr[0].indices) {
            finalArr[i] = newArr[0][i].toBoolean()
        }
        return finalArr
    }
}