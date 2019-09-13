package com.comandante.uncolor.vkmusic.utils;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.utils.ui_error_handler_states.BaseUIErrorHandlerState;
import com.comandante.uncolor.vkmusic.utils.ui_error_handler_states.UIErrorHandlerResignInState;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UIErrorHandler {

    private RetryAfterErrorListener listener;

    @BindView(R.id.textViewExceptionTitle)
    TextView textViewExceptionTitle;

    @BindView(R.id.textViewExceptionSubTitle)
    TextView textViewExceptionSubTitle;

    @BindView(R.id.imageViewException)
    ImageView imageViewException;

    @BindView(R.id.buttonExceptionRetry)
    Button buttonRetry;

    private UIErrorType type;

    private View view;

    public UIErrorHandler(Context context, View view) {
        this.view = view;
        ButterKnife.bind(context, view);
    }

    @OnClick(R.id.buttonExceptionRetry)
    void onRetryButtonClick(){
        if(listener == null){
            return;
        }
        listener.onRetry(type);
    }

    public void setResignInState(){
        UIErrorHandlerResignInState state = new UIErrorHandlerResignInState();
        fillHandler(state);

    }

    private void fillHandler(BaseUIErrorHandlerState state){
        if(state.getType() == null){
            return;
        }
        else {
            type = state.getType();
        }

        if(state.getTitle() != null){
            textViewExceptionTitle.setVisibility(View.VISIBLE);
            textViewExceptionTitle.setText(state.getTitle());
        }else {
            textViewExceptionTitle.setVisibility(View.GONE);
        }

        if(state.getSubTitle() != null){
            textViewExceptionSubTitle.setText(View.VISIBLE);
            textViewExceptionSubTitle.setText(state.getTitle());
        }else {
            textViewExceptionSubTitle.setVisibility(View.GONE);
        }

        if(state.getButtonTitle() != null){
            buttonRetry.setVisibility(View.VISIBLE);
            buttonRetry.setText(state.getButtonTitle());
        }else {
            buttonRetry.setVisibility(View.GONE);
        }

        if(state.getImageResourceId() != 0){
            imageViewException.setVisibility(View.VISIBLE);
            imageViewException.setImageResource(state.getImageResourceId());
        }else {
            textViewExceptionTitle.setVisibility(View.GONE);
        }
    }

    public void setRetryListener(RetryAfterErrorListener listener) {
        this.listener = listener;
    }

    public interface RetryAfterErrorListener {
        void onRetry(UIErrorType state);
    }

}
