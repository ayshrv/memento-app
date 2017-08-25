package com.example.gaurav.smar_test2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by gaurav on 4/2/17.
 */
public class Setting extends AppCompatActivity{
    EditText threshold_sound_level = null;
    EditText record_duration = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        threshold_sound_level = (EditText) findViewById(R.id.threshold_sound_level);
        record_duration = (EditText) findViewById(R.id.record_duration);
        threshold_sound_level.setText(Integer.toString(MainService.THRESHOLD_SOUND_LEVEL));
        record_duration.setText(Integer.toString(MainService.RECORD_DURATION));
    }

    public void doneChanges(View v) {
        MainService.THRESHOLD_SOUND_LEVEL = Integer.parseInt(threshold_sound_level.getText().toString());
        MainService.RECORD_DURATION = Integer.parseInt(record_duration.getText().toString());
        finish();
    }
}
