package com.blabla.sreenshot;

import static com.blabla.sreenshot.ScreenShotPlugin.REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.nio.ByteBuffer;


public class ScreenShotService extends Service {
    private static final int ID_MEDIA_PROJECTION = REQUEST_CODE;

    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplayMediaRecorder;
    private ImageReader mImageReader;


    public class MediaProjectionBinder extends Binder {
        public ScreenShotService getService() {
            return ScreenShotService.this;
        }
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void start(int resultCode, Intent resultData, ScreenShotPlugin.OnScreenShotListener callback) {
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager == null) {
           stopSelf();
            return;
        }
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
        if (mediaProjection == null) {
            stopSelf();
            return;
        }
        int w = getScreenWidth();
        int h = getScreenHeight();
        mImageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 1);
        virtualDisplayMediaRecorder = mediaProjection.createVirtualDisplay(
                "screen-mirror",
                w,
                h,
                Resources.getSystem().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                null,
                null
        );
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Image image = imageReader.acquireLatestImage();
                if (image == null) {
                    return;
                }
                int width = image.getWidth();
                int height = image.getHeight();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                image.close();
                callback.onFinish(bitmap);
                mImageReader.close();
                stopSelf();
            }
        }, new Handler());
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    /**
     * 绑定Service
     *
     * @param context           context
     * @param serviceConnection serviceConnection
     */
    public static void bindService(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, ScreenShotService.class);
        boolean bindService = context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        Log.d("MediaProjectionService", "bindService " + bindService);
    }

    /**
     * 解绑Service
     *
     * @param context           context
     * @param serviceConnection serviceConnection
     */
    public static void unbindService(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MediaProjectionService", "onBind ");
        return new MediaProjectionBinder();
        // return mMessenger.getBinder();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MediaProjectionService", "onCreate");
        // 获取服务通知
        Notification notification = createForegroundNotification();
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        startForeground(ID_MEDIA_PROJECTION, notification);
    }

    private Notification createForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 唯一的通知通道的id.
        String notificationChannelId = "notification_channel_id_01";

        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "Foreground Service Notification";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");
            //LED灯
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            //震动
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        //通知小图标
        // builder.setSmallIcon(R.drawable.ic_launcher);
        //通知标题
        builder.setContentTitle("RoomRecordService");
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis());
        //创建通知并返回
        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MediaProjectionService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 销毁
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void destroy() {
        Log.d("MediaProjectionService", "destroy");
        stopMediaRecorder();
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        if (mediaProjectionManager != null) {
            mediaProjectionManager = null;
        }
        stopForeground(true);
    }


    /**
     * 结束 媒体录制
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopMediaRecorder() {

        if (virtualDisplayMediaRecorder != null) {
            virtualDisplayMediaRecorder.release();
            virtualDisplayMediaRecorder = null;
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
    }
}
