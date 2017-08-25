package com.example.gaurav.smar_test2.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.arlib.floatingsearchview.util.Util;
import com.example.gaurav.smar_test2.R;
import com.example.gaurav.smar_test2.SearchResultItem;

import java.util.ArrayList;
import java.util.List;

public class SearchDetailListAdapter extends RecyclerView.Adapter<SearchDetailListAdapter.ViewHolder> {

    private List<SearchResultItem> mDataSet = new ArrayList<>();

    private int mLastAnimatedItemPosition = -1;

    public interface OnItemClickListener{
        void onClick(SearchResultItem r);
    }

    private OnItemClickListener mItemsOnClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTimeStamp;
        public final TextView mText;


        public ViewHolder(View view) {
            super(view);
            mTimeStamp = (TextView) view.findViewById(R.id.ts);
            mText= (TextView) view.findViewById(R.id.text);

        }
    }

    public void swapData(List<SearchResultItem> mNewDataSet) {
        mDataSet = mNewDataSet;
        notifyDataSetChanged();
    }

    @Override
    public SearchDetailListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchDetailListAdapter.ViewHolder holder, final int position) {

        SearchResultItem r = mDataSet.get(position);
        holder.mTimeStamp.setText(r.ts);
        holder.mText.setText(r.text);

        String type = r.type;
        if(type.equals("speech")) {
            holder.mText.setTextColor(Color. parseColor("#000000"));
        } else if (type.equals("vision")) {
            holder.mText.setTextColor(Color. parseColor("#696969"));
        } else if (type.equals("face")) {
            holder.mText.setTextColor(Color. parseColor("#3fa906"));
        }

//        int color = Color.parseColor(colorSuggestion.getHex());
//        holder.mColorName.setTextColor(color);
//        holder.mColorValue.setTextColor(color);

        if(mLastAnimatedItemPosition < position){
            animateItem(holder.itemView);
            mLastAnimatedItemPosition = position;
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
