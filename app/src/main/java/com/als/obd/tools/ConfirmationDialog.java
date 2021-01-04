package com.als.obd.tools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.als.obd.R;

public class ConfirmationDialog extends Dialog {


    public interface ConfirmationListener {
        public void OkBtnReady();

    }

    private String formType;
    private ConfirmationListener readyListener;

    public ConfirmationDialog(Context context, String type, ConfirmationListener readyListener) {
        super(context);
        this.formType = type;
        this.readyListener = readyListener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        setContentView(R.layout.popup_edit_delete_lay);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        getWindow().setAttributes(lp);


        TextView changeTitleView, titleDescView;
        changeTitleView = (TextView)findViewById(R.id.changeTitleView);
        titleDescView=(TextView)findViewById(R.id.titleDescView);
        TextView confirmPopupButton = (TextView)findViewById(R.id.confirmPopupButton);
        TextView cancelPopupButton = (TextView)findViewById(R.id.cancelPopupButton);

        changeTitleView.setText("Confirmation");


        cancelPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        confirmPopupButton.setOnClickListener(new OkJobListener());

    }



    private class OkJobListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            readyListener.OkBtnReady();
        }
    }


}
