package com.hapi.mediapicker

/**
 *
 * @date 2018/12/14
 */
interface ImagePickCallback :BaseCallBack{

    fun onSuccess(result: String?)

    override fun onPermissionNotGet(permission: String){

    }
}