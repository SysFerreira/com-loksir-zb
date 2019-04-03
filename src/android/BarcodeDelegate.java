package com.loksir.zb;

import android.util.Log;

// zebra
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

// Cordova
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;


public class BarcodeDelegate implements IDcsSdkApiDelegate {
    public static final String TAG = ">>>ZB>>>Delegate";
    
//SDKHandler sdk, 
    public BarcodeDelegate(){
        //this.sdkHandler = sdk;  
		//this.callbackContext = callbackContext;
        Log.d(TAG, "Delegate constructor made!");
    }


    @Override
    public void dcssdkEventBinaryData(byte[] bytes, int i) {
        //
        Log.d(TAG, "Binary Data.");
    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {
        //
        Log.d(TAG, "FirmwareUpdate");
    }

    @Override
    public void dcssdkEventImage(byte[] bytes, int i) {
        //
        Log.d(TAG, "Image");
    }

    @Override
    public void dcssdkEventVideo(byte[] bytes, int i) {
        Log.d(TAG, "Video");
    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo dcsScannerInfo) {
        //
        Log.d(TAG, "CommunicationSessionEstablished");
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int i) {
        //
        Log.d(TAG, "CommunicationSessionTerminated");
    }

    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo dcsScannerInfo) {
        //
        Log.d(TAG, "Scanner Appeared");
    }

    @Override
    public void dcssdkEventScannerDisappeared(int i) {
        //
        Log.d(TAG, "ScannerDisappeared");
    }

    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID) {
        //
        String s = new String(barcodeData);
        Log.d(TAG, "barcode event data: "+ s);
		//PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
		//r.setKeepCallback(true);
		//callbackContext.sendPluginResult(r);

        if (zb.mBarcodeContext != null) {
            PluginResult r = new PluginResult(PluginResult.Status.OK, s);
            r.setKeepCallback(true);
            zb.mBarcodeContext.sendPluginResult(r);
        }
		
        /*
		Log.d(TAG, "Barcode: " + s);
        Log.d(TAG,"Barcode data is : " + barcodeData.toString());
        Log.d(TAG,"Barcode Type is : " + barcodeType);
        Log.d(TAG,"Barcode scanner id is : " + fromScannerID);
		*/

    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {
        //
        Log.d(TAG, "AuxScannerAppeared");
    }


}
