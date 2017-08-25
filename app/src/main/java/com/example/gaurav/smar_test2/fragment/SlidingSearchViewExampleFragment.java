package com.example.gaurav.smar_test2.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.example.gaurav.smar_test2.DBHelper;
import com.example.gaurav.smar_test2.LogActivity;
import com.example.gaurav.smar_test2.LogViewer;
import com.example.gaurav.smar_test2.MainActivity;
import com.example.gaurav.smar_test2.MainService;
import com.example.gaurav.smar_test2.R;
import com.example.gaurav.smar_test2.SearchDetailActivity;
import com.example.gaurav.smar_test2.SearchResult;
import com.example.gaurav.smar_test2.Setting;
import com.example.gaurav.smar_test2.adapter.SearchResultsListAdapter;
import com.example.gaurav.smar_test2.helper.StorageHelper;
import com.example.gaurav.smar_test2.persongroupmanagement.PersonGroupActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class SlidingSearchViewExampleFragment extends BaseExampleFragment {
    private final String TAG = "BlankFragment";

    public static final long FIND_SUGGESTION_SIMULATED_DELAY = 250;

    private static final long ANIM_DURATION = 350;

    private TextView mHeaderView;
    private View mDimSearchViewBackground;
    private ColorDrawable mDimDrawable;
    private FloatingSearchView mSearchView;

    private boolean isFocus = false;

    private RecyclerView mSearchResultsList;
    private SearchResultsListAdapter mSearchResultsAdapter;

    private boolean mIsDarkSearchTheme = false;

    private String mLastQuery = "";

    private String smar_group_id = null;

    public SlidingSearchViewExampleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sliding_search_example, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSearchView = (FloatingSearchView) view.findViewById(R.id.floating_search_view);
        mHeaderView = (TextView)view.findViewById(R.id.header_view);

        mSearchResultsList = (RecyclerView) view.findViewById(R.id.search_results_list);

        mDimSearchViewBackground = view.findViewById(R.id.dim_background);
        mDimDrawable = new ColorDrawable(Color.BLACK);
        mDimDrawable.setAlpha(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mDimSearchViewBackground.setBackground(mDimDrawable);
        }else {
            mDimSearchViewBackground.setBackgroundDrawable(mDimDrawable);
        }
        smar_group_id = checkSmarGroup();
        isFocus = false;
        setupFloatingSearch();
        setupResultsList();
        setupDrawer();
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/league.ttf");
        mHeaderView.setTypeface(font);
    }


    private class getAsyncSearchResult extends AsyncTask<String,Void,List<SearchResult>> {

        @Override
        protected List<SearchResult> doInBackground(String... params) {
            Log.e("UI","running backgroung in getAsyncSearchResult");
            String query = params[0];
            DBHelper mDBHelper = DBHelper.getInstance(getActivity());
            List<SearchResult> results = mDBHelper.search(query);
            return results;
        }

        @Override
        protected void onPostExecute(List<SearchResult> searchResults) {
            for (SearchResult i : searchResults) {
                Log.e("UI",i.ts +" / " + i.speech_text +" / " +i.vision_text+" / " +i.face_text);
            }
            mSearchResultsAdapter.swapData(searchResults);
        }
    }


    private void setupFloatingSearch() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    isFocus = false;
                }
                /*
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {

                    //this shows the top left circular progress
                    //you can call it where ever you want, but
                    //it makes sense to do it when loading something in
                    //the background.
                    mSearchView.showProgress();

                    //simulates a query call to a data source
                    //with a new query.
                    DataHelper.findSuggestions(getActivity(), newQuery, 5,
                            FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {

                                @Override
                                public void onResults(List<ColorSuggestion> results) {

                                    //this will swap the data and
                                    //render the collapse/expand animations as necessary
                                    mSearchView.swapSuggestions(results);

                                    //let the users know that the background
                                    //process has completed
                                    mSearchView.hideProgress();
                                }
                            });
                }*/

                Log.d(TAG, "onSearchTextChanged()");
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

                mLastQuery = searchSuggestion.getBody();
            }

            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;
                if (query.trim()!="") {
                    isFocus = true;
                }
                mSearchResultsAdapter.swapData(new ArrayList<SearchResult>());

                if(query.equals("") || query == null) {}
                else {
                    new getAsyncSearchResult().execute(query);
                }

                Log.d(TAG, "onSearchAction()");
            }
        });


        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                if (isFocus == false) {
                    int headerHeight = getResources().getDimensionPixelOffset(R.dimen.sliding_search_view_header_height);
                    ObjectAnimator anim = ObjectAnimator.ofFloat(mSearchView, "translationY",
                            headerHeight, 0);
                    anim.setDuration(350);


                    anim.addListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //show suggestions when search bar gains focus (typically history suggestions)
                            //mSearchView.swapSuggestions(DataHelper.getHistory(getActivity(), 3));

                        }
                    });
                    anim.start();
                }
                fadeDimBackground(0, 150, null);
                isFocus = true;
                //mSearchView.setSearchText(mLastQuery);
                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {
                /*
                int headerHeight = getResources().getDimensionPixelOffset(R.dimen.sliding_search_view_header_height);
                ObjectAnimator anim = ObjectAnimator.ofFloat(mSearchView, "translationY",
                        0, headerHeight);
                anim.setDuration(350);
                anim.start();*/
                fadeDimBackground(150, 0, null);

                //set the title of the bar so that when focus is returned a new query begins

                //mSearchView.setSearchText(mLastQuery);

                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                //mSearchView.setSearchText(searchSuggestion.getBody());

                Log.d(TAG, "onFocusCleared()");
            }
        });


        //handle menu clicks the same way as you would
        //in a regular activity
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                /*
                if (item.getItemId() == R.id.action_change_colors) {

                    mIsDarkSearchTheme = true;

                    //demonstrate setting colors for items
                    mSearchView.setBackgroundColor(Color.parseColor("#787878"));
                    mSearchView.setViewTextColor(Color.parseColor("#e9e9e9"));
                    mSearchView.setHintTextColor(Color.parseColor("#e9e9e9"));
                    mSearchView.setActionMenuOverflowColor(Color.parseColor("#e9e9e9"));
                    mSearchView.setMenuItemIconColor(Color.parseColor("#e9e9e9"));
                    mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
                    mSearchView.setClearBtnColor(Color.parseColor("#e9e9e9"));
                    mSearchView.setDividerColor(Color.parseColor("#BEBEBE"));
                    mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
                } else {

                    //just print action
                    Toast.makeText(getActivity().getApplicationContext(), item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                }*/
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                int id = item.getItemId();

                //noinspection SimplifiableIfStatement
                if (id == R.id.action_settings) {
                    Intent intent = new Intent(getActivity(),Setting.class);
                    startActivity(intent);
                } else if (id == R.id.action_log) {
                    Intent intent = new Intent(getActivity(),LogActivity.class);
                    startActivity(intent);
                } else if (id == R.id.action_start_service) {
                    if (smar_group_id != null) {
                        Intent intent = new Intent(getActivity(), MainService.class);
                        intent.putExtra("smar_group_id", smar_group_id);
                        getActivity().startService(intent);
                    } else {
                        Log.e("face","can not start main service. no group id");
                        LogViewer.addLog("MainActivity: can not start main service. no group id");
                    }
                } else if (id == R.id.action_stop_service) {
                    Intent intent = new Intent(getActivity(), MainService.class);
                    getActivity().stopService(intent);
                    DBHelper mDBHelper = DBHelper.getInstance(getActivity());
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

            }
        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHome"
        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {

                Log.d(TAG, "onHomeClicked()");
            }
        });

        /*
         * Here you have access to the left icon and the text of a given suggestion
         * item after as it is bound to the suggestion list. You can utilize this
         * callback to change some properties of the left icon and the text. For example, you
         * can load the left icon images using your favorite image loading library, or change text color.
         *
         *
         * Important:
         * Keep in mind that the suggestion list is a RecyclerView, so views are reused for different
         * items in the list.
         */
        /*
        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
                ColorSuggestion colorSuggestion = (ColorSuggestion) item;

                String textColor = mIsDarkSearchTheme ? "#ffffff" : "#000000";
                String textLight = mIsDarkSearchTheme ? "#bfbfbf" : "#787878";

                if (colorSuggestion.getIsHistory()) {
                    leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_history_black_24dp, null));

                    Util.setIconColor(leftIcon, Color.parseColor(textColor));
                    leftIcon.setAlpha(.36f);
                } else {
                    leftIcon.setAlpha(0.0f);
                    leftIcon.setImageDrawable(null);
                }

                textView.setTextColor(Color.parseColor(textColor));
                String text = colorSuggestion.getBody()
                        .replaceFirst(mSearchView.getQuery(),
                                "<font color=\"" + textLight + "\">" + mSearchView.getQuery() + "</font>");
                textView.setText(Html.fromHtml(text));
            }

        });*/
    }

    private void setupResultsList() {
        mSearchResultsAdapter = new SearchResultsListAdapter();
        mSearchResultsAdapter.setItemsOnClickListener(new SearchResultsListAdapter.OnItemClickListener(){
            @Override
            public void onClick(SearchResult r) {
                Intent intent = new Intent(getActivity(), SearchDetailActivity.class);
                intent.putExtra("ts",r.ts);
                startActivity(intent);
                Log.e("UI",r.ts);
            }
        });
        mSearchResultsList.setAdapter(mSearchResultsAdapter);
        mSearchResultsList.setLayoutManager(new LinearLayoutManager(getContext()));

    };


    @Override
    public boolean onActivityBackPress() {
        //if mSearchView.setSearchFocused(false) causes the focused search
        //to close, then we don't want to close the activity. if mSearchView.setSearchFocused(false)
        //returns false, we know that the search was already closed so the call didn't change the focus
        //state and it makes sense to call supper onBackPressed() and close the activity
        if (isFocus) {
            mSearchResultsAdapter.swapData(new ArrayList<SearchResult>());
            int headerHeight = getResources().getDimensionPixelOffset(R.dimen.sliding_search_view_header_height);
            ObjectAnimator anim = ObjectAnimator.ofFloat(mSearchView, "translationY",
                    0, headerHeight);
            anim.setDuration(350);
            anim.start();
            isFocus = false;
            //mSearchView.setSearchText("");
            mSearchView.setSearchFocused(false);
            mLastQuery = "";
            return true;
        }
        return false;
    }

    private void setupDrawer() {
        attachSearchViewActivityDrawer(mSearchView);
    }

    private void fadeDimBackground(int from, int to, Animator.AnimatorListener listener) {
        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (Integer) animation.getAnimatedValue();
                mDimDrawable.setAlpha(value);
            }
        });
        if(listener != null) {
            anim.addListener(listener);
        }
        anim.setDuration(ANIM_DURATION);
        anim.start();
    }









    public String checkSmarGroup() {
        Log.e("face","checkSmarGroup called");
        String smar_group_id = null;
        List<String> personGroupIdList = new ArrayList<>();
        Set<String> personGroupIds = StorageHelper.getAllPersonGroupIds(getActivity());
        int size_group = personGroupIds.size();
        for (String personGroupId: personGroupIds) {
            personGroupIdList.add(personGroupId);
        }
        if(size_group == 1) {
            String personGroupName =StorageHelper.getPersonGroupName(
                    personGroupIdList.get(0), getActivity());
            Log.e("face",personGroupName);
            Log.e("face","size 1");
            if (personGroupName.equals("smar")) {
                smar_group_id = personGroupIdList.get(0);
                Log.e("face","updated smar id. Yeh!!!");
            } else {
                StorageHelper.deletePersonGroups(personGroupIdList,getActivity());
            }
        } else if (size_group == 0) {
            Log.e("face","no previous group found");
        } else {
            StorageHelper.deletePersonGroups(personGroupIdList,getActivity());
        }
        return smar_group_id;
    }

    public void loadPersonGroup(String smar_group_id) {
        Log.e("face","loadPersonGroup called");
        Intent intent = new Intent(getActivity(), PersonGroupActivity.class);
        intent.putExtra("AddNewPersonGroup", false);
        intent.putExtra("PersonGroupName", "smar");
        intent.putExtra("PersonGroupId", smar_group_id);
        startActivity(intent);
    }

    public void createPersonGroup() {
        Log.e("face","createPersonGroup called");
        String personGroupId = UUID.randomUUID().toString();
        Intent intent = new Intent(getActivity(), PersonGroupActivity.class);
        intent.putExtra("AddNewPersonGroup", true);
        intent.putExtra("PersonGroupName", "smar");
        intent.putExtra("PersonGroupId", personGroupId);
        startActivity(intent);
    }

}
