package com.example.gaurav.smar_test2.adapter;

/**
 * Copyright (C) 2015 Ari C.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.arlib.floatingsearchview.util.Util;
import com.example.gaurav.smar_test2.R;
import com.example.gaurav.smar_test2.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsListAdapter extends RecyclerView.Adapter<SearchResultsListAdapter.ViewHolder> {

    private List<SearchResult> mDataSet = new ArrayList<>();

    private int mLastAnimatedItemPosition = -1;

    public interface OnItemClickListener{
        void onClick(SearchResult r);
    }

    private OnItemClickListener mItemsOnClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTimeStamp;
        public final TextView mSpeechText;
        public final TextView mVisionText;
        public final TextView mFaceText;

        public ViewHolder(View view) {
            super(view);
            mTimeStamp = (TextView) view.findViewById(R.id.time_stamp);
            mSpeechText= (TextView) view.findViewById(R.id.speech_text);
            mVisionText = (TextView) view.findViewById(R.id.vision_text);
            mFaceText = (TextView) view.findViewById(R.id.face_text);
        }
    }

    public void swapData(List<SearchResult> mNewDataSet) {
        mDataSet = mNewDataSet;
        notifyDataSetChanged();
    }

    public void setItemsOnClickListener(OnItemClickListener onClickListener){
        this.mItemsOnClickListener = onClickListener;
    }

    @Override
    public SearchResultsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_results_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchResultsListAdapter.ViewHolder holder, final int position) {

        SearchResult r = mDataSet.get(position);
        holder.mTimeStamp.setText(r.ts);
        holder.mFaceText.setText(r.face_text);
        holder.mVisionText.setText(r.vision_text);
        holder.mSpeechText.setText(r.speech_text);

//        int color = Color.parseColor(colorSuggestion.getHex());
//        holder.mColorName.setTextColor(color);
//        holder.mColorValue.setTextColor(color);

        if(mLastAnimatedItemPosition < position){
            animateItem(holder.itemView);
            mLastAnimatedItemPosition = position;
        }

        if(mItemsOnClickListener != null){
            Log.e("UI","event listener SETTTT!!");
            holder.mTimeStamp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemsOnClickListener.onClick(mDataSet.get(position));
                    Log.e("UI","onClick");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    private void animateItem(View view) {
        view.setTranslationY(Util.getScreenHeight((Activity) view.getContext()));
        view.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(700)
                .start();
    }
}
