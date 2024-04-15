package com.example.myapplication;
import android.annotation.SuppressLint;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;



import java.util.List;
import java.util.concurrent.Executor;

public class TaskUpdate {
    public TelephonyManager telephonyManager;
    @SuppressLint("MissingPermission")
    public TaskUpdate(TelephonyManager TM){
        this.telephonyManager=TM;
        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
            }
        };
        TelephonyManager.CellInfoCallback callback = new TelephonyManager.CellInfoCallback() {
            @Override
            public void onCellInfo( List<CellInfo> cellInfo) {

            }
        };
        telephonyManager.requestCellInfoUpdate(executor, callback);
    }
}
