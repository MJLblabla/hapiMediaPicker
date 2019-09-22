


 implementation('com.pince.maven:lib-imgPicker:1.0.0')

     val mPicPickHelper = PicPickHelper(this)
           val mVideoPickHelper = VideoPickHelper(this)


           // 选择图片　默认选择弹窗
           button.setOnClickListener {
               mPicPickHelper.show(Size.Cover, object : ImagePickCallback {
                   override fun onSuccess(result: String?) {
                       Toast.makeText(this@MainActivity, "文件路径${result}", Toast.LENGTH_SHORT).show()
                   }

               })
           }

           // 选择图片从　相机
           button3.setOnClickListener {
               mPicPickHelper.fromCamera(Size.Cover, object : ImagePickCallback {
                   override fun onSuccess(result: String?) {
                       Toast.makeText(this@MainActivity, "文件路径${result}", Toast.LENGTH_SHORT).show()
                   }

               })
           }

           // 选择图片从　图库
           button2.setOnClickListener {
               mPicPickHelper.fromLocal(Size.Cover, object : ImagePickCallback {
                   override fun onSuccess(result: String?) {
                       Toast.makeText(this@MainActivity, "文件路径${result}", Toast.LENGTH_SHORT).show()
                   }
               })
           }


           //选择视频　使用默认弹窗
           button4.setOnClickListener {
               mVideoPickHelper.show(VideoPickCallBack { params ->
                   val path = params?.path
                   val thumb = params?.thumbPath
                   val len = params?.duration
                   val size = params?.size

                   Toast.makeText(this@MainActivity, "文件路径${path}", Toast.LENGTH_SHORT).show()
               })
           }

           //选择视频　从图库
           button5.setOnClickListener {
               mVideoPickHelper.fromLocal(VideoPickCallBack { params ->
                   val path = params?.path
                   val thumb = params?.thumbPath
                   val len = params?.duration
                   val size = params?.size

                   Toast.makeText(this@MainActivity, "文件路径${path}", Toast.LENGTH_SHORT).show()
               })
           }

           //选择视频　从相击
           button6.setOnClickListener {
               mVideoPickHelper.fromCamera(VideoPickCallBack { params ->
                   val path = params?.path
                   val thumb = params?.thumbPath
                   val len = params?.duration
                   val size = params?.size
                   Toast.makeText(this@MainActivity, "文件路径${path}", Toast.LENGTH_SHORT).show()
               })
           }


       }
}
　
　
　