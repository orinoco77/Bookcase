package uk.co.sequoiasolutions.bookcase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements ScanResultReceiver.Receiver {

    static MainActivity ma;
    private Button startButton;
    private Button configureButton;
    private Button scanButton;
    private ListView listViewEbooks;
    private ProgressBar spinner;
    private boolean started = false;
    private static String mLog;
    private ServerHandler handler;
    private String path;
    private String port;
    private SharedPreferences sharedPref;
    public ScanResultReceiver mReceiver;
    public static final String STATE_START = "Started";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            started = savedInstanceState.getBoolean(STATE_START);
        mReceiver = new ScanResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ma = this;
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setOnClickListener(startListener);
        configureButton = (Button) findViewById(R.id.buttonConfigure);
        configureButton.setOnClickListener(configureListener);
        scanButton = (Button) findViewById(R.id.buttonScan);
        scanButton.setOnClickListener(scanListener);
        listViewEbooks = (ListView) findViewById(R.id.listViewEbooks);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        if (started) {
            startButton.setText("Stop Server");
        } else {
            startButton.setText("Start Server");
        }
        populateListView();
    }


    private OnClickListener startListener = new OnClickListener() {
        public void onClick(View v) {
            if (started) {
                Toast.makeText(MainActivity.this, "Stopping Server.", Toast.LENGTH_LONG).show();
                startButton.setText("Start Server");
                started = false;
                handler.stop();
            } else {
                port = sharedPref.getString("port", "");
                if (port.equals(""))
                    port = "8080";
                path = sharedPref.getString("path", "");
                if (path.equals(""))
                    path = "/sdcard/";
                File testPath = new File(path);
                if (testPath.exists()) {
                    handler = new ServerHandler(Integer.parseInt(port), path);

                    Toast.makeText(MainActivity.this, "Starting Server.", Toast.LENGTH_LONG).show();
                    startButton.setText("Stop Server");
                    started = true;
                    try {
                        handler.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to find the specified path.", Toast.LENGTH_LONG).show();
                }
            }
        }
    };


    private OnClickListener configureListener = new OnClickListener() {
        public void onClick(View v) {
            if (!started) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else {
                Toast.makeText(MainActivity.this, "Please stop the server before changing configuration.", Toast.LENGTH_LONG).show();
            }
        }
    };

    private OnClickListener scanListener = new OnClickListener() {
        public void onClick(View v) {
            spinner.setVisibility(View.VISIBLE);
            ScanService.startActionScanEbooks(MainActivity.this, path, mReceiver);
        }
    };

    private void populateListView() {
        EbookDataSource dataSource = new EbookDataSource(MainActivity.this);
        dataSource.open();
        String[] columns = new String[]{MySQLiteHelper.COLUMN_TITLE, MySQLiteHelper.COLUMN_AUTHOR, MySQLiteHelper.COLUMN_IMAGEURL};
        int[] viewIDs = new int[]{R.id.title, R.id.description, R.id.imageView};
        Cursor cursor = dataSource.getEbookCursor();
        SimpleCursorAdapter adapter;
        adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.listviewlayout, cursor, columns, viewIDs, 0);
        listViewEbooks.setAdapter(adapter);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == 0) {
            spinner.setVisibility(View.GONE);
            populateListView();
        }
        if (resultCode == 1) {
            Toast.makeText(MainActivity.this, resultData.getString("errorMessage"), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_START, started);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onBackPressed() {
        if (started)
            moveTaskToBack(true); // we can't allow the activity to be destroyed or we'll lose the connection to the webserver
        else
            super.onBackPressed();
    }
}