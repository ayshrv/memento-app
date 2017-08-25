package com.example.gaurav.smar_test2;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.example.gaurav.smar_test2.helper.ImageHelper;
import com.example.gaurav.smar_test2.helper.SampleApp;
import com.example.gaurav.smar_test2.helper.StorageHelper;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.TrainingStatus;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Created by gaurav on 1/2/17.
 */
public class bingVisionService extends IntentService {
    private Bitmap mBitmap;
    private VisionServiceClient client;
    private String LOG_TAG = "vision";
    private CountDownLatch doneSignal = null;

    private String LOG_TAG1 = "face_service";
    List<Face> faces;
    String mPersonGroupId;
    boolean detected;
    private String vision_result = "";
    private String face_result = "";

    public bingVisionService() {
        super("bingVisionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        doneSignal = new CountDownLatch(1);
        detected = false;
        vision_result = "";
        face_result = "";
        String time_stamp = intent.getStringExtra("time_stamp");
        String filename = getImageFileName(time_stamp);
        mPersonGroupId = intent.getStringExtra("smar_group_id");
        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.vision_key));
        }

        getImageCaption(filename);
        try {
            Log.e(LOG_TAG,"waiting for signal");
            doneSignal.await();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG,"error in doneSignal");
            LogViewer.addLog("Vision API: error in doneSignal");
            e.printStackTrace();
        }
        DBHelper mDBHelper = DBHelper.getInstance(this);
        mDBHelper.saveVision(time_stamp,vision_result);
        mDBHelper.saveFace(time_stamp, face_result);

        //mDBHelper.search("why do people hello");
        //mDBHelper.getItemDetail("2017-02-03 07:37:38");
        deleteImageFile(filename);
        Log.e(LOG_TAG, "done!");
    }

    public String getImageFileName(String time_stamp) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path +="/Smar/Image/"+time_stamp+".jpg";
        return path;
    }

    public void doDescribe() {
        Log.e(LOG_TAG, "Describing...");
        try {
            new doRequest().execute();
        } catch (Exception e)
        {
            Log.e(LOG_TAG,"Error encountered. Exception is: " + e.toString());
            LogViewer.addLog("Vision API: Error in doDescribe. "+ e.toString());
            doneSignal.countDown();
        }
    }

    private void getImageCaption(String filename) {
        Log.e(LOG_TAG,"get Image Caption called");
        mBitmap = ImageHelper.loadSizeLimitedBitmapFromPath(filename);
        if (mBitmap != null) {
            // Add detection log.
            Log.e(LOG_TAG, " resized to " + mBitmap.getWidth()
                    + "x" + mBitmap.getHeight());

            doDescribe();
        }
    }

    private void deleteImageFile(String filename) {
        File file = new File(filename);
        file.delete();
    }


    private String process() throws VisionServiceException, IOException {
        Log.e(LOG_TAG,"processing....");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v = this.client.describe(inputStream, 1);

        String result = gson.toJson(v);
        Log.e(LOG_TAG, result);

        return result;
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
                doneSignal.countDown();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                Log.e(LOG_TAG,"Error in post execute: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);

                for (Caption caption: result.description.captions) {
                    Log.e(LOG_TAG,"Caption: " + caption.text + ", confidence: " + caption.confidence);
                    vision_result = vision_result + " " + caption.text + " (" + caption.confidence + ").";
                }

                LogViewer.addLog("Vision API: "+vision_result);

                /*
                for (String tag: result.description.tags) {
                    mEditText.append("Tag: " + tag + "\n");
                }*/
                //doneSignal.countDown();
                detect(mBitmap);
            }
        }
    }



    // Background task of face identification.
    private class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {
        private boolean mSucceed = true;
        String mPersonGroupId;
        IdentificationTask(String personGroupId) {
            this.mPersonGroupId = personGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params) {
            String logString = "Request: Identifying faces ";
            for (UUID faceId: params) {
                logString += faceId.toString() + ", ";
            }
            logString += " in group " + mPersonGroupId;
            Log.e(LOG_TAG1,logString);

            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try{
                TrainingStatus trainingStatus = faceServiceClient.getPersonGroupTrainingStatus(
                        this.mPersonGroupId);     /* personGroupId */

                if (trainingStatus.status != TrainingStatus.Status.Succeeded) {
                    Log.e(LOG_TAG1,"Person group training status is " + trainingStatus.status);
                    mSucceed = false;
                    return null;
                }

                // Start identification.
                return faceServiceClient.identity(
                        this.mPersonGroupId,   /* personGroupId */
                        params,                  /* faceIds */
                        1);  /* maxNumOfCandidatesReturned */
            }  catch (Exception e) {
                mSucceed = false;
                Log.e(LOG_TAG1,e.getMessage());
                doneSignal.countDown();
                return null;
            }
        }

        @Override
        protected void onPostExecute(IdentifyResult[] result) {
            // Show the result on screen when detection is done.
            setUiAfterIdentification(result, mSucceed);
        }
    }




    // Show the result on screen when detection is done.
    private void setUiAfterIdentification(IdentifyResult[] result, boolean succeed) {
        if (succeed) {
            // Set the information about the detection result.
            Log.e(LOG_TAG1, "Identification is done");

            if (result != null) {
                String logString = "Response: Success. ";
                for (IdentifyResult identifyResult: result) {
                    logString += "Identified: "
                            + (identifyResult.candidates.size() > 0
                            ? StorageHelper.getPersonName(identifyResult.candidates.get(0).personId.toString(), mPersonGroupId, bingVisionService.this) + " with confidence "+identifyResult.candidates.get(0).confidence
                            : "Unknown Person")
                            + ". ";

                    if (identifyResult.candidates.size() > 0) {
                        face_result = face_result + " " + StorageHelper.getPersonName(identifyResult.candidates.get(0).personId.toString(), mPersonGroupId, bingVisionService.this) + " (" + identifyResult.candidates.get(0).confidence + ") ";
                    }
                    LogViewer.addLog("Face API: " + face_result);

                }

                //mIdentifyResults.get(position).candidates.get(0).confidence
                Log.e(LOG_TAG1,logString);
                doneSignal.countDown();
            } else {
                Log.e(LOG_TAG1,"null result in setUIAfterIdentification");
                doneSignal.countDown();
            }
        }
        Log.e(LOG_TAG1,"SUCCESS! DONE!!! face recognition");
        doneSignal.countDown();
    }

    // Background task of face detection.
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try{
                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            }  catch (Exception e) {
                Log.e(LOG_TAG1,"error in background in DetectionTask");
                doneSignal.countDown();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Face[] result) {
            if (result != null) {
                // Set the adapter of the ListView which contains the details of detected faces.
                faces = new ArrayList<>();
                faces = Arrays.asList(result);
                if (result.length == 0) {
                    detected = false;
                    Log.e(LOG_TAG1,"No faces detected!");
                    LogViewer.addLog("Face API: No faces detected");
                    doneSignal.countDown();
                } else {
                    detected = true;
                    identify();
                }
            } else {
                detected = false;
                Log.e(LOG_TAG1,"no result postexecute detection task");
                doneSignal.countDown();
            }
        }
    }

    // Start detecting in image.
    private void detect(Bitmap bitmap) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        // Start a background task to detect faces in the image.
        new DetectionTask().execute(inputStream);
    }

    // Called when the "Detect" button is clicked.
    public void identify() {
        // Start detection task only if the image to detect is selected.
        if (detected && mPersonGroupId != null) {
            // Start a background task to identify faces in the image.
            List<UUID> faceIds = new ArrayList<>();
            for (Face face: faces) {
                faceIds.add(face.faceId);
            }

            new IdentificationTask(mPersonGroupId).execute(
                    faceIds.toArray(new UUID[faceIds.size()]));
        } else {
            // Not detected or person group exists.
            Log.e(LOG_TAG1,"Please select an image and create a person group first.");
            doneSignal.countDown();
        }
    }
}

