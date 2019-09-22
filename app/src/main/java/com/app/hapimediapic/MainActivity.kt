package com.app.hapimediapic

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.hapi.mediapicker.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val mPicPickHelper = PicPickHelper(this)
        val mVideoPickHelper = VideoPickHelper(this)


        // 选择图片　默认选择弹窗
        button.setOnClickListener {
            mPicPickHelper.show(Size(1,1), object : ImagePickCallback {
                override fun onSuccess(result: String?) {
                    Toast.makeText(this@MainActivity, "文件路径${result}", Toast.LENGTH_SHORT).show()
                }

            })
        }

        // 选择图片从　相机
        button3.setOnClickListener {
            mPicPickHelper.fromCamera(null, object : ImagePickCallback {
                override fun onSuccess(result: String?) {
                    Toast.makeText(this@MainActivity, "文件路径${result}", Toast.LENGTH_SHORT).show()
                }

            })
        }

        // 选择图片从　图库
        button2.setOnClickListener {
            mPicPickHelper.fromLocal(Size(1,1), object : ImagePickCallback {
                override fun onSuccess(result: String?) {
                    Toast.makeText(this@MainActivity, "文件路径${result}", Toast.LENGTH_SHORT).show()
                }
            })
        }


        //选择视频　使用默认弹窗
        button4.setOnClickListener {
            mVideoPickHelper.show(object : VideoPickCallBack {
                override fun onSuccess(params: MediaParams) {
                    val path = params.path
                    val thumb = params.thumbPath
                    val len = params.duration
                    val size = params.size

                    Toast.makeText(this@MainActivity, "文件路径${path}", Toast.LENGTH_SHORT).show()
                }
            })

        }

        //选择视频　从图库
        button5.setOnClickListener {
            mVideoPickHelper.fromLocal(object : VideoPickCallBack {
                override fun onSuccess(params: MediaParams) {
                    val path = params.path
                    val thumb = params.thumbPath
                    val len = params.duration
                    val size = params.size

                    Toast.makeText(this@MainActivity, "文件路径${path}", Toast.LENGTH_SHORT).show()
                }
            })

        }

        //选择视频　从相击
        button6.setOnClickListener {
            mVideoPickHelper.fromCamera(object : VideoPickCallBack {
                override fun onSuccess(params: MediaParams) {
                    val path = params.path
                    val thumb = params.thumbPath
                    val len = params.duration
                    val size = params.size


                    Toast.makeText(this@MainActivity, "文件路径${path}", Toast.LENGTH_SHORT).show()
                }
            })

        }
    }
}
