package com.example.myapplication;
import android.annotation.SuppressLint;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IDEN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;


public class RetrieveData {
    public TelephonyManager telephonyManager;
    public List<CellInfo> cellInfoList;
    public CellInfo cellInfo;
    public SignalStrength signalStrength;
    public ServiceState serviceState;
    public CellIdentity cellIdentity;
    public String networkType;
    public CellSignalStrengthLte cellSignalStrengthLte;
    public CellSignalStrengthWcdma cellSignalStrengthWcdma;
    public CellSignalStrengthGsm cellSignalStrengthGsm;

    @SuppressLint({"NewApi", "MissingPermission"})
    public RetrieveData(TelephonyManager TM) {
        while (true) {
            try {
                //necessary instances
                this.telephonyManager = TM;
                this.cellInfoList = telephonyManager.getAllCellInfo();
                this.cellInfo = cellInfoList.get(0);
                this.signalStrength = telephonyManager.getSignalStrength();
                this.serviceState = telephonyManager.getServiceState();
                this.cellIdentity = cellInfo.getCellIdentity();
                for (final CellInfo infoLte : cellInfoList) {
                    if (infoLte instanceof CellInfoLte && infoLte.isRegistered()) {
                        this.cellSignalStrengthLte = ((CellInfoLte) infoLte).getCellSignalStrength();
                    }
                }
                for (final CellInfo infoGsm : cellInfoList) {
                    if (infoGsm instanceof CellInfoGsm && infoGsm.isRegistered()) {
                        this.cellSignalStrengthGsm = ((CellInfoGsm) infoGsm).getCellSignalStrength();
                    }
                }
                for (final CellInfo infoWcdma : cellInfoList) {
                    if (infoWcdma instanceof CellInfoWcdma && infoWcdma.isRegistered()) {
                        this.cellSignalStrengthWcdma = ((CellInfoWcdma) infoWcdma).getCellSignalStrength();
                    }
                }
                break;
            } catch (Exception e) {
                System.out.println("Nope");
            }
        }
    }
    public String[] collectinfo() {
        TaskUpdate taskUpdate = new TaskUpdate(telephonyManager);
        this.networkType = networkType();
        String[] info;
        System.out.println("Collect info being called");
        info = new String[]{telephonyManager.getNetworkOperatorName(), dBmPower(), SNR(), networkType(), FrequencyBand(), CellID(), timestamp()};
        return info;
    }



    @SuppressLint("MissingPermission")

    public String networkType() {
        try {
            switch (telephonyManager.getDataNetworkType()) {
                case NETWORK_TYPE_EDGE:
                case NETWORK_TYPE_GPRS:
                case NETWORK_TYPE_CDMA:
                case NETWORK_TYPE_IDEN:
                case NETWORK_TYPE_1xRTT:
                    return ("2G");
                case NETWORK_TYPE_UMTS:
                case NETWORK_TYPE_HSDPA:
                case NETWORK_TYPE_HSPA:
                case NETWORK_TYPE_HSPAP:
                case NETWORK_TYPE_EVDO_0:
                case NETWORK_TYPE_EVDO_A:
                case NETWORK_TYPE_EVDO_B:
                    return ("3G");
                case NETWORK_TYPE_LTE:
                    return ("4G");
                default:
                    return ("N/A");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return "N/A";
        }
    }

    //returns cell identity details in function of what networkType() returned
    public String CellID() {
        try {
            switch (networkType) {
                case "2G":
                    return GSMCID();
                case "3G":
                    return WCDMACID();
                case "4G":
                    return LTECID();
                default:
                    return "N/A";
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return "N/A";
        }
    }

    public String LTECID() {
        try {
            String string = cellIdentity.toString();
            int mci = string.indexOf("mCi");
            int mpci = string.indexOf("mPci");
            //int mtac = string.indexOf("mTac");
            return string.substring(mci + 4, mpci - 1);//"PCI:"+string.substring(mpci+5,mtac-1)
        } catch (Exception e) {
            System.out.println(e.toString());
            return "N/A";
        }

    }

    public String GSMCID() {
        try {
            String string = cellIdentity.toString();
            int mcid = string.indexOf("mCid");
            int marfcn = string.indexOf("mArfcn");
            return string.substring(mcid + 5, marfcn - 1);
        } catch (Exception e) {
            System.out.println(e.toString());
            return "N/A";
        }
    }

    public String WCDMACID() {
        try {
            String string = cellIdentity.toString();
            int mcid = string.indexOf("mCid");
            int mpsc = string.indexOf("mPsc");
            return string.substring(mcid + 5, mpsc - 1);
        } catch (Exception e) {
            System.out.println(e.toString());
            return "N/A";
        }
    }

    public String timestamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            return formattedDate;
        } catch (Exception e) {
            System.out.println(e.toString());
            return "N/A";
        }
    }


    //power in dbm for 2g 3g 4g
    public String dBmPower() {
        TaskUpdate taskUpdate = new TaskUpdate(telephonyManager);

        try {
            System.out.println("WE ARE HERE");
            System.out.println(cellInfo);
            switch (networkType) {
                case "2G":
                    return String.valueOf(cellSignalStrengthGsm.getDbm());
                case "3G":
                    return String.valueOf(cellSignalStrengthWcdma.getDbm());
                case "4G":
                    return String.valueOf(cellSignalStrengthLte.getDbm());
                default:
                    return "N/A";
            }
        } catch (Exception e){
            System.out.println(e.toString());
            return "N/A";
        }
    }

    //SNR only for LTE
    public String SNR(){
        try {
            if (networkType == "4G") {
                String string = signalStrength.toString();
                int rssnr = string.indexOf("rssnr");
                int cqi = string.indexOf("cqi");
                return string.substring(rssnr + 6, cqi - 1);
            } else {
                return "N/A";
            }
        } catch(Exception e) {
            System.out.println(e.toString());
            return "N/A";
        }
    }

        public String FrequencyBand() {
            TaskUpdate taskUpdate = new TaskUpdate(telephonyManager);
            if (cellInfo instanceof CellInfoLte) {
                CellIdentityLte cellIdentityLte = ((CellInfoLte) cellInfo).getCellIdentity();
                int earfcn = cellIdentityLte.getEarfcn();
                return String.valueOf(earfcn);
            } else if (cellInfo instanceof CellInfoGsm) {
                CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) cellInfo).getCellIdentity();
                int arfcn = cellIdentityGsm.getArfcn();
                return String.valueOf(arfcn);
            } else if (cellInfo instanceof CellInfoWcdma) {
                CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) cellInfo).getCellIdentity();
                int uarfcn = cellIdentityWcdma.getUarfcn();
                return String.valueOf(uarfcn);
            } else {
                return "N/A";
            }
        }
    }



