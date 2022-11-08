package com.hapi.mediapicker

import android.Manifest
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hapi.permission.HapiPermission

import java.lang.ref.WeakReference

class PicPickHelper(activity: AppCompatActivity) {
    private var activityWeakReference = WeakReference(activity)
    private val photoRequestFragment by lazy { getPhotoRequstFragment(activity) }

    /**
     * 使用默认选择弹窗
     */
    fun show(size: Size?, callback: ImagePickCallback) {
        photoRequestFragment.callback = callback
        photoRequestFragment.size = size
        val bottomDialog = activityWeakReference.get()
            ?.let { Dialog(it, R.style.BottomViewWhiteMask) }
        val contentView = LayoutInflater.from(activityWeakReference.get())
            .inflate(R.layout.dialog_choose_pic, null)
        bottomDialog?.setCancelable(true)
        bottomDialog?.setCanceledOnTouchOutside(true)
        bottomDialog?.setContentView(contentView)
        bottomDialog?.show()

        val imageChooseListener = View.OnClickListener { v ->
            bottomDialog?.dismiss()
            when (v.id) {
                R.id.view1 -> {
                    fromCamera()
                }
                R.id.view2 -> {
                    fromLocal()
                }
            }
        }
        contentView.findViewById<TextView>(R.id.view1).setOnClickListener(imageChooseListener)
        contentView.findViewById<TextView>(R.id.view2).setOnClickListener(imageChooseListener)
        contentView.findViewById<TextView>(R.id.view3).setOnClickListener(imageChooseListener)

        val attributes = bottomDialog?.window?.attributes
        attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes?.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes?.gravity = Gravity.BOTTOM
        bottomDialog?.window?.attributes = attributes
    }

    /**
     * 打开相机选择
     */
    fun fromCamera(size: Size?, callback: ImagePickCallback) {
        photoRequestFragment.callback = callback
        photoRequestFragment.size = size
        fromCamera()
    }

    /**
     * 打开相册
     */
    fun fromLocal(size: Size?, callback: ImagePickCallback) {
        photoRequestFragment.callback = callback
        photoRequestFragment.size = size
        fromLocal()
    }

    // 获取Fragment的方法
    private fun getPhotoRequstFragment(activity: androidx.fragment.app.FragmentActivity): PhotoRequestFragment {
        // 查询是否已经存在了该Fragment，这样是为了让该Fragment只有一个实例
        var rxPermissionsFragment: PhotoRequestFragment? = findPhotoRequstFragment(activity)
        val isNewInstance = rxPermissionsFragment == null
        // 如果还没有存在，则创建Fragment，并添加到Activity中
        if (isNewInstance) {
            rxPermissionsFragment = PhotoRequestFragment()
            val fragmentManager = activity.supportFragmentManager

            fragmentManager
                .beginTransaction()
                .add(rxPermissionsFragment, TAG)
                .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }

        return rxPermissionsFragment!!
    }

    // 利用tag去找是否已经有该Fragment的实例
    private fun findPhotoRequstFragment(activity: androidx.fragment.app.FragmentActivity): PhotoRequestFragment? {
        return activity.fragmentManager.findFragmentByTag(TAG) as PhotoRequestFragment?
    }

    private fun fromCamera() {
        activityWeakReference.get()?.let {
            HapiPermission.requestPermission(
                it, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            ) { grantedPermissions, deniedPermissions, alwaysDeniedPermissions ->
                if (grantedPermissions.size == 2) {
                    val mCameraFilePath = ImageChooseHelper.cameraFilePath
                    photoRequestFragment.mCameraFilePath = mCameraFilePath
                    photoRequestFragment.startActivityForResult(
                        ImageChooseHelper.takePhotoIntent(
                            it,
                            mCameraFilePath
                        ), REQUEST_CODE_CAMERA
                    )
                } else {
                    photoRequestFragment.callback?.onPermissionNotGet(deniedPermissions.toString() + alwaysDeniedPermissions.toString())
                }
            }
        }
    }

    private fun fromLocal() {
        activityWeakReference.get()?.let {
            HapiPermission.requestPermission(
                it, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) { grantedPermissions, deniedPermissions, alwaysDeniedPermissions ->
                if (grantedPermissions.size == 1) {
                    photoRequestFragment.startActivityForResult(
                        ImageChooseHelper.pickImageIntent(),
                        REQUEST_CODE_CHOOSE_LOCAL
                    )
                } else {
                    photoRequestFragment.callback?.onPermissionNotGet(deniedPermissions.toString() + alwaysDeniedPermissions.toString())
                }
            }
        }
    }

    companion object {
        val REQUEST_CODE_CHOOSE_LOCAL = RequestCodeCreator.generate()
        val REQUEST_CODE_CAMERA = RequestCodeCreator.generate()
        val REQUEST_CODE_CROP = RequestCodeCreator.generate()
        val TAG = "PhotoRequestFragment"
    }
}
