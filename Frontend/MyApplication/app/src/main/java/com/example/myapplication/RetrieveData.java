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
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Locale;




public class RetrieveData {
    public TelephonyManager tManager;
    public List<CellInfo> cellinfolist;
    public CellInfo cellinfo;
    public SignalStrength signalStrength;
    public ServiceState serviceState;
    public CellIdentity cellId;
    public String network;
    public CellSignalStrengthLte strengthLte;
    public CellSignalStrengthWcdma strengthWcdma;
    public CellSignalStrengthGsm strengthGsm;

    @SuppressLint({"NewApi", "MissingPermission"})
    /*
     * Constructor for RetrieveData class.
     * Initializes necessary instances for retrieving cell data.
     *
     * @param TM TelephonyManager instance used to access telephony services
     */
    public RetrieveData(TelephonyManager TM) { //input: TelephonyManager TM
        while (true) {
            try {
                // Initialize necessary instances
                this.tManager = TM; // Initialize TelephonyManager instance
                this.cellinfolist = tManager.getAllCellInfo(); // Get list of all cell info
                this.cellinfo = cellinfolist.get(0); // Get the first cell info
                this.signalStrength = tManager.getSignalStrength(); // Get signal strength
                this.serviceState = tManager.getServiceState(); // Get service state
                this.cellId = cellinfo.getCellIdentity(); // Get cell identity

                // Loop through cell info list to find registered LTE cell and get signal strength
                for (final CellInfo infoLte : cellinfolist) {
                    if (infoLte instanceof CellInfoLte && infoLte.isRegistered()) {
                        this.strengthLte = ((CellInfoLte) infoLte).getCellSignalStrength();
                    }
                }

                // Loop through cell info list to find registered GSM cell and get signal strength
                for (final CellInfo infoGsm : cellinfolist) {
                    if (infoGsm instanceof CellInfoGsm && infoGsm.isRegistered()) {
                        this.strengthGsm = ((CellInfoGsm) infoGsm).getCellSignalStrength();
                    }
                }

                // Loop through cell info list to find registered WCDMA cell and get signal strength
                for (final CellInfo infoWcdma : cellinfolist) {
                    if (infoWcdma instanceof CellInfoWcdma && infoWcdma.isRegistered()) {
                        this.strengthWcdma = ((CellInfoWcdma) infoWcdma).getCellSignalStrength();
                    }
                }
                break; // Exit the loop after successfully initializing all instances
            } catch (Exception e) {
                // Catch any exceptions and continue looping
            }
        }
    }
    public String[] collectinfo() { //Function that
        TaskUpdate taskUpdate = new TaskUpdate(tManager);
        this.network = findnetworktype();
        String[] info;
        System.out.println("Collect info being called");
        info = new String[]{tManager.getNetworkOperatorName(), PowerdB(),
                SNR(), findnetworktype(), FrequencyBand(),
                CellID(), timestamp(), macAddr()};
        return info;
    }

    @SuppressLint("MissingPermission")

    /*
     * Method to determine the network type based on the data network type.
     *
     * @return String representing the network type (2G, 3G, 4G, or N/A if unknown)
     */
    public String findnetworktype() {
        try {
            // Switch statement to determine network type based on data network type
            switch (tManager.getDataNetworkType()) {
                case NETWORK_TYPE_EDGE:
                case NETWORK_TYPE_GPRS:
                case NETWORK_TYPE_CDMA:
                case NETWORK_TYPE_IDEN:
                case NETWORK_TYPE_1xRTT:
                    return ("2G"); // Return 2G for EDGE, GPRS, CDMA, IDEN, and 1xRTT
                case NETWORK_TYPE_UMTS:
                case NETWORK_TYPE_HSDPA:
                case NETWORK_TYPE_HSPA:
                case NETWORK_TYPE_HSPAP:
                case NETWORK_TYPE_EVDO_0:
                case NETWORK_TYPE_EVDO_A:
                case NETWORK_TYPE_EVDO_B:
                    return ("3G"); // Return 3G for UMTS, HSDPA, HSPA, HSPAP, EVDO_0, EVDO_A, and EVDO_B
                case NETWORK_TYPE_LTE:
                    return ("4G"); // Return 4G for LTE
                default:
                    return ("N/A"); // Return N/A for other network types
            }
        } catch (Exception e) {
            System.out.println(e.toString()); // Print exception message
            return "N/A"; // Return N/A if an exception occurs
        }
    }


    //returns cell identity details in function of what networkType() returned
    /*
     * Method to determine the Cell ID based on the network type.
     *
     * return String representing the Cell ID (or "N/A" if unknown)
     */
    public String CellID() {
        try {
            // Switch statement to determine Cell ID based on network type
            switch (network) {
                case "2G":
                    return GSMCID(); // Return GSM Cell ID for 2G network
                case "3G":
                    return WCDMACID(); // Return WCDMA Cell ID for 3G network
                case "4G":
                    return LTECID(); // Return LTE Cell ID for 4G network
                default:
                    return "N/A"; // Return N/A for unknown network types
            }
        } catch (Exception e) {
            System.out.println(e.toString()); // Print exception message
            return "N/A"; // Return N/A if an exception occurs
        }
    }



    /*
     * Method to extract GSM Cell ID from cellIdentity.
     *
     * return String representing the GSM Cell ID (or "N/A" if unknown)
     */
    public String GSMCID() {
        try {
            // Convert cellIdentity to string
            String string = cellId.toString();
            // Find index of "mCid" and "mArfcn" in the string
            int mcid = string.indexOf("mCid");
            int marfcn = string.indexOf("mArfcn");
            // Extract GSM Cell ID substring from the string
            return string.substring(mcid + 5, marfcn - 1);
        } catch (Exception e) {
            System.out.println(e.toString()); // Print exception message
            return "N/A"; // Return N/A if an exception occurs
        }
    }

    /*
     * Method to extract WCDMA Cell ID from cellIdentity.
     *
     * return String representing the WCDMA Cell ID (or "N/A" if unknown)
     */
    public String WCDMACID() {
        try {
            // Convert cellIdentity to string
            String string = cellId.toString();
            // Find index of "mCid" and "mPsc" in the string
            int mcid = string.indexOf("mCid");
            int mpsc = string.indexOf("mPsc");
            // Extract WCDMA Cell ID substring from the string
            return string.substring(mcid + 5, mpsc - 1);
        } catch (Exception e) {
            System.out.println(e.toString()); // Print exception message
            return "N/A"; // Return N/A if an exception occurs
        }
    }

    /*
     * Method to extract LTE Cell ID from cellIdentity.
     *
     * return String representing the LTE Cell ID (or "N/A" if unknown)
     */
    public String LTECID() {
        try {
            // Convert cellIdentity to string
            String string = cellId.toString();
            // Find index of "mCi" and "mPci" in the string
            int mci = string.indexOf("mCi");
            int mpci = string.indexOf("mPci");
            // Extract LTE Cell ID substring from the string
            return string.substring(mci + 4, mpci - 1);
        } catch (Exception e) {
            System.out.println(e.toString()); // Print exception message
            return "N/A"; // Return N/A if an exception occurs
        }
    }

    /*
     * Method to get current timestamp.
     *
     * return String representing the current timestamp (or "N/A" if unknown)
     */
    public String timestamp() {
        try {
            // Create SimpleDateFormat object with desired date format
            SimpleDateFormat dateForm = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            // Get current date and format it as a string
            String formattedDate = dateForm.format(new Date());
            return formattedDate; // Return formatted date string
        } catch (Exception e) {
            System.out.println(e.toString()); // Print exception message
            return "N/A"; // Return N/A if an exception occurs
        }
    }

    /*
     * Method to get the Signal-to-Noise Ratio (SNR) for LTE networks.
     *
     * return String representing the SNR (or "N/A" if unknown or not applicable)
     */
    public String SNR() {
        try {
            if ("4G".equals(network)) { // Check if network type is LTE
                String string = signalStrength.toString(); // Convert signal strength to string
                int rssnr = string.indexOf("rssnr"); // Find index of "rssnr" in the string
                int cqi = string.indexOf("cqi"); // Find index of "cqi" in the string
                // Extract SNR substring from the string and return it
                return string.substring(rssnr + 6, cqi - 1);
            } else {
                return "N/A"; // Return "N/A" if network type is not LTE
            }
        } catch (Exception e) {
            System.out.println(e.toString()); // Print exception message
            return "N/A"; // Return "N/A" if an exception occurs
        }
    }
    /*
     * Method to get the frequency band of the network.
     *
     * return String representing the frequency band (or "N/A" if unknown)
     */
    public String FrequencyBand() { //https://www.frequencycheck.com/carriers/touch-lebanon
        TaskUpdate taskUpdate = new TaskUpdate(tManager); // Initialize TaskUpdate object

        if (cellinfo instanceof CellInfoLte) { // Check if the cell info is for LTE network
            CellIdentityLte cellIdentityLte = ((CellInfoLte) cellinfo).getCellIdentity();
            int bandwidth = cellIdentityLte.getBandwidth() / 1000000; // Get bandwidth in MHz
            return String.valueOf(bandwidth); // Return bandwidth as string
        } else if (cellinfo instanceof CellInfoGsm) { // Check if the cell info is for GSM network
            CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) cellinfo).getCellIdentity();
            int arfcn = cellIdentityGsm.getArfcn(); // Get Absolute Radio Frequency Channel Number (ARFCN)
            double bandwidth = Math.round(900 + 0.2 * arfcn); // Calculate bandwidth using ARFCN formula for GSM https://www.sqimway.com/gsm_band.php
            return String.valueOf(bandwidth); // Return bandwidth as string
        } else if (cellinfo instanceof CellInfoWcdma) { // Check if the cell info is for WCDMA network
            CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) cellinfo).getCellIdentity();
            int uarfcn = cellIdentityWcdma.getUarfcn(); // Get UTRA Absolute Radio Frequency Channel Number (UARFCN)
            double bandwidth = Math.round(0.2 * uarfcn); // Calculate bandwidth using UARFCN formula for WCDMA //https://www.sqimway.com/umts_band.php
            return String.valueOf(bandwidth); // Return bandwidth as string
        } else {
            return "N/A"; // Return "N/A" if network type is unknown
        }
    }


    /*
     * Method to get the signal power in dBm for 2g 3g 4g.
     *
     * return String representing the signal power in dBm (or "N/A" if unknown)
     */
    public String PowerdB() {
        TaskUpdate taskUpdate = new TaskUpdate(tManager); // Initialize TaskUpdate object

        try {
            // Print debug information
            System.out.println("WE ARE HERE");
            System.out.println(cellinfo);

            // Determine network type and return corresponding signal power value
            switch (network) {
                case "2G":
                    return String.valueOf(strengthGsm.getDbm());
                case "3G":
                    return String.valueOf(strengthWcdma.getDbm());
                case "4G":
                    return String.valueOf(strengthLte.getDbm());
                default:
                    return "N/A"; // Return "N/A" if network type is unknown
            }
        } catch (Exception e) {
            System.out.println(e.toString()); // Print exception message
            return "N/A"; // Return "N/A" if an exception occurs
        }
    }
    /*
     * Method to get the MAC address of the device.
     *
     * return String representing the MAC address (or "N/A" if not available)
     */
    private String macAddr() { // Between Android 6.0 (API level 23) and Android 9 (API level 28), local device MAC addresses will return 02:00:00:00:00:00.
        try {                     //Devices running Android 10 (API level 29) and higher report randomized MAC addresses to all apps that aren't device owner apps.
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] Bytes = nif.getHardwareAddress();
                System.out.println(Bytes);
                if (Bytes == null) {
                    return "N/A";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : Bytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString(); //return MAC Address string
            }
        } catch (Exception ex) {
            return "02:00:00:00:00:00";// if exception return
        }
        return "02:00:00:00:00:00"; // if MAC address is not found/security constraints
    }






}
