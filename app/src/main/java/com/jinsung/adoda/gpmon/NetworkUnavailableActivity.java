package com.jinsung.adoda.gpmon;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkUnavailableActivity extends ActionBarActivity {

    String mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_unavailable);

        Intent callerIntent = getIntent();
        mContext = callerIntent.getStringExtra("context");
        int errorCode = callerIntent.getIntExtra("errorCode", 0);
        String description = callerIntent.getStringExtra("description");

        TextView tvDescription = (TextView)this.findViewById(R.id.textview_description);
        tvDescription.setText(
            String.format(
                getString(R.string.net_unavail_text),
                description
            )
        );

        Button btnTapToReload = (Button)this.findViewById(R.id.button_reload);
        btnTapToReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Toast.makeText(NetworkUnavailableActivity.this, "Server not accessible", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);

        if (mContext.equals("login")) {
            moveTaskToBack(true);

            finish();

            android.os.Process.killProcess(android.os.Process.myPid());
        }
        else
            super.onBackPressed();

    }

}
