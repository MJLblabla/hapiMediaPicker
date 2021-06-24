package com.hapi.mediapicker

import android.content.Context
import android.content.pm.ProviderInfo
import androidx.core.content.FileProvider

/**
 * @date 2018/9/18
 */
class ImagePickProvider : FileProvider() {

    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)
        ImageChooseHelper.init(context)
    }
}
