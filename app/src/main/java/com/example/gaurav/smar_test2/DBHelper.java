package com.example.gaurav.smar_test2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Filter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by gaurav on 3/2/17.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "memento.db";
    private static final String SQL_CREATE_TABLE_SPEECH = "CREATE TABLE IF NOT EXISTS memento_speech (_id INTEGER PRIMARY KEY AUTOINCREMENT, ts TIMESTAMP, text VARCHAR(500))";
    private static final String SQL_CREATE_TABLE_VISION = "CREATE TABLE IF NOT EXISTS memento_vision (_id INTEGER PRIMARY KEY AUTOINCREMENT, ts TIMESTAMP, text VARCHAR(500))";
    private static final String SQL_CREATE_TABLE_FACE = "CREATE TABLE IF NOT EXISTS memento_face (_id INTEGER PRIMARY KEY AUTOINCREMENT, ts TIMESTAMP, text VARCHAR(500))";

    private static final String SQL_DELETE_SPEECH = "DROP TABLE IF EXISTS memento_speech";
    private static final String SQL_DELETE_VISION = "DROP TABLE IF EXISTS memento_vision";
    private static final String SQL_DELETE_FACE = "DROP TABLE IF EXISTS memento_face";

    private static DBHelper sInstance;
    private String LOG_TAG = "dbhelper";

    public static synchronized DBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void saveSpeech(String time_stamp, String text) {
        Log.e(LOG_TAG, "saveSpeech");
        if (text == "") {
            text = "No Speech";
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("ts", time_stamp);
            contentValues.put("text", text.trim().toLowerCase());
            db.insertOrThrow("memento_speech", null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in saveSpeech");
        } finally {
            db.endTransaction();
        }
    }

    public void saveVision(String time_stamp, String text) {
        Log.e(LOG_TAG,"saveVision");
        if (text == "") {
            text = "No vision description";
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("ts", time_stamp);
            contentValues.put("text", text.trim().toLowerCase());
            db.insertOrThrow("memento_vision", null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in saveVision");
        } finally {
            db.endTransaction();
        }

    }

    public void saveFace(String time_stamp, String text) {
        Log.e(LOG_TAG,"saveFace");
        if (text == "") {
            text = "No faces detected";
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("ts", time_stamp);
            contentValues.put("text", text.trim().toLowerCase());
            db.insertOrThrow("memento_face", null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in saveFace");
        } finally {
            db.endTransaction();
        }
    }

    public List<SearchResult> search(String query) {
        if(!query.equals("")) {
            SQLiteDatabase db = this.getReadableDatabase();
            db.beginTransaction();
            List<SearchResult> result = new ArrayList<>();
            try {
                String ignore_word_string = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
                List<String> ignore_words = Arrays.asList(ignore_word_string.split(","));
                query = query.replaceAll("[^a-zA-Z]", " ").toLowerCase();
                String[] splited = query.trim().split("\\s+");

                List<String> selected = new ArrayList<String>();
                for(String word:splited) {
                    String lowercase = word.toLowerCase();
                    if(!ignore_words.contains(lowercase)) {
                        selected.add(lowercase);
                    }
                }

                String query_string = "(";
                for(String word: selected) {
                    query_string += " s.text LIKE '%" + word + "%' OR v.text LIKE '%" + word + "%' OR";
                }
                query_string = query_string.substring(0,query_string.length()-2);
                query_string +=")";

                Log.e(LOG_TAG,"QUERY STRING: "+query_string);

                Cursor cursor = db.rawQuery("SELECT s._id,s.ts,s.text,v.text,f.text from memento_speech as s, memento_vision as v, memento_face as f WHERE "+query_string+" AND v.ts=s.ts AND f.ts=s.ts ORDER BY s.ts ASC", null);
                //Log.e(LOG_TAG, "SEARCH");
                while (cursor.moveToNext()) {
                    SearchResult item = new SearchResult();
                    item._id = cursor.getLong(0);
                    item.ts = cursor.getString(1);
                    item.speech_text = cursor.getString(2);
                    item.vision_text = cursor.getString(3);
                    item.face_text = cursor.getString(4);
                    result.add(item);
                    //Log.e(LOG_TAG, cursor.getString(0) + " / " + cursor.getString(1) + " / " + cursor.getString(2) + " / " + cursor.getString(3));
                }
                cursor.close();
                db.setTransactionSuccessful();

                Log.e(LOG_TAG, "search !!!!");
                for (SearchResult i : result) {
                    Log.e(LOG_TAG,i.ts +" / " + i.speech_text +" / " +i.vision_text+" / " +i.face_text);
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in search");
            } finally {
                db.endTransaction();
            }
            return result;
        }
        return null;
    }

    public List<SearchResultItem> getItemDetail(String time_stamp) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        List<SearchResultItem> result = new ArrayList<>();
        try {
            Cursor cursor = db.rawQuery("SELECT ts,text FROM memento_speech WHERE abs((julianday(ts)-julianday('" + time_stamp + "'))*86400) < 300 ORDER BY ts ASC ",null);
            Log.e(LOG_TAG, "SEARCH");
            while (cursor.moveToNext()) {
                SearchResultItem item = new SearchResultItem();
                item.type = "speech";
                item.ts = cursor.getString(0);
                item.text = cursor.getString(1);
                result.add(item);
                Log.e(LOG_TAG,"speech / "+ cursor.getString(0) + " / " + cursor.getString(1));
            }
            cursor = db.rawQuery("SELECT ts,text FROM memento_vision WHERE abs((julianday(ts)-julianday('" + time_stamp + "'))*86400) < 300 ORDER BY ts ASC ",null);
            while (cursor.moveToNext()) {
                SearchResultItem item = new SearchResultItem();
                item.type = "vision";
                item.ts = cursor.getString(0);
                item.text = cursor.getString(1);
                result.add(item);
                Log.e(LOG_TAG,"vision / "+ cursor.getString(0) + " / " + cursor.getString(1));
            }
            cursor = db.rawQuery("SELECT ts,text FROM memento_face WHERE abs((julianday(ts)-julianday('" + time_stamp + "'))*86400) < 300 ORDER BY ts ASC ",null);
            while (cursor.moveToNext()) {
                SearchResultItem item = new SearchResultItem();
                item.type = "face";
                item.ts = cursor.getString(0);
                item.text = cursor.getString(1);
                result.add(item);
                Log.e(LOG_TAG,"face / "+ cursor.getString(0) + " / " + cursor.getString(1));
            }
            cursor.close();
            db.setTransactionSuccessful();

            Collections.sort(result, new Comparator<SearchResultItem>() {
                @Override
                public int compare(SearchResultItem o1, SearchResultItem o2) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date1 = new Date();
                    Date date2 = new Date();
                    try {
                        date1 = simpleDateFormat.parse(o1.ts);
                        date2 = simpleDateFormat.parse(o2.ts);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return date1.compareTo(date2);
                }
            });
            Log.e(LOG_TAG,"getItemDetail !!!!");
            for (SearchResultItem i : result) {
                Log.e(LOG_TAG,i.ts +" / " +  i.type + " / " + i.text);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in getItemDetail");
        } finally {
            db.endTransaction();
        }
        return result;

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(LOG_TAG, "onCreate database");
        db.execSQL(SQL_CREATE_TABLE_SPEECH);
        db.execSQL(SQL_CREATE_TABLE_VISION);
        db.execSQL(SQL_CREATE_TABLE_FACE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_SPEECH);
        db.execSQL(SQL_DELETE_VISION);
        db.execSQL(SQL_DELETE_FACE);
        onCreate(db);
    }
}
