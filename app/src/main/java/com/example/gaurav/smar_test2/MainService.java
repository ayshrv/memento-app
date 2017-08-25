package com.example.gaurav.smar_test2;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by gaurav on 29/1/17.
 */
public class MainService extends Service {
    String SERVICE_TAG = "service_tag";
    String LOG_TAG = "AudioRecordTest";
    String LOG_TAG1 = "AudioRecordTest1";
    Thread main_thread = null;
    public static int MAIN_THREAD_SLEEP_TIME = 10;
    public static int RECORD_DURATION = 500;
    public static int THRESHOLD_SOUND_LEVEL = 2000;
    public static int APP_START = 1;
    Camera mCamera;
    private static final String IMAGE_FOLDER = "Smar/Image";
    private static final String AUDIO_RECORDER_FOLDER = "Smar/Audio";
    String smar_group_id = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String getCurrentTimeStamp() {
        String date = (DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()).toString());
        return date;
    }

    public String getAudioFileName(String time_stamp) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path +="/Smar/Audio/"+time_stamp+".wav";
        return path;
    }

    public String getImageFileName(String time_stamp) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path +="/Smar/Image/"+time_stamp+".jpg";
        return path;
    }

    public void capture_image(final String time_stamp){
        mCamera = Camera.open();
        try {
            mCamera.setPreviewTexture(new SurfaceTexture(10));
        } catch (IOException e1) {
            Log.e("CAMERA","Error in camera setPreviewTexture");
            LogViewer.addLog("MainService : Error in camera setPreviewTexture");
        }

        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(640, 480);
        //params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setPictureFormat(ImageFormat.JPEG);
        mCamera.setParameters(params);
        mCamera.startPreview();
        mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(getImageFileName(time_stamp));
                    outStream.write(data);
                    outStream.close();
                    Log.d("CAMERA", "onPictureTaken - wrote bytes: " + data.length);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally
                {
                    startBingVisionService(time_stamp);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        });
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e(SERVICE_TAG, "Service thread started");
            Recorder r = new Recorder();
            String time_stamp = "";
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(MAIN_THREAD_SLEEP_TIME);
                    double level = r.getAmplitude();
                    //Log.e(LOG_TAG,Double.toString(level));

                    if (r.state == 1) { // active state
                        //Log.e(LOG_TAG,"state is 1");
                        r.time ++;
                        if (r.time > RECORD_DURATION) {
                            if ((level > THRESHOLD_SOUND_LEVEL) && APP_START==1) {
                                r.time = 0;
                                capture_image(getCurrentTimeStamp());
                            } else {
                                r.time = 0;
                                r.state = 0;
                                Log.e(SERVICE_TAG, "recording closed");

                                LogViewer.addLog("audio recording closed");

                                r.onRecord(false, getAudioFileName(time_stamp));
                                startBingSpeechService(time_stamp);
                                r.onRecord(true, null);
                            }
                        }
                    }

                    if ((r.state == 0) && (level > THRESHOLD_SOUND_LEVEL) &&  (APP_START==1)) { // passive state
                        r.onRecord(false, null);
                        r.state = 1;
                        time_stamp = getCurrentTimeStamp();
                        Log.e(SERVICE_TAG,"recording started");

                        LogViewer.addLog("audio recording started");

                        r.onRecord(true, getAudioFileName(time_stamp));
                        capture_image(time_stamp);
                    }

                } catch (InterruptedException e) {
                    Log.e(SERVICE_TAG,"In catch clause main thread");
                    e.printStackTrace();
                    r.onRecord(false,null);
                    r.onStop();
                    Thread.currentThread().interrupt();
                }
            }
            r.onStop();
            Log.e(SERVICE_TAG, "main thread interrupted successfully");
        }
    };

    public void startBingSpeechService(String time_stamp) {
        Intent intent = new Intent(this, bingSpeechService.class);
        intent.putExtra("time_stamp",time_stamp);

        startService(intent);
    }

    public void startBingVisionService(String time_stamp) {
        Intent intent = new Intent(this, bingVisionService.class);
        intent.putExtra("time_stamp",time_stamp);
        intent.putExtra("smar_group_id",smar_group_id);

        startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        smar_group_id = intent.getStringExtra("smar_group_id");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.e(SERVICE_TAG,"Service is created");

        // create Audio + Image directory if not exists
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,IMAGE_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(filepath,AUDIO_RECORDER_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }

        // start new thread and you your work there
        main_thread = new Thread(runnable);
        main_thread.start();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        int ONGOING_NOTIFICATION_ID = 001;
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        main_thread.interrupt();
        Log.e(SERVICE_TAG,"Service is destroyed");
    }
}
