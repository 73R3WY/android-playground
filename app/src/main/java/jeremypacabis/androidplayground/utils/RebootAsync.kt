package jeremypacabis.androidplayground.utils

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import eu.chainfire.libsuperuser.Shell

/**
 * Created by jeremypacabis on July 08, 2018.
 * @author Jeremy Patrick Pacabis <jeremy@ingenuity.ph>
 * jeremypacabis.androidplayground.utils <AndroidPlayground>
 */
class RebootAsync(private val context: Context) : AsyncTask<String, Void, Void>() {

    override fun doInBackground(vararg params: String?): Void? {
        // Check if superuser (root access) is available and execute reboot, else display toast message
        if (Shell.SU.available()) {
            when (params[0]) {
                "reboot" -> Shell.SU.run("reboot")
                "recov" -> Shell.SU.run("reboot recovery")
                "shutdown" -> Shell.SU.run("reboot -p")
                "sysui" -> Shell.SU.run("pkill com.android.systemui")
            }
        } else {
            (context as Activity).runOnUiThread {
                Toast.makeText(context, "Device is not rooted!", Toast.LENGTH_SHORT).show()
            }
        }

        return null
    }
}