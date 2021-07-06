package com.mohdroid.webapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat

class Permissions(private val context: AppCompatActivity) {

    companion object {
        const val PERMISSION_REQUEST_USER_ACCESS_TO_THE_ACCOUNT = 0
        const val PERMISSIONS_REQUEST_CAMERA = 1
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2
    }

    @RequiresApi(Build.VERSION_CODES.M)
     fun readCameraPermission(): Int {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CAMERA)) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setTitle("Camera permission")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setMessage("Please enable access to camera")
                builder.setOnDismissListener { context.requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
                }
                builder.show()
            } else{
                requestPermissions(context, arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
            }
            return 0
        }
        else return 1

    }
     @RequiresApi(Build.VERSION_CODES.M)
     fun readGeoLocationPermission(): Int {
         // Do We need to ask for permission?
         if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             // Should we show an explanation?
             if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                 val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                 builder.setTitle("Location permission")
                 builder.setPositiveButton(android.R.string.ok, null)
                 builder.setMessage("Please enable access to Location")
                 builder.setOnDismissListener { context.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CAMERA)
                 }
                 builder.show()
             } else {
                 // No explanation needed, we can request the permission.
                 requestPermissions(context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)

             }
             return 0
         }
         else return 1
     }

}