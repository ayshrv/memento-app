package com.example.gaurav.smar_test2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.gaurav.smar_test2.adapter.SearchDetailListAdapter;
import com.example.gaurav.smar_test2.adapter.SearchResultsListAdapter;

import java.util.List;

/**
 * Created by gaurav on 5/2/17.
 */
public class SearchDetailActivity extends AppCompatActivity {
    private RecyclerView mSearchDetailList;
    private SearchDetailListAdapter mSearchDetailAdapter;

    private class getAsyncSearchResult extends AsyncTask<String,Void,List<SearchResultItem>> {

        @Override
        protected List<SearchResultItem> doInBackground(String... params) {
            Log.e("UI","running backgroung in getAsyncSearchResult");
            String query = params[0];
            DBHelper mDBHelper = DBHelper.getInstance(SearchDetailActivity.this);
            List<SearchResultItem> results = mDBHelper.getItemDetail(query);
            return results;
        }

        @Override
        protected void onPostExecute(List<SearchResultItem> searchResults) {
            for (SearchResultItem i : searchResults) {
                Log.e("UI",i.ts +" / " + i.type +" / " +i.text);
            }
            mSearchDetailAdapter.swapData(searchResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_detail);
        mSearchDetailList = (RecyclerView) findViewById(R.id.search_detail_list);
        String ts = getIntent().getStringExtra("ts");
        Log.e("search_detail","get time stamp "+ts);
        setupDetailList();

        new getAsyncSearchResult().execute(ts);
    }

    private void setupDetailList() {
        mSearchDetailAdapter = new SearchDetailListAdapter();
        mSearchDetailList.setAdapter(mSearchDetailAdapter);
        mSearchDetailList.setLayoutManager(new LinearLayoutManager(this));
    };
}
