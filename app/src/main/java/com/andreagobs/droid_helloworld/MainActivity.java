package com.andreagobs.droid_helloworld;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_ADMIN;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "XXX";
    private static final int ENABLE_BT_REQUEST_ID = 1;
    private static final int ENABLE_GPS_REQUEST_ID = 2;
    private static final int SCAN_INTERVAL = 10000;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mScanner;
    private ScanCallback mScanCallback;
    private Handler handler = new Handler();

    private Button mButton;
    private Button mButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button);
        mButton2 = (Button) findViewById(R.id.button2);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{ ACCESS_FINE_LOCATION }, ENABLE_GPS_REQUEST_ID);
                        }
                        return;
                    }
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{ BLUETOOTH_ADMIN }, ENABLE_BT_REQUEST_ID);
                        }
                        return;
                    }

                    final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    {
                        Log.w(TAG, "GPS not provided");
                        Intent enableLocation = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableLocation, ENABLE_GPS_REQUEST_ID);
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (!locationManager.isLocationEnabled())
                        {
                            Log.w(TAG, "GPS not enabled");
                            Intent enableLocation = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableLocation, ENABLE_GPS_REQUEST_ID);
                            return;
                        }
                    }

                    final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                    if (!mBluetoothAdapter.isEnabled())
                    {
                        Log.w(TAG, "BT not enabled");
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
                        return;
                    }

                    mScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    mScanCallback = new ScanCallback() {
                        @Override
                        public void onScanResult(int callBackType, ScanResult result) {
                            Log.d(TAG,"onScanResult: " + result.getDevice().getName());
                            super.onScanResult(callBackType, result);
                        }

                        @Override
                        public void onBatchScanResults(List<ScanResult> results) {
                            for(ScanResult scanResult : results) {
                                Log.d(TAG,"ScanResult: " + scanResult.toString());
                            }
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            Log.d(TAG,"Scan failed with error code: " + errorCode);
                        }
                    };

                    ScanSettings settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();

                    List<ScanFilter> filters = new ArrayList<ScanFilter>();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "stop scan...");
                            mScanner.stopScan(mScanCallback);
                        }
                    }, SCAN_INTERVAL);

                    Log.d(TAG, "start scan...");
                    mScanner.stopScan(mScanCallback);
                    mScanner.startScan(filters, settings, mScanCallback);
                    //scanner.startScan(filters, settings, mScanCallback);
                }
                catch (Exception ex) {
                    Log.e(TAG, "Scan error: " + ex);
                }
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "stop scan...");
                    mScanner.stopScan(mScanCallback);
                }
                catch (Exception ex) {
                    Log.e(TAG, "Stop error: " + ex);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
            case ENABLE_BT_REQUEST_ID:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "BT enabled now on this device");
                }
                break;

            case ENABLE_GPS_REQUEST_ID:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "GPS enabled now on this device");
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ENABLE_BT_REQUEST_ID:
            case ENABLE_GPS_REQUEST_ID:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission granted: id=" + requestCode);
                }
                break;
            }
        }
    }
}