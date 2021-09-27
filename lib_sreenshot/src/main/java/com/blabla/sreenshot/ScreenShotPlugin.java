package com.blabla.sreenshot;

import static com.blabla.sreenshot.Constants.ERROR_CODE_NO_PERMISSION;
import static com.blabla.sreenshot.Constants.ERROR_CODE_RECORD_DIR_ERROR;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.blabla.sreenshot.utils.RequestFragment;
import com.blabla.sreenshot.utils.RequestFragmentHelper;

import java.io.File;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class ScreenShotPlugin {

    public interface OnScreenShotListener {
        void onFinish(Bitmap bitmap);
        public void onError(int code, String msg);
    }


    public static final int REQUEST_CODE = 10031386;

    private static class InstanceHolder {
        private static final ScreenShotPlugin instance = new ScreenShotPlugin();
    }

    public static ScreenShotPlugin getInstance() {
        return InstanceHolder.instance;
    }

    private ScreenShotPlugin() {
        super();
    }


    private ServiceConnection serviceConnection;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startMediaRecorder(final FragmentActivity activity, final OnScreenShotListener callback) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // 此处宽高需要获取屏幕完整宽高，否则截屏图片会有白/黑边
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        RequestFragment fragment = RequestFragmentHelper.INSTANCE.getPermissionReqFragment(activity);
        fragment.setCall(new Function3<Integer, Integer, Intent, Unit>() {
            @Override
            public Unit invoke(Integer integer, final Integer integer2, final Intent intent) {
                if (createVirtualDisplay(integer, integer2)) {
                     serviceConnection = new ServiceConnection() {

                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            ScreenShotService   mediaProjectionService = ((ScreenShotService.MediaProjectionBinder) iBinder).getService();
                            mediaProjectionService.start(integer2, intent, new OnScreenShotListener() {
                                @Override
                                public void onFinish(Bitmap bitmap) {
                                    callback.onFinish(bitmap);
                                    ScreenShotService.unbindService(activity,serviceConnection);
                                }

                                @Override
                                public void onError(int code, String msg) {
                                    ScreenShotService.unbindService(activity,serviceConnection);
                                }
                            });
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {

                        }
                    };
                    ScreenShotService.bindService(activity, serviceConnection);
                }
                return null;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            private boolean createVirtualDisplay(int requestCode, int resultCode) {

                if (requestCode != REQUEST_CODE) {
                    callback.onError(ERROR_CODE_NO_PERMISSION, "no permission");
                    return false;
                }
                if (resultCode != Activity.RESULT_OK) {
                    callback.onError(ERROR_CODE_NO_PERMISSION, "no permission");
                    return false;
                }
                return true;
            }
        });
        fragment.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }
}
