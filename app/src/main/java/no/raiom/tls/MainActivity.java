package no.raiom.tls;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    WakeToScanReceiver alarm = new WakeToScanReceiver();
    private Handler mHandler;
    private AppConfig app;

    private static final String KEY = "WHAT_EVER";
    private ExpandableListView mConfigList;

    private static final int REQUEST_LINK_TO_DBX = 0;  // This value is up to you

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = AppConfig.getInstance(this);
        if (DropboxAppender.hasLinkedAccount(this)) {
            findViewById(R.id.fisk).setVisibility(View.INVISIBLE);
        }

        mHandler = new Handler();

        mConfigList = (ExpandableListView) findViewById(R.id.device_config_list);
        mConfigList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2, int arg3, long arg4) {
                TempLogDeviceConfig deviceConfig = AppConfig.getInstance(MainActivity.this).deviceConfig;
                TempLogDeviceConfig.Device device = deviceConfig.get(arg2);
                if (arg3 == 1) {
                    Toast.makeText(MainActivity.this, "Started scanning", Toast.LENGTH_LONG).show();
                    //Log.i("Fisken", "Started scanning");
                    //Intent service = new Intent(MainActivity.this, BleScanService.class);
                    //service.putExtra("start_scanning", true);
                    //ArrayList addrs = new ArrayList();
                    //addrs.add(device.device);
                    //service.putStringArrayListExtra("device_addrs", addrs);
                    //startService(service);
                    //mHandler.postDelayed(new Runnable() {
                    //    @Override
                    //    public void run() {
                    //        Intent service = new Intent(MainActivity.this, BleScanService.class);
                    //        service.putExtra("start_scanning", false);
                    //        startService(service);
                    //    }
                    //}, 1000);
                }
                Log.i("Fisken", "arg2 " + arg2 + " arg3 " + arg3 + " device.addr" + device.device);
                return false;
            }
        });
        populateList();

        Log.i("Fisken", "Mainactivity.onCreate");
    }

    public void populateList() {
        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

        TempLogDeviceConfig deviceConfig = AppConfig.getInstance(this).deviceConfig;
        for (TempLogDeviceConfig.Device device : deviceConfig) {
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(KEY, device.name);

            List<Map<String, String>> children = new ArrayList<Map<String, String>>();

            Map<String, String> curChildMap = new HashMap<String, String>();
            children.add(curChildMap);
            curChildMap.put(KEY, device.device);

            curChildMap = new HashMap<String, String>();
            children.add(curChildMap);
            curChildMap.put(KEY, device.desc);

            childData.add(children);
        }

        // Set up our adapter
        SimpleExpandableListAdapter mConfigAdapter = new SimpleExpandableListAdapter(this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                new String[] {KEY},
                new int[] { android.R.id.text1 },

                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {KEY},
                new int[] { android.R.id.text1 });
        Log.i("Fisken", "Mainactivity.populateList: " + mConfigAdapter + " " + mConfigList);
        mConfigList.setAdapter(mConfigAdapter);
    }

    public void onClicked(View view) {
        Log.i("Fisken", "MainActivity.onClicked");
        app.getScanner().startScan();
    }

    public void offClicked(View view) {
        Log.i("Fisken", "MainActivity.offClicked");
        app.getScanner().stopScan();
    }

    public void fiskClicked(View view) {
        Log.i("Fisken", "MainActivity.fiskClicked");
        DropboxAppender.startLink((Activity)this, this, REQUEST_LINK_TO_DBX);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When the user clicks START ALARM, set the alarm.
            case R.id.start_action:
                alarm.setAlarm(this);
                return true;
            // When the user clicks CANCEL ALARM, cancel the alarm.
            case R.id.cancel_action:
                alarm.cancelAlarm(this);
                return true;
            case R.id.action_settings:
                Log.e("Fisken", "settings");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i("Fisken", "Result_OK");
            } else {
                Log.i("Fisken", "Failed, resultCode: " + resultCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
