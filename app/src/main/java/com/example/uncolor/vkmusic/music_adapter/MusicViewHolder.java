package com.example.uncolor.vkmusic.music_adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.auth_activity.music_fragment.BaseMusicFragmentPresenter;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.utils.DurationConverter;

import java.util.Objects;

/**
 * Created by Uncolor on 25.08.2018.
 */

public class MusicViewHolder extends RecyclerView.ViewHolder {

    private TextView textViewSongTitle;
    private TextView textViewArtist;
    private TextView textViewTotalTime;
    private ImageButton imageButtonDownload;
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
    }

    public void bind(BaseMusic music, BaseMusic currentMusic) {
        this.music = music;
        textViewSongTitle.setText(music.getTitle());
        textViewArtist.setText(music.getArtist());
        textViewTotalTime.setText(DurationConverter.getDurationFormat(music.getDuration()));
        imageButtonDownload.setOnClickListener(getOnDownloadClickListener());
        linearLayoutBackground.setOnClickListener(getOnPlayClickListener());
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

    private View.OnClickListener getOnPlayClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onPlayTrack(music, getAdapterPosition());
            }
        };
    }

    private View.OnClickListener getOnDownloadClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (music == null) {
                    App.Log("null");
                } else {
                    App.Log("not null");
                }
                presenter.onUploadTrack(music);
            }
        };
    }


}
