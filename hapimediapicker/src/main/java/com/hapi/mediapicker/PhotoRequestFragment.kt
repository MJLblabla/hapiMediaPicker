package com.hapi.mediapicker


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.FileProvider
import android.util.Log
import com.hapi.mediapicker.PicPickHelper.Companion.TAG
import java.io.File

final class PhotoRequestFragment : androidx.fragment.app.Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.retainInstance = true
    }

    var size: Size? = null
    var callback: ImagePickCallback? = null

    var mCameraFilePath: String? = null
    var mTempFilePath: String? = null

    private fun checkSizeCrop(): Boolean {
        return !(size == null || size!!.aspectX < 1 || size!!.aspectY < 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            PicPickHelper.REQUEST_CODE_CHOOSE_LOCAL -> {
                if (data?.data != null) {
                    if (!checkSizeCrop()) {
                        val originFileData =
                            ContentUriUtil.getDataFromUri(
                                requireContext(),
                                data.data!!,
                                ContentUriUtil.ContentType.image
                            )
                        if(originFileData==null){
                            callback?.onSuccess("", data.data)
                        }else{
                            ImageCompression(requireContext())
                                .setOutputFilePath(ImageChooseHelper.compressFilePath)
                                .setCompressCallback(object : ImagePickCallback {

                                    override fun onSuccess(result: String?, uri: Uri?) {
                                        callback?.onSuccess(result, data.data)
                                    }
                                }).execute(originFileData)
                        }
                    } else {
                        mTempFilePath = ImageChooseHelper.cropFilePath
                        val filePath = ContentUriUtil.getDataFromUri(
                            requireContext(),
                            data.data!!,
                            ContentUriUtil.ContentType.image
                        )
                        val imageUri = FileProvider.getUriForFile(
                            requireContext(),
                            ImageChooseHelper.PROVIDER_KEY,
                            File(filePath!!)
                        )
                        val intent = ImageChooseHelper.cropImageIntent(
                            requireActivity(),
                            imageUri!!,
                            size!!,
                            mTempFilePath!!
                        )
                        startActivityForResult(intent, PicPickHelper.REQUEST_CODE_CROP)
                    }
                } else {
                    Log.e(TAG, "REQUEST_CODE_CHOOSE_LOCAL data.getData() is null")
                }
            }
            PicPickHelper.REQUEST_CODE_CAMERA -> {
                if (!checkSizeCrop()) {
                    ImageCompression(requireContext())
                        .setOutputFilePath(ImageChooseHelper.compressFilePath)
                        .setCompressCallback(object : ImagePickCallback {
                            override fun onSuccess(result: String?, uri: Uri?) {
                                callback?.onSuccess(result, data?.data)
                            }
                        }).execute(mCameraFilePath)
                } else {
                    val imageUri =
                        FileProvider.getUriForFile(
                            requireContext(),
                            ImageChooseHelper.PROVIDER_KEY,
                            File(mCameraFilePath!!)
                        )
                    mTempFilePath = ImageChooseHelper.cropFilePath
                    val intent = ImageChooseHelper.cropImageIntent(
                        requireActivity(),
                        imageUri,
                        size!!,
                        mTempFilePath!!
                    )
                    startActivityForResult(intent, PicPickHelper.REQUEST_CODE_CROP)
                }
            }
            PicPickHelper.REQUEST_CODE_CROP -> {// 所有图片选取都得走得这一步
                ImageCompression(requireContext())
                    .setOutputFilePath(ImageChooseHelper.compressFilePath)
                    .setCompressCallback(object : ImagePickCallback {
                        override fun onSuccess(result: String?, uri: Uri?) {
                            callback?.onSuccess(result, data?.data)
                        }
                    }).execute(mTempFilePath)
            }
            else -> {
            }
        }
    }

}
