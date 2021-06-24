package com.hapi.mediapicker

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.hapi.ut.OsInfoUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import java.lang.ref.WeakReference

/**
 *
 * @date 2018/12/14
 */
class VideoPickHelper constructor(activity: androidx.fragment.app.FragmentActivity) {

    private var host = WeakReference<androidx.fragment.app.FragmentActivity>(activity)
    private val rxPermissions by lazy { RxPermissions(activity) }
    private val videoRequestFragment by lazy { getPhotoRequstFragment(activity) }

    fun show(callback: VideoPickCallBack) {

        videoRequestFragment.callback = callback
        val bottomDialog = Dialog(host.get(), R.style.BottomViewWhiteMask)
        val contentView = LayoutInflater.from(host.get()).inflate(R.layout.dialog_choose_pic, null)
        bottomDialog.setCancelable(true)
        bottomDialog.setCanceledOnTouchOutside(true)
        bottomDialog.setContentView(contentView)
        bottomDialog.show()

        val imageChooseListener = View.OnClickListener { v ->
            bottomDialog.dismiss()
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

        val attributes = bottomDialog.window.attributes
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.gravity = Gravity.BOTTOM
        bottomDialog.window.attributes = attributes
    }


    // 获取Fragment的方法
    private fun getPhotoRequstFragment(activity: androidx.fragment.app.FragmentActivity): VideoRequestFragment {
        // 查询是否已经存在了该Fragment，这样是为了让该Fragment只有一个实例
        var rxPermissionsFragment: VideoRequestFragment? = findPhotoRequstFragment(activity)
        val isNewInstance = rxPermissionsFragment == null
        // 如果还没有存在，则创建Fragment，并添加到Activity中
        if (isNewInstance) {
            rxPermissionsFragment = VideoRequestFragment()
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
    private fun findPhotoRequstFragment(activity: androidx.fragment.app.FragmentActivity): VideoRequestFragment? {
        return activity.fragmentManager.findFragmentByTag(TAG) as VideoRequestFragment?
    }

    fun fromCamera(callback: VideoPickCallBack){

        videoRequestFragment.callback = callback
        fromCamera()
    }
    private fun fromCamera() {
        host.get()?.let {
            rxPermissions.requestEachCombined(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                .subscribe{permission->
                    if (permission.granted) {//全部同意后调用
                        val intent = ImageChooseHelper.videoIntent(it)
                        videoRequestFragment.startActivityForResult(intent, REQUEST_CODE_CAMERA)
                    } else if (permission.shouldShowRequestPermissionRationale) {//只要有一个选择：禁止，但没有选择“以后不再询问”，以后申请权限，会继续弹出提示
                        videoRequestFragment.callback?.onPermissionNotGet(permission.name)
                    } else {//只要有一个选择：禁止，但选择“以后不再询问”，以后申请权限，不会继续弹出提示
                        videoRequestFragment.callback?.onPermissionNotGet(permission.name)
                    }
                }
        }
    }

    fun fromLocal(callback: VideoPickCallBack){
        videoRequestFragment.callback = callback
        fromLocal()
    }

    private fun fromLocal() {
        host.get()?.let {
            rxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe{
                        permission->
                    if (permission.granted) {//全部同意后调用
                        val intent =
                            Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "video/*" //String VIDEO_UNSPECIFIED = "video/*";

                        val wrapperIntent =
                            Intent.createChooser(intent, null)

                        videoRequestFragment.startActivityForResult(wrapperIntent, REQUEST_CODE_PICK)

                    } else if (permission.shouldShowRequestPermissionRationale) {//只要有一个选择：禁止，但没有选择“以后不再询问”，以后申请权限，会继续弹出提示
                        videoRequestFragment.callback?.onPermissionNotGet(permission.name)
                    } else {//只要有一个选择：禁止，但选择“以后不再询问”，以后申请权限，不会继续弹出提示
                        videoRequestFragment.callback?.onPermissionNotGet(permission.name)
                    }
                }
        }
    }



    companion object {
        private val TAG = "VideoPickHelper"
        val REQUEST_CODE_PICK = RequestCodeCreator.generate()
        val REQUEST_CODE_CAMERA = RequestCodeCreator.generate()
    }


}