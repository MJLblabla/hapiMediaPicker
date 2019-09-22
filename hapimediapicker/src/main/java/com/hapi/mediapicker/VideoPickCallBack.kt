package com.hapi.mediapicker


interface VideoPickCallBack : BaseCallBack {

    fun onSuccess(params: MediaParams)

    override fun onPermissionNotGet(permission: String) {

    }

}
