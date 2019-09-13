package com.comandante.uncolor.vkmusic.music_adapter.holders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenterInterface;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.music.PlaylistRepository;
import com.comandante.uncolor.vkmusic.utils.DurationConverter;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TempMusicViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.textViewSongTitle)
    TextView textViewSongTitle;

    @BindView(R.id.textViewArtist)
    TextView textViewArtist;

    @BindView(R.id.textViewTotalTime)
    TextView textViewTotalTime;

    @BindView(R.id.imageButtonDownload)
    ImageButton imageButtonDownload;

    @BindView(R.id.imageViewDownloaded)
    ImageView imageViewDownloaded;

    @BindView(R.id.imageViewAlbum)
    ImageView imageViewAlbum;

    @BindView(R.id.progressBarDownloading)
    ProgressBar progressBarDownloading;

    @BindView(R.id.linearLayoutBackground)
    LinearLayout linearLayoutBackground;

    private BaseMusic music;

    private BaseMusicPresenterInterface presenter;

    public TempMusicViewHolder(@NonNull View itemView, BaseMusicPresenterInterface presenter) {
        super(itemView);
        this.presenter = presenter;
        ButterKnife.bind(this, itemView);
    }

    public void bind(BaseMusic music){
        this.music = music;
        textViewArtist.setText(music.getArtist());
        textViewSongTitle.setText(music.getTitle());
        textViewTotalTime.setText(DurationConverter.getDurationFormat(music.getDuration()));
        bindTrackSelection();
        bindState();
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

    private void bindTrackSelection(){
        BaseMusic currentMusic = PlaylistRepository.get().getCurrentMusic();
        if (currentMusic == null) {
            return;
        }
        long musicId = music.getId();
        long currentMusicId = currentMusic.getId();
        if (Objects.equals(musicId, currentMusicId)) {
            linearLayoutBackground.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                    R.drawable.track_selected_drawable));
        } else {
            linearLayoutBackground.setBackground(ContextCompat.getDrawable(itemView.getContext(),
                    R.drawable.track_not_selected_drawable));
        }
    }


    @OnClick(R.id.linearLayoutBackground)
    void onPlayClick() {
        presenter.onPlayTrack(music, 0);
    }

    @OnClick(R.id.imageButtonDownload)
    void onButtonDownloadClick(){
        if(music.getState() == BaseMusic.STATE_COMPLETED){
            presenter.onDeleteTrack(music, getAdapterPosition());
        }
        else if(music.getState() == BaseMusic.STATE_DEFAULT) {
            presenter.onUploadTrack(music);
        }
    }
}
