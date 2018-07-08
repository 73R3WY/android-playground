package jeremypacabis.androidplayground.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import jeremypacabis.androidplayground.R
import jeremypacabis.androidplayground.utils.RebootAsync
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Created by jeremypacabis on June 27, 2018.
 * @author Jeremy Patrick Pacabis <jeremy@ingenuity.ph>
 * jeremypacabis.androidplayground <AndroidPlayground>
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_COARSE_LOCATION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialize()
    }

    private fun initialize() {
        // On click action for the Shutdown button with root access
        button_shutdown_root.setOnClickListener {
            Log.e("onClick", (it as Button).text.toString())
            rootShutdown()
        }

        // On click action for the Shutdown button without root access
        button_shutdown_no_root.setOnClickListener {
            Log.e("onClick", (it as Button).text.toString())
            rootlessShutdown()
        }

        // On click action for the Turn on Location button
        button_activate_location.setOnClickListener {
            Log.e("onClick", (it as Button).text.toString())
            activateAndGetLocation()
        }
    }

    private fun rootShutdown() {
        RebootAsync(this).execute("reboot")
//        try {
//            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot -p"))
//            process.waitFor()
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//        }
    }

    private fun rootlessShutdown() {
//        val shutdownIntent = Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN")
//        shutdownIntent.putExtra("android.intent.extra.KEY_CONFIRM", true)
//        startActivity(shutdownIntent)
//
//        val shutdownIntent = Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN")
//        shutdownIntent.putExtra("android.intent.extra.KEY_CONFIRM", false)
//        shutdownIntent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
//        shutdownIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(shutdownIntent)
    }

    private fun activateAndGetLocation() {
//        requestLocationPermission()
        statusCheck()
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            val longitude = location?.longitude
            val latitude = location?.latitude
            location_info.text = "Lng: $longitude Lat: $latitude"
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
            statusCheck()
        }

        override fun onProviderDisabled(provider: String?) {
            location_info.text = getString(R.string.gps_disabled)
            statusCheck()
        }
    }

    private fun statusCheck() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10F, locationListener)
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ -> startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun requestLocationPermission() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Location Permission")
        alertDialogBuilder.setMessage("This app needs locations permissions. Please grant this permission to continue using the features of the app.")
        alertDialogBuilder.setNegativeButton(android.R.string.no, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                alertDialogBuilder.setPositiveButton(android.R.string.yes, { _, _ ->
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
                })
                alertDialogBuilder.show()
            }
        } else {
            val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val gpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!gpsProviderEnabled && !networkProviderEnabled) {
                alertDialogBuilder.setPositiveButton(android.R.string.yes, { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                })
                alertDialogBuilder.show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Android Playground", "Coarse Location permission granted")
                } else {
                    val intent = Intent()
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = uri
                    startActivity(intent)
                }
            }
        }
    }
}