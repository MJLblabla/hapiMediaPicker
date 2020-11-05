package com.hapi.mediapicker

import android.net.Uri

/**
 *
 * @date 2018/12/14
 */
interface ImagePickCallback :BaseCallBack{

    fun onSuccess(result: String?,url:Uri?)

    override fun onPermissionNotGet(permission: String){

    }
}