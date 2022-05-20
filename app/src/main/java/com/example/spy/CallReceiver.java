package com.example.spy;

import android.content.Context;
import android.util.Log;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {

    private static final String TAG = "CallReceiver";
    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        Log.i(TAG, "onIncomingCallStarted: " + number);

    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.i(TAG, "onOutgoingCallStarted: " + number);
    }
}
