package com.comandante.uncolor.vkmusic.music_adapter;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.auth_activity.music_fragment.BaseMusicFragmentPresenter;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.utils.DurationConverter;
import com.flurry.android.FlurryAgent;

import java.util.Objects;

/**
 * Created by Uncolor on 25.08.2018.
 */

public class MusicViewHolder extends RecyclerView.ViewHolder implements ApiResponse.ApiFailureListener{

    private TextView textViewSongTitle;
    private TextView textViewArtist;
    private TextView textViewTotalTime;
    private ImageButton imageButtonDownload;
    private ImageView imageViewDownloaded;
    private ImageView imageViewAlbum;
    private ProgressBar progressBarDownloading;
    private LinearLayout linearLayoutBackground;
    private BaseMusicFragmentPresenter presenter;
    private BaseMusic music;

    public MusicViewHolder(View itemView, BaseMusicFragmentPresenter presenter) {
        super(itemView);
        this.presenter = presenter;
        textViewSongTitle = itemView.findViewById(R.id.textViewSongTitle);
        textViewArtist = itemView.findViewById(R.id.textViewArtist);
        textViewTotalTime = itemView.findViewById(R.id.textViewTotalTime);
        imageButtonDownload = itemView.findViewById(R.id.imageButtonDownload);
        linearLayoutBackground = itemView.findViewById(R.id.linearLayoutBackground);
        imageViewDownloaded = itemView.findViewById(R.id.imageViewDownloaded);
        imageViewAlbum = itemView.findViewById(R.id.imageViewAlbum);
        progressBarDownloading  = itemView.findViewById(R.id.progressBarDownloading);
    }

    public void bind(BaseMusic music, BaseMusic currentMusic) {
        this.music = music;
        textViewSongTitle.setText(music.getTitle());
        textViewArtist.setText(music.getArtist());
        textViewTotalTime.setText(DurationConverter.getDurationFormat(music.getDuration()));
        imageButtonDownload.setOnClickListener(getOnDownloadClickListener());
        linearLayoutBackground.setOnClickListener(getOnPlayClickListener());
        bindTrackSelection(currentMusic);
        bindState();
        if(this.music.getAlbumImageUrl() == null) {
            imageViewAlbum.setImageResource(R.drawable.album_default);
            presenter.onFindAlbumImageUrl(this.music, getAdapterPosition());
        }
        else {
            Glide.with(App.getContext())
                    .load(this.music.getAlbumImageUrl())
                    .into(imageViewAlbum);
        }
    }

    private void bindTrackSelection(BaseMusic currentMusic){
        if (currentMusic == null) {
            return;
        }
        if (Objects.equals(music.getDownload(), currentMusic.getDownload())) {
            linearLayoutBackground.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                    R.drawable.track_selected_drawable));
        } else {
            linearLayoutBackground.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                    R.drawable.track_not_selected_drawable));
        }
    }


    private void bindState(){
       switch (music.getState()){
           case BaseMusic.STATE_DEFAULT:
               imageViewDownloaded.setVisibility(View.GONE);
               progressBarDownloading.setVisibility(View.GONE);
               imageButtonDownload.setImageResource(R.drawable.download);
               imageButtonDownload.setEnabled(true);
               break;
           case BaseMusic.STATE_DOWNLOADING:
               imageViewDownloaded.setVisibility(View.GONE);
               progressBarDownloading.setVisibility(View.VISIBLE);
               imageButtonDownload.setImageResource(R.drawable.download);
               imageButtonDownload.setEnabled(false);
               break;
           case BaseMusic.STATE_COMPLETED:
               imageViewDownloaded.setVisibility(View.VISIBLE);
               progressBarDownloading.setVisibility(View.GONE);
               imageButtonDownload.setImageResource(R.drawable.ic_trash);
               imageButtonDownload.setEnabled(true);
               break;
       }
    }


    private View.OnClickListener getOnPlayClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent(itemView.getContext().getString(R.string.log_play_track));
                presenter.onPlayTrack(music, getAdapterPosition());
            }
        };
    }

    private View.OnClickListener getOnDownloadClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(music.getState() == BaseMusic.STATE_COMPLETED){
                    presenter.onDeleteTrack(music, getAdapterPosition());
                }
                else if(music.getState() == BaseMusic.STATE_DEFAULT) {
                    FlurryAgent.logEvent(itemView.getContext().getString(R.string.log_download_track));
                    presenter.onUploadTrack(music);
                }
            }
        };
    }


    @Override
    public void onFailure(int code, String message) {

    }
}
