package com.hapi.mediapicker

/**
 * @author athoucai
 * @date 2018/12/14
 */
interface ImagePickCallback :BaseCallBack{

    fun onSuccess(result: String?)

    override fun onPermissionNotGet(permission: String){

    }
}