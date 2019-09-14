package com.example.paginationtest;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.util.List;
import java.util.logging.Handler;

public class PostRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder>  {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final int VIEW_TYPE_TEXT = 2;
    private boolean isLoaderVisible = false;
    RequestOptions options;

    private List<PostItem> mPostItems;
    Context mContext;


    public PostRecyclerAdapter(List<PostItem> postItems,Context context) {
        this.mPostItems = postItems;
        this.mContext = context;
        options = new RequestOptions().centerCrop().placeholder(R.drawable.loading_shape).error(R.drawable.loading_shape);
    }

    @NonNull
    @Override
    public  BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            case VIEW_TYPE_TEXT:
                return new TextHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.new_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);

    }

    @Override
    public int getItemViewType(int position) {
        String s = mPostItems.get(position).getType();
        if (isLoaderVisible) {
            if(position == mPostItems.size()-1){
                return VIEW_TYPE_LOADING;
            }
            else {
                if(s.equals("text")){

                    return VIEW_TYPE_TEXT;
                }
                else {
                    return VIEW_TYPE_NORMAL;
                }
            }
        }

        else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mPostItems == null ? 0 : mPostItems.size();
    }

    public void addItems(List<PostItem> postItems) {
        mPostItems.addAll(postItems);
        notifyDataSetChanged();
    }

    public void addLoading() {
        isLoaderVisible = true;
        mPostItems.add(new PostItem());
        notifyItemInserted(mPostItems.size() - 1);
    }

    public void removeLoading() {
        isLoaderVisible = false;
        int position = mPostItems.size() - 1;
        PostItem item = getItem(position);
        if (item != null) {
            mPostItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        mPostItems.clear();
        notifyDataSetChanged();
    }

    PostItem getItem(int position) {
        return mPostItems.get(position);
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.textViewTitle)
        TextView textViewTitle;
        @BindView(R.id.textViewDescription)
        TextView textViewDescription;
        @BindView(R.id.loadingKusalm)
        ImageView imageView;



        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);
            PostItem item = mPostItems.get(position);

            textViewTitle.setText(item.getTitle());
            textViewDescription.setText(item.getDescription());
            Glide.with(mContext).load(mPostItems.get(position).getImage_url()).apply(options).into(this.imageView);
        }
    }

    public class ProgressHolder extends BaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void clear() {
        }
    }

    public class TextHolder extends BaseViewHolder {

        @BindView(R.id.videoFullScreenPlayer)PlayerView videoFullScreenPlayer;
        @BindView(R.id.spinnerVideoDetails)ProgressBar spinnerVideoDetails;
        /*@BindView(R.id.playButton)
        ImageButton imageButton;*/

        SimpleExoPlayer exoPlayer;

        String Url;
        Context context;


        public TextHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);
            PostItem item = mPostItems.get(position);

            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = ExoPlayerFactory.newSimpleInstance(mContext,trackSelector);

            Uri videoURI = Uri.parse(item.getImage_url());

            DefaultHttpDataSourceFactory defaultHttpDataSourceFactory = new DefaultHttpDataSourceFactory("xxx");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource videoSource = new ExtractorMediaSource.Factory(defaultHttpDataSourceFactory)
                    .createMediaSource(videoURI);

            videoFullScreenPlayer.setPlayer(exoPlayer);
            videoFullScreenPlayer.setUseController(true);
            videoFullScreenPlayer.setControllerHideOnTouch(true);
            exoPlayer.prepare(videoSource);


            /*imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exoPlayer.setPlayWhenReady(true);
                    imageButton.setVisibility(View.GONE);
                }
            });*/


        }


    }
}
