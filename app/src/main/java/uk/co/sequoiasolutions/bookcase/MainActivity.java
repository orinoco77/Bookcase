package uk.co.sequoiasolutions.bookcase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {


    private Button startButton;
    private Button configureButton;
    private Button scanButton;
    private boolean started = false;
    private static String mLog;
    private ServerHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setOnClickListener(startListener);
        configureButton = (Button) findViewById(R.id.buttonConfigure);
        configureButton.setOnClickListener(configureListener);
        scanButton = (Button) findViewById(R.id.buttonScan);
        scanButton.setOnClickListener(scanListener);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String port = sharedPref.getString("port", "");
        String path = sharedPref.getString("path", "");
        handler = new ServerHandler(Integer.parseInt(port));
        handler.Path = path;
    }


    private OnClickListener startListener = new OnClickListener() {
        public void onClick(View v) {
            if (started) {
                Toast.makeText(MainActivity.this, "Stopping Server.", Toast.LENGTH_LONG).show();
                startButton.setText("Start Server");
                started = false;
                handler.stop();
            } else {
                Toast.makeText(MainActivity.this, "Starting Server.", Toast.LENGTH_LONG).show();
                startButton.setText("Stop Server");
                started = true;
                try {
                    handler.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private OnClickListener configureListener = new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
    };

    private OnClickListener scanListener = new OnClickListener() {
        public void onClick(View v) {

        }
    };

}