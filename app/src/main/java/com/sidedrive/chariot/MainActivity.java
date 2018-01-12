package com.sidedrive.chariot;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button m_rider, m_driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_driver = (Button) findViewById(R.id.driver);
        m_rider = (Button) findViewById(R.id.rider);

        m_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent driverLoginIntent = new Intent(MainActivity.this, DriverLoginActivity.class);
                startActivity(driverLoginIntent);
                finish();
                return;
            }
        });

        m_rider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent riderLoginIntent = new Intent(MainActivity.this, RiderLoginActivity.class);
                startActivity(riderLoginIntent);
                finish();
                return;
            }
        });

    }
}
