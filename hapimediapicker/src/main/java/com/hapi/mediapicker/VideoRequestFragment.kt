package com.hapi.mediapicker

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import android.text.TextUtils
import com.hapi.ut.BitmapUtils
import com.hapi.ut.FileUtil
import com.hapi.ut.constans.FileConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

class VideoRequestFragment : androidx.fragment.app.Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.retainInstance = true
    }

    var callback: VideoPickCallBack? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {//从相册选取视频
            activity?.let {
                GlobalScope.launch(Dispatchers.Main) {
                    val ret = async(Dispatchers.IO) {
                        val path = ContentUriUtil.getDataFromUri(
                            it,
                            data.data!!,
                            ContentUriUtil.ContentType.video
                        )
                        getVideoParams(path)
                    }
                    val p = ret.await()
                    callback?.onSuccess(p ?: MediaParams())
                }
            }
        }
    }

    @WorkerThread
    fun getVideoParams(videoPath: String?): MediaParams? {
        if (FileConstants.CACHE_VIDEO_DIR == null) {
            activity?.application?.let { FileConstants.initFileConfig(it) }
        }
        if (TextUtils.isEmpty(videoPath)) {
            return null
        }
        val media = MediaMetadataRetriever()
        try {
            var path = videoPath
            val file = path?.let { File(it) }
            media.setDataSource(path)
            //取得指定时间（第6us）的Bitmap，即可以实现抓图（缩略图）功能
            val thumb = media.getFrameAtTime(3)
            val thumPath = FileConstants.CACHE_VIDEO_DIR + System.currentTimeMillis() + ".jpeg"
            BitmapUtils.saveBitmap2JPG(thumb, thumPath)
            val video_length =
                media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()?:0L
            val size = FileUtil.getFileSize(file)
            val mimeType = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            //如果不包含文件后缀就复制一份有后缀的文件
            if ((file?.name?.contains(".")) != true) {
                val suffix = mimeType?.substring(mimeType?.lastIndexOf("/") + 1)
                path = FileConstants.CACHE_VIDEO_DIR + file?.name + "." + suffix
                val newFile = FileUtil.createFile(path)
                newFile?.let {
                    file?.copyTo(it, true)
                }
            }
            return MediaParams(path, thumPath, size, video_length, mimeType)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            media.release()
        }
        return null
    }
}