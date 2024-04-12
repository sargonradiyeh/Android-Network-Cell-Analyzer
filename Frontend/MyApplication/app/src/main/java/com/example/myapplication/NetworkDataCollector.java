package com.example.myapplication;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.*;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



public class NetworkDataCollector {
    private TelephonyManager telephonyMgr;
    private List<CellInfo> allCellInfo;
    private CellInfo primaryCellInfo;
    private SignalStrength currentSignalStrength;
    private ServiceState currentState;
    private CellIdentity primaryCellIdentity;
    private CellSignalStrengthLte lteStrength;
    private CellSignalStrengthWcdma wcdmaStrength;
    private CellSignalStrengthGsm gsmStrength;
    private PhoneStateListener stateListener;
    private String currentNetworkType;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.P)
    public NetworkDataCollector(TelephonyManager teleManager, Context context) {
        while (true) {
            try {
                this.telephonyMgr = teleManager;
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Permissions should be handled before this point.
                    // This will log a message and prevent further execution if permissions are not granted.
                    Log.e("NetworkDataCollector", "ACCESS_FINE_LOCATION permission is not granted.");
                    return; // Exit if permissions are not granted.
                }
                this.allCellInfo = telephonyMgr.getAllCellInfo();
                this.primaryCellInfo = allCellInfo.get(0);
                this.currentSignalStrength = telephonyMgr.getSignalStrength();
                this.currentState = telephonyMgr.getServiceState();
                this.primaryCellIdentity = primaryCellInfo.getCellIdentity();

                for (CellInfo info : allCellInfo) {
                    if (info instanceof CellInfoLte && info.isRegistered()) {
                        this.lteStrength = ((CellInfoLte) info).getCellSignalStrength();
                    }
                }

                for (CellInfo info : allCellInfo) {
                    if (info instanceof CellInfoGsm && info.isRegistered()) {
                        this.gsmStrength = ((CellInfoGsm) info).getCellSignalStrength();
                    }
                }

                for (CellInfo info : allCellInfo) {
                    if (info instanceof CellInfoWcdma && info.isRegistered()) {
                        this.wcdmaStrength = ((CellInfoWcdma) info).getCellSignalStrength();
                    }
                }
                break;
            } catch (Exception e) {
                System.out.println("Retry");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String[] createDataSnapshot() {
        this.currentNetworkType = determineNetworkType();
        String[] data = {
                telephonyMgr.getNetworkOperatorName(),
                signalDbm(),
                lteSignalToNoiseRatio(),
                currentNetworkType,
                String.valueOf(currentState.getChannelNumber()),
                retrieveCellId(),
                currentTimestamp(),
                numericTimestamp()
        };
        return data;
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String determineNetworkType() {
        try {
            int networkType = telephonyMgr.getDataNetworkType();
            if (networkType == TelephonyManager.NETWORK_TYPE_LTE || networkType == TelephonyManager.NETWORK_TYPE_NR) {
                return "4G/5G";
            } else if (networkType <= TelephonyManager.NETWORK_TYPE_HSPAP) {
                return "3G";
            } else if (networkType <= TelephonyManager.NETWORK_TYPE_EDGE) {
                return "2G";
            } else {
                return "Unknown";
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return "Unknown";
        }
    }

    public String currentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss aa");
        return dateFormat.format(new Date());
    }

    public String numericTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        return dateFormat.format(new Date()).replaceAll("\\D+", "");
    }

    public String signalDbm() {
        try {
            switch (currentNetworkType) {
                case "2G":
                    return String.valueOf(gsmStrength.getDbm());
                case "3G":
                    return String.valueOf(wcdmaStrength.getDbm());
                case "4G/5G":
                    return String.valueOf(lteStrength.getDbm());
                default:
                    return "N/A";
            }
        } catch (Exception e) {
            return "Error";
        }
    }

    public String lteSignalToNoiseRatio() {
        try {
            if ("4G/5G".equals(currentNetworkType)) {
                String details = currentSignalStrength.toString();
                int rssnrIndex = details.indexOf("rssnr");
                int cqiIndex = details.indexOf("cqi");
                return details.substring(rssnrIndex + 6, cqiIndex - 1);
            } else {
                return "N/A";
            }
        } catch (Exception e) {
            return "Error";
        }
    }

    public String retrieveCellId() {
        try {
            String cellDetails = primaryCellIdentity.toString();
            int idStart = cellDetails.indexOf("mCid") + 5;
            int idEnd = cellDetails.indexOf(" ", idStart);
            return cellDetails.substring(idStart, idEnd);
        } catch (Exception e) {
            return "Unknown ID";
        }
    }
}
