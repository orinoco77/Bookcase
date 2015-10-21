package uk.co.sequoiasolutions.bookcase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;




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
    private EbookAdapter ebookAdapter;
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
        ebookAdapter = new EbookAdapter(this);
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
        List<Ebook> ebooks = dataSource.getAllEbooks();
        Ebook[] ebookArray = new Ebook[ebooks.size()];
        ebooks.toArray(ebookArray);
        ebookAdapter.setEbookList(ebookArray);
        ListView listView = (ListView) findViewById(R.id.listViewEbooks);
        listView.setAdapter(ebookAdapter);
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

    private class EbookAdapter extends BaseAdapter { //The stocks list adaptor

        class ViewHolder {
            TextView title;
            TextView author;
            ImageView image;
            TextView filename;
        }

        private LayoutInflater layoutInflater;
        private Ebook[] ebooks = null; //Array of stocks


        public EbookAdapter(Context context) {
            super();


            layoutInflater = LayoutInflater.from(context);
        }

        public void setEbookList(Ebook[] ebooksinfo) {
            this.ebooks = ebooksinfo;// //////////////LITERALLY THIS

        }

        @Override
        public int getCount() {
            return ebooks.length;
        }

        @Override
        public Object getItem(int position) {
            return ebooks[position];
        }

        public Ebook[] getAll() { //Return the array of ebooks
            return ebooks;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder; //New holder
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.listviewlayout,
                        null);
                holder = new ViewHolder();
                // Creates the new viewholder define above, storing references to the children
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.author = (TextView) convertView.findViewById(R.id.author);
                holder.filename = (TextView) convertView.findViewById(R.id.filename);
                holder.image = (ImageView) convertView.findViewById(R.id.imageView);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            holder.title.setText(ebooks[position].getTitle());
            holder.author.setText(ebooks[position].getAuthor());
            holder.filename.setText(ebooks[position].getEbookUrl());
            byte[] bytes = ebooks[position].getImageUrl();
            holder.image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            return convertView;
        }
    }
}