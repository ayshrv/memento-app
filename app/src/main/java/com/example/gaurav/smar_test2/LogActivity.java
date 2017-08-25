package com.example.gaurav.smar_test2;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

/**
 * Created by gaurav on 4/2/17.
 */
public class LogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        final LogAdapter logAdapter = new LogAdapter();
        ListView listView = (ListView) findViewById(R.id.log);
        listView.setAdapter(logAdapter);

        Switch start_switch = (Switch) findViewById(R.id.start_switch);
        start_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainService.APP_START = 1;
                } else {
                    MainService.APP_START = 0;
                }
            }
        });

        if (MainService.APP_START==1) {
            start_switch.setChecked(true);
        } else {
            start_switch.setChecked(false);
        }


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                logAdapter.notifyDataSetChanged();
                handler.postDelayed(this, 1000);
            }
        }, 1000);

    }

    // The adapter of the ListView which contains the identification log.
    private class LogAdapter extends BaseAdapter {
        // The identification log.
        List<String> log;

        LogAdapter() {
            log = LogViewer.getLog();
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return log.size();
        }

        @Override
        public Object getItem(int position) {
            return log.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_log, parent, false);
            }
            convertView.setId(position);

            ((TextView)convertView.findViewById(R.id.log)).setText(log.get(position));

            return convertView;
        }
    }
}
