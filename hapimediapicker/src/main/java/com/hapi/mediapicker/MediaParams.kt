package com.hapi.mediapicker

/**
 * @author athoucai
 * @date 2019/2/18
 */
class MediaParams constructor(var path: String? = null,
                              var thumbPath: String? = null,
                              var size: Long = -1,
                              var duration: Long = -1,
                              var mimeType: String? = null) {
}