package com.estimote.examples.demos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.estimote.examples.demos.R;


public class BeaconActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.becon_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());

        findViewById(R.id.btn_search_for_beacon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListBeaconsActivity(SearchBeaconActivity.class.getName());
            }
        });

    }

    /**
     * Start Beacon Activity
     * @param extra
     */
    private void startListBeaconsActivity(String extra) {
        Intent intent = new Intent(BeaconActivity.this, ListBeaconsActivity.class);
        intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, extra);
        startActivity(intent);
    }
}
