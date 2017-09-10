
package com.example.benjamin.popularmovie;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


public class GreenAdapter extends RecyclerView.Adapter<GreenAdapter.ViewHolder> {

    private Cursor mCursor;
    private String[] mWeatherData;
    private String imageBasePath = "https://image.tmdb.org/t/p/w500/";


    private ListItemClickListener mOnClickListener;

    public GreenAdapter(ListItemClickListener listener) {
        mOnClickListener = listener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.photo_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder ViewHolder, int index) {

        String fullPath;
        String search = "image.tmdb.org";
        String moviePath = mWeatherData[index];

        //If rhe moviePath is already a URL then do nothing
        if (moviePath != null) {
            if (moviePath.toLowerCase().indexOf(search.toLowerCase()) != -1) {
                //There is the keyword
                fullPath = moviePath;

            } else {
                //No key word
                fullPath = imageBasePath + moviePath;
            }
        } else {
            fullPath = imageBasePath + moviePath;
        }
        //fullPath = imageBasePath + moviePath;
        Context context = ViewHolder.mWeatherTextView.getContext();

        Picasso.with(context)
                .load(fullPath)
                .into(ViewHolder.mWeatherTextView);
    }

    @Override
    public int getItemCount() {
        if (null == mWeatherData) return 0;
        return mWeatherData.length;
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    public void setWeatherData(String[] movieList) {
        mWeatherData = movieList;
        notifyDataSetChanged();
    }


    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Within ViewHolder ///////////////////////////////////////////////////////
        public final ImageView mWeatherTextView;


        public ViewHolder(View view) {
            super(view);
            mWeatherTextView = (ImageView) view.findViewById(R.id.tv_image);
            itemView.setOnClickListener(this);
        }
        // Within ViewHolder ///////////////////////////////////////////////////////

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}