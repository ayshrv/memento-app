package com.example.gaurav.smar_test2;
/*
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
*/

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.example.gaurav.smar_test2.fragment.BaseExampleFragment;
import com.example.gaurav.smar_test2.fragment.SlidingSearchViewExampleFragment;
import com.example.gaurav.smar_test2.helper.StorageHelper;
import com.example.gaurav.smar_test2.persongroupmanagement.PersonGroupActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements BaseExampleFragment.BaseExampleFragmentCallbacks{

    private static final String LOG_TAG = "AudioRecordTest";
    private static final String LOG_TAG1 = "AudioRecordTest1";
    //private String smar_group_id = null;
    private DrawerLayout mDrawerLayout;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */
        showFragment(new SlidingSearchViewExampleFragment());
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,Setting.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_log) {
            Intent intent = new Intent(this,LogActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_start_service) {
            if (smar_group_id != null) {
                Intent intent = new Intent(this, MainService.class);
                intent.putExtra("smar_group_id",smar_group_id);
                startService(intent);
            } else {
                Log.e("face","can not start main service. no group id");
                LogViewer.addLog("MainActivity: can not start main service. no group id");
            }
        } else if (id == R.id.action_stop_service) {
            Intent intent = new Intent(this, MainService.class);
            stopService(intent);
            DBHelper mDBHelper = DBHelper.getInstance(this);
            mDBHelper.close();
        } else if (id == R.id.action_add_people) {
            if (smar_group_id == null) {
                Log.e("face","creating NEW!");
                createPersonGroup();
            } else {
                Log.e("face","found OLD!!");
                loadPersonGroup(smar_group_id);
            }
        }

        return super.onOptionsItemSelected(item);
    }
    */
    /*
    public void onStartService(View v) {
        if (smar_group_id != null) {
            Intent intent = new Intent(this, MainService.class);
            intent.putExtra("smar_group_id",smar_group_id);
            startService(intent);
        } else {
            Log.e("face","can not start main service. no group id");
        }
    }

    public void onDestroyService(View v) {
        Intent intent = new Intent(this, MainService.class);
        stopService(intent);
        DBHelper mDBHelper = DBHelper.getInstance(this);
        mDBHelper.close();
    }

    public void onAddPeople(View v) {
        Log.e("face","onAddPeople called");
        if (smar_group_id == null) {
            Log.e("face","creating NEW!");
            createPersonGroup();
        } else {
            Log.e("face","found OLD!!");
            loadPersonGroup(smar_group_id);
        }
    }*/
    /*
    public String checkSmarGroup() {
        Log.e("face","checkSmarGroup called");
        String smar_group_id = null;
        List<String> personGroupIdList = new ArrayList<>();
        Set<String> personGroupIds = StorageHelper.getAllPersonGroupIds(MainActivity.this);
        int size_group = personGroupIds.size();
        for (String personGroupId: personGroupIds) {
            personGroupIdList.add(personGroupId);
        }
        if(size_group == 1) {
            String personGroupName =StorageHelper.getPersonGroupName(
                    personGroupIdList.get(0), MainActivity.this);
            Log.e("face",personGroupName);
            Log.e("face","size 1");
            if (personGroupName.equals("smar")) {
                smar_group_id = personGroupIdList.get(0);
                Log.e("face","updated smar id. Yeh!!!");
            } else {
                StorageHelper.deletePersonGroups(personGroupIdList,MainActivity.this);
            }
        } else if (size_group == 0) {
            Log.e("face","no previous group found");
        } else {
            StorageHelper.deletePersonGroups(personGroupIdList,MainActivity.this);
        }
        return smar_group_id;
    }

    public void loadPersonGroup(String smar_group_id) {
        Log.e("face","loadPersonGroup called");
        Intent intent = new Intent(MainActivity.this, PersonGroupActivity.class);
        intent.putExtra("AddNewPersonGroup", false);
        intent.putExtra("PersonGroupName", "smar");
        intent.putExtra("PersonGroupId", smar_group_id);
        startActivity(intent);
    }

    public void createPersonGroup() {
        Log.e("face","createPersonGroup called");
        String personGroupId = UUID.randomUUID().toString();
        Intent intent = new Intent(MainActivity.this, PersonGroupActivity.class);
        intent.putExtra("AddNewPersonGroup", true);
        intent.putExtra("PersonGroupName", "smar");
        intent.putExtra("PersonGroupId", personGroupId);
        startActivity(intent);
    }
    */



    @Override
    public void onAttachSearchViewToDrawer(FloatingSearchView searchView) {
        searchView.attachNavigationDrawerToMenuButton(mDrawerLayout);
    }

    @Override
    public void onBackPressed() {
        List fragments = getSupportFragmentManager().getFragments();
        BaseExampleFragment currentFragment = (BaseExampleFragment) fragments.get(fragments.size() - 1);

        if (!currentFragment.onActivityBackPress()) {
            super.onBackPressed();
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }
}
