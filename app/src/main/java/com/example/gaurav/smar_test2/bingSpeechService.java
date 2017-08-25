package com.example.gaurav.smar_test2;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by gaurav on 30/1/17.
 */
public class bingSpeechService extends IntentService implements ISpeechRecognitionServerEvents{
    String SERVICE_TAG = "service_tag";
    DataRecognitionClient dataClient = null;
    FinalResponseStatus isReceivedResponse = FinalResponseStatus.NotReceived;
    int m_waitSeconds = 60;
    int TIME_CHECKER_WAIT = 5000; // (in ms)
    int status = 0;
    volatile long bingWait = 0;
    Thread time_checker_thread = null;
    private CountDownLatch doneSignal = null;
    private String final_speech_text = null;


    public enum FinalResponseStatus { NotReceived, OK, Timeout }

    public bingSpeechService() {
        super("bingSpeechService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        status = 0;
        doneSignal = new CountDownLatch(1);
        bingWait = System.currentTimeMillis();
        final_speech_text = "";
        String time_stamp = intent.getStringExtra("time_stamp");
        String filename = getAudioFileName(time_stamp);
        Log.e(SERVICE_TAG, "bingSpeech for " + filename);
        if (null == this.dataClient) {
            this.dataClient = SpeechRecognitionServiceFactory.createDataClient(
                    this.getMode(),
                    this.getDefaultLocale(),
                    this,
                    this.getPrimaryKey());

        }
        time_checker_thread = new Thread(runnable);
        time_checker_thread.start();
        this.SendAudioHelper(filename);
        try {
            Log.e(SERVICE_TAG,"waiting for signal");
            doneSignal.await();
        } catch (InterruptedException e) {
            Log.e(SERVICE_TAG,"error in doneSignal");
            LogViewer.addLog("Speech API: Error in doneSignal");
            e.printStackTrace();
        }
        time_checker_thread.interrupt();
        deleteAudioFile(filename);
        Log.e(SERVICE_TAG, "FINAL " + final_speech_text);

        LogViewer.addLog("Speech API: "+final_speech_text);

        DBHelper mDBHelper = DBHelper.getInstance(this);
        mDBHelper.saveSpeech(time_stamp,final_speech_text);

        Log.e(SERVICE_TAG, "done!");
    }

    public String getAudioFileName(String time_stamp) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path +="/Smar/Audio/"+time_stamp+".wav";
        return path;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e(SERVICE_TAG,"time_checker_thread started");
            boolean end = false;
            while(!Thread.currentThread().isInterrupted() && !end) {
                if(status == 1) {
                    long current  = System.currentTimeMillis();
                    long diff = current - bingWait;
                    if (diff > TIME_CHECKER_WAIT) {
                        Log.e(SERVICE_TAG,"time_checker_thread detected end");
                        end = true;
                        doneSignal.countDown();
                    }
                }
            }
        }
    };

    private void deleteAudioFile(String filename) {
        File file = new File(filename);
        file.delete();
    }

    public String getPrimaryKey() {
        return this.getString(R.string.speech_key);
    }

    private SpeechRecognitionMode getMode() {
        return SpeechRecognitionMode.LongDictation;
    }

    private String getDefaultLocale() {
        return "en-us";
    }

    private void SendAudioHelper(String filename) {
        RecognitionTask doDataReco = new RecognitionTask(this.dataClient, this.getMode(), filename);
        try
        {
            doDataReco.execute().get(m_waitSeconds, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            Log.e(SERVICE_TAG,"Error in SendAudioHelper");
            LogViewer.addLog("Speech API: Error in sendAudioHelper");
            doDataReco.cancel(true);
            isReceivedResponse = FinalResponseStatus.Timeout;
        }
    }

    public void onFinalResponseReceived(final RecognitionResult response) {
        status = 1;
        boolean isFinalDicationMessage = this.getMode() == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);

        if (isFinalDicationMessage) {
            this.isReceivedResponse = FinalResponseStatus.OK;
        }

        if (!isFinalDicationMessage) {
            Log.e(SERVICE_TAG,"********* Final n-BEST Results *********");
            for (int i = 0; i < response.Results.length; i++) {
                Log.e(SERVICE_TAG,"[" + i + "]" + " Confidence=" + response.Results[i].Confidence +
                        " Text=\"" + response.Results[i].DisplayText + "\"");
                final_speech_text = final_speech_text + " " + response.Results[i].DisplayText;
            }
        }
    }

    /**
     * Called when a final response is received and its intent is parsed
     */
    public void onIntentReceived(final String payload) {
        Log.e(SERVICE_TAG,"Speech Process completed yeh!");
    }

    public void onPartialResponseReceived(final String response) {
        status = 0;
        bingWait = System.currentTimeMillis();
        Log.e(SERVICE_TAG,"PARTIAL RESULT: "+response);
    }

    public void onError(final int errorCode, final String response) {
        Log.e(SERVICE_TAG,"Error " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        Log.e(SERVICE_TAG,response);
        status = 1;

        LogViewer.addLog("Speech API: ERROR "+SpeechClientStatus.fromInt(errorCode));
    }

    /**
     * Called when the microphone status has changed.
     * @param recording The current recording state
     */
    public void onAudioEvent(boolean recording) {

    }

    private void resetDataClient() {
        // Reset everything
        if (this.dataClient != null) {
            try {
                this.dataClient.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.dataClient = null;
        }
    }

    /*
     * Speech recognition with data (for example from a file or audio source).
     * The data is broken up into buffers and each buffer is sent to the Speech Recognition Service.
     * No modification is done to the buffers, so the user can apply their
     * own VAD (Voice Activation Detection) or Silence Detection
     *
     * @param dataClient
     * @param recoMode
     * @param filename
     */
    private class RecognitionTask extends AsyncTask<Void, Void, Void> {
        DataRecognitionClient dataClient;
        SpeechRecognitionMode recoMode;
        String filename;

        RecognitionTask(DataRecognitionClient dataClient, SpeechRecognitionMode recoMode, String filename) {
            this.dataClient = dataClient;
            this.recoMode = recoMode;
            this.filename = filename;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.e(SERVICE_TAG,"Background started");
            try {
                // Note for wave files, we can just send data from the file right to the server.
                // In the case you are not an audio file in wave format, and instead you have just
                // raw data (for example audio coming over bluetooth), then before sending up any
                // audio data, you must first send up an SpeechAudioFormat descriptor to describe
                // the layout and format of your raw audio data via DataRecognitionClient's sendAudioFormat() method.
                // String filename = recoMode == SpeechRecognitionMode.ShortPhrase ? "whatstheweatherlike.wav" : "batman.wav";
                //InputStream fileStream = getAssets().open("whatstheweatherlike.wav");
                //File file = new File("/storage/emulated/0/Smar/Audio/gaurav.wav");
                File file = new File(filename);
                InputStream fileStream = new FileInputStream(file);
                int bytesRead = 0;
                byte[] buffer = new byte[1024];

                do {
                    // Get  Audio data to send into byte buffer.
                    bytesRead = fileStream.read(buffer);

                    if (bytesRead > -1) {
                        // Send of audio data to service.
                        dataClient.sendAudio(buffer, bytesRead);
                    }
                } while (bytesRead > 0);

            } catch (Throwable throwable) {
                status = 1;
                Log.e(SERVICE_TAG,"Error in doInBackground");
                LogViewer.addLog("Speech API: Error in doInBackground");
                throwable.printStackTrace();
            }
            finally {
                Log.e(SERVICE_TAG,"completed upload");
                LogViewer.addLog("Speech API: upload complete");
                dataClient.endAudio();
            }

            return null;
        }
    }

}
