package com.hapi.permission;

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

object HapiPermission {
    @JvmField
    var permissionFragment: PermissionFragment? = null
    fun requestPermission(
        context: AppCompatActivity,
        permissions: Array<String>,
        permissionCallback: PermissionCallback?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var isAllGet = true
            for (p in permissions) {
                if (PackageManager.PERMISSION_GRANTED != context.checkSelfPermission(
                        p
                    )
                ) {
                    isAllGet = false
                }
            }
            if (isAllGet) {
                permissionCallback?.onComplete(permissions.toList(), ArrayList(), ArrayList());
                return
            }
            if (permissionFragment == null) {
                permissionFragment = PermissionFragment()
            }
            permissionFragment!!.setOnAttachCallback {
                permissionFragment!!.requestPermission(
                    permissions
                )
            }
            permissionFragment!!.setOnPermissionCallback(permissionCallback)
            val fragmentTransaction = context.supportFragmentManager.beginTransaction()
            fragmentTransaction.add(permissionFragment!!, "permissionFragment@777").commit()
        } else {
            permissionCallback?.onComplete(permissions.toList(), ArrayList(), ArrayList());
        }
    }
}