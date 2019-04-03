package com.loksir.zb;

// Cordova
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// android
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.content.Context;
//import android.os.ParcelUuid;
//import android.bluetooth.BluetoothSocket;


// java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


//---
// zebra RFID SDK
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.BATTERY_EVENT;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RegionInfo;
import com.zebra.rfid.api3.RegulatoryConfig;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TagStorageSettings;
import com.zebra.rfid.api3.TriggerInfo;
import com.zebra.rfid.api3.SetAttribute;


//----
// barcode library
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.RMDAttributes;
import com.zebra.scannercontrol.SDKHandler;

/**
 * This class echoes a string called from JavaScript.
 */
public class zb extends CordovaPlugin {
	private static final String LOG_TAG = ">>>ZB>>>>";
	private static final String DEVICE_PREFIX = "RFD8500";
	//private static Readers      rfidReaders; // root java class of zebra sdk
	//private static ReaderDevice rfidDevice;
	private RFIDReader rfidReader;
	private TriggerInfo mTriggerInfo;

	private static boolean mConnected = false;
	private static boolean mListen = false;
	//private static boolean mPause     = true;	
	private MyEventHandler mEventHandler = null;

	private static CallbackContext mBatteryContext = null;
	private static boolean mBatteryInProgress = false;

	private static CallbackContext mInventoryContext = null;
	private static boolean mInventoryInProgress = false;
	private static boolean mInventoryStarted = false;

	// unique tags
	private static HashMap<String, Integer> mTags = new HashMap<String, Integer>();
	private Integer mTagCount = 0;

	
	// barcode related
	private boolean mBarcodeMode = false;	
    private SDKHandler sdkHandler;
	private BarcodeDelegate barcodeDelegate;
    private Integer scannerID=null;
	private boolean mBarcodeConnected = false;
	public static CallbackContext mBarcodeContext = null;

	private static Integer mTransmitPower = 270;
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("coolMethod")) {
			String message = args.getString(0);
			Log.d(LOG_TAG, "coolMethod>>>>>>>>");
			Log.d(LOG_TAG, args.toString());
			this.coolMethod(message, callbackContext);
			return true;
		}

		if (action.equals("init")) {
			Log.d(LOG_TAG, "*** init: value=" + args.getString(0));
			String message = args.getString(0);
			this.zb_init(message, callbackContext);			
			return true;
		}

		if (action.equals("connect")) {
			Log.d(LOG_TAG, "*** connect: value=" + args.getString(0));
			String message = args.getString(0);
			this.zb_connect(message, callbackContext);
			return true;
		}

		if (action.equals("listen")) {
			Log.d(LOG_TAG, "*** listen: value=" + args.getString(0));
			String message = args.getString(0);
			this.zb_listen(message, callbackContext);
			return true;
		}

		if (action.equals("beep")) {
			Log.d(LOG_TAG, "*** beep: value=" + args.getString(0));
			String message = args.getString(0);
			this.zb_beep(message, callbackContext);
			return true;
		}
		if (action.equals("battery")) {
			Log.d(LOG_TAG, "*** battery: value=" + args.getString(0));
			String message = args.getString(0);
			this.zb_battery(message, callbackContext);
			return true;
		}


		if (action.equals("inventory")) {
			Log.d(LOG_TAG, "*** inventory: value=" + args.getString(0));
			String message = args.getString(0);
			this.zb_inventory(message, callbackContext);
			return true;
		}


		/*if (action.equals("info")) {
			Log.d(LOG_TAG, "*** get info of scanner ***");
			this.zb_info(callbackContext);
			return true;
		}
		*/
		
		if (action.equals("barcode_init")) {
			String message = args.getString(0);
			this.zb_barcode_init(message, callbackContext);	
			return true;
		}
		if (action.equals("barcode_connect")) {
			String message = args.getString(0);
			this.zb_barcode_connect(message, callbackContext);			
			return true;
		}
		if (action.equals("barcode_trigger")) {
			String message = args.getString(0);
			this.zb_barcode_trigger(message, callbackContext);			
			return true;
		}
		if (action.equals("barcode_mode")) {
			String message = args.getString(0);
			this.zb_barcode_mode(message, callbackContext);			
			return true;
		}
		if (action.equals("antenna_getPower")) {
			String message = args.getString(0);
			this.zb_antenna_getPower(message, callbackContext);
			return true;
		}
		if (action.equals("antenna_setPower")) {
			String message = args.getString(0);
			this.zb_antenna_setPower(message, callbackContext);
			return true;
		}

		
		return false;
	}

	/* -------------------------------------
	   implementation of each action 
	   ----------------------------------------
	*/
	private void coolMethod(String message, CallbackContext callbackContext) {
		if (message != null && message.length() > 0) {
			callbackContext.success(message);
		} else {
			callbackContext.error("Expected one non-empty string argument.");
		}
	} // coolMethod


	// ---------------------------------------------------------
	private void zb_init(String p_scannerid, final CallbackContext callbackContext) {
		//if (p_rfid == null) { callbackContext.error("Not enough parameter"); return; }		
		//if (p_enable.matches("[^01]*")) {callbackContext.error("Parameter should be 0 or 1"); return;}	
		final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		//final UUID SPP_UUID1 = UUID.fromString("2ad8a392-0e49-e52c-a6d2-60834c012263");
		//final UUID SPP_UUID2 = UUID.fromString("7B4239C4-12D0-406E-B99C-2D6E6A02D6CF");
		//final String p_scanner = "RFD850017355523020269";
		final String p_scanner = "RFD8500";

		if (rfidReader != null) {
			callbackContext.error("Scanner already init");
			return;
		}
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
					if (bta == null)
						throw new Exception("Blue-tooth adapter is not available");
					if (!bta.isEnabled())
						throw new Exception("Blue-tooth is disabled, check configuration.");
					Set<BluetoothDevice> btPairedDevices = bta.getBondedDevices();
					if (btPairedDevices == null)
						throw new Exception("No paired Blue-tooth device found.");
					if (btPairedDevices.size() == 0)
						throw new Exception("Paired devices list is empty.");
					Readers rfidReaders = new Readers();
					ArrayList<ReaderDevice> aList = rfidReaders.GetAvailableRFIDReaderList();
					for (ReaderDevice d : aList) {
						if (d.getName().startsWith(p_scanner)) {
							rfidReader = d.getRFIDReader();  // root class to interface and performing all operations with RFID reader						
							break;
						}
					}
					if (rfidReader == null)
						throw new Exception("RFD8500 not found!");
					Log.d(LOG_TAG, "+++++ init success");
					callbackContext.success("RFD8500 founded.");
				} catch (Exception e) {
					String s = "Init:Exception: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
					return;
				}				
			} // run
		}); // threadPool	
	} // zb_init

	
	
	
	// ---------------------------------------------------------
	private void zb_connect(final String p_enable, final CallbackContext callbackContext) {
		if (p_enable == null) {
			callbackContext.error("Not enough parameter");
			return;
		}
		if (p_enable.matches("[^01]*")) {
			callbackContext.error("Parameter should be 0 or 1");
			return;
		}
		if (rfidReader == null) {
			callbackContext.error("Scanner not yet init");
			return;
		}
		if ( p_enable.equals("1") && mConnected) {
			callbackContext.error("Already connected");
			return;
		}
		if ( p_enable.equals("0")  && !mConnected) {
			callbackContext.error("Already disconnected");
			return;
		}		
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					if (p_enable.equals("1")) {
						Log.d(LOG_TAG, "+++++Before Connect");
						rfidReader.connect();
						mConnected = true;
						callbackContext.success("Scanner connected");
					}
					if (p_enable.equals("0")) {
						Log.d(LOG_TAG, "+++++Before Disconnect");
						rfidReader.disconnect();
						mConnected = false;
						callbackContext.success("Scanner disconnected");
					}
					return;
				} catch (InvalidUsageException e) {
					String s = "Connect:Invalid Usage:" + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
					return;
				} catch (OperationFailureException e) {
					String s = "Connect:Operation Failure:" + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
					return;
				}
			} // run
		});    //threadPool
	} // zb_connect

	// ---------------------------------------------------------
	private void zb_listen(final String p_enable, final CallbackContext callbackContext) {
		if (p_enable == null) {
			callbackContext.error("Not enough parameter");
			return;
		}
		if (p_enable.matches("[^01]*")) {
			callbackContext.error("Parameter should be 0 or 1");
			return;
		}
		if (rfidReader == null) {
			callbackContext.error("Scanner not yet init");
			return;
		}
		if (!mConnected) {
			callbackContext.error("Scanner not yet connect");
			return;
		}
		
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					if (p_enable.equals("0")) {
						if (!mListen)
							throw new Exception("Listen already turn off!");
						if (mEventHandler == null)
							throw new Exception("Listen handler is null, but want to turn off again!");
						Log.d(LOG_TAG, "Try to stop listening");
						rfidReader.Events.removeEventsListener(mEventHandler);
						mEventHandler = null;
						mListen = false;

						callbackContext.success("Listening is stopped");
						return;
					}
					if (p_enable.equals("1")) {
						if (mListen)
							throw new Exception("Listen already turn on!");
						if (mEventHandler != null)
							throw new Exception("Listen handler is not null, but want to turn on again!");
						// register event
						Log.d(LOG_TAG, "Try to start listening");
						mEventHandler = new MyEventHandler();
						rfidReader.Events.addEventsListener(mEventHandler);
						mListen = true;

						// configure
						rfidReader.Config.getDeviceStatus(true, false, false); // battery, power, temperature
						// subscribe
						rfidReader.Events.setInventoryStartEvent(true);
						rfidReader.Events.setInventoryStopEvent(true);
						rfidReader.Events.setHandheldEvent(true);
						rfidReader.Events.setTagReadEvent(true);
						rfidReader.Events.setBatchModeEvent(true);
						rfidReader.Events.setReaderDisconnectEvent(true);
						/*
						TagStorageSettings tss = rfidReader.Config.getTagStorageSettings();
						tss.setTagFields(TAG_FIELD.PHASE_INFO);
						tss.setTagFields(TAG_FIELD.PEAK_RSSI);
						tss.setTagFields();
						set tag storage settings on the reader with all fields
						tss.setTagFields(TAG_FIELD.ALL_TAG_FIELDS);
						rfidReader.Config.setTagStorageSettings(tss);
						*/

						//rfidReader.Events.setBatteryEvent(true);

						callbackContext.success("Listening is started");
						//return;
					}
				} catch (InvalidUsageException e) {
					String s = "Listen:InvalidUsage: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (OperationFailureException e) {
					String s = "Listen:Operation Failure: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (Exception e) {
					String s = "Listen:Other Exception: " + e.getMessage();
					Log.e(LOG_TAG, s);
					callbackContext.error(s);
				}
			} // run
		}); // threadPool	
	} // zb_listen


	// ---------------------------------------------------------
	private void zb_beep(final String p_mode, final CallbackContext callbackContext) {
		if (p_mode == null) {
			callbackContext.error("Not enough parameter");
			return;
		}
		if (rfidReader == null) {
			callbackContext.error("Scanner not yet init");
			return;
		}
		if (!mConnected) {
			callbackContext.error("Scanner not yet connect");
			return;
		}
		//if (!mListen)
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					if (p_mode.equalsIgnoreCase("QUIET"))
						rfidReader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
					else if (p_mode.equalsIgnoreCase("LOW"))
						rfidReader.Config.setBeeperVolume(BEEPER_VOLUME.LOW_BEEP);
					else if (p_mode.equalsIgnoreCase("MEDIUM"))
						rfidReader.Config.setBeeperVolume(BEEPER_VOLUME.MEDIUM_BEEP);
					else if (p_mode.equalsIgnoreCase("HIGH"))
						rfidReader.Config.setBeeperVolume(BEEPER_VOLUME.HIGH_BEEP);
					else
						throw new Exception("Parameter value invalid. Should be quiet, low, medium, high");
					callbackContext.success("Beeper volume set");
				} catch (InvalidUsageException e) {
					String s = "Beep:InvalidUsage: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (OperationFailureException e) {
					String s = "Beep:Operation Failure: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (Exception e) {
					String s = "Beep:Other Exception: " + e.getMessage();
					Log.e(LOG_TAG, s);
					callbackContext.error(s);
				}
			} // run()
		}); //threadpool
	} // zb_beep

	// ---------------------------------------------------------
	private void zb_battery(final String p_mode, final CallbackContext callbackContext) {
		//if (p_mode == null) { callbackContext.error("Not enough parameter"); return; }
		//if (p_enable.matches("[^01]*")) {callbackContext.error("Parameter should be 0 or 1"); return;}
		if (rfidReader == null) {
			callbackContext.error("Scanner not yet init");
			return;
		}
		if (!mConnected) {
			callbackContext.error("Scanner not yet connect");
			return;
		}
		if (!mListen) {
			callbackContext.error("Listening not turn on");
			return;
		}
		// if mBatteryInProgress , no need to check, can invoke many time. Better to make a timer to automatic query scanner battery status.
		// if mBatteryContext
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					rfidReader.Config.getDeviceStatus(true, false, false); // battery, power, temperature
					rfidReader.Events.setBatteryEvent(true);  // will immediate trigger a event.

					mBatteryContext = callbackContext; // store the context and wait for event come back.
					mBatteryInProgress = true;
					PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
					r.setKeepCallback(true);
					callbackContext.sendPluginResult(r);
				} catch (InvalidUsageException e) {
					String s = "Battery:InvalidUsage: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (OperationFailureException e) {
					String s = "Battery:Operation Failure: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (Exception e) {
					String s = "Battery:Other Exception: " + e.getMessage();
					Log.e(LOG_TAG, s);
					callbackContext.error(s);
				}
			} // run()
		}); //threadpool
	} // zb_battery


	// ---------------------------------------------------------
	private void zb_inventory(final String p_enable, final CallbackContext callbackContext) {
		if (p_enable == null) {
			callbackContext.error("Not enough parameter");
			return;
		}
		if (p_enable.matches("[^01]*")) {
			callbackContext.error("Parameter should be 0 or 1");
			return;
		}
		if (rfidReader == null) {
			callbackContext.error("Scanner not yet init");
			return;
		}
		if (!mConnected) {
			callbackContext.error("Scanner not yet connect");
			return;
		}
		if (!mListen) {
			callbackContext.error("Listening not turn on");
			return;
		}

		if (p_enable.equals("1") && mInventoryContext != null) {
			callbackContext.error("Inventory already enabled");
			return;
		}
		if (p_enable.equals("0") && mInventoryContext == null) {
			callbackContext.error("Inventory already disabled");
			return;
		}

		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					// perform simple inventory
					// Keep getting tags in the eventReadNotify event if registered
					if (p_enable.equals("1")) {
						rfidReader.Config.setStartTrigger(mTriggerInfo.StartTrigger);
						rfidReader.Config.setStopTrigger(mTriggerInfo.StopTrigger);

						// rfidReader.Actions.Inventory.perform();						
						mInventoryContext = callbackContext;
						mInventoryInProgress = true;
						mTags.clear(); mTagCount=0;
						PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
						r.setKeepCallback(true);
						callbackContext.sendPluginResult(r);
					}
					if (p_enable.equals("0")) // stop the inventory
					{
						rfidReader.Actions.Inventory.stop();
						mInventoryContext = null;
						mInventoryInProgress = false;
						mTags.clear(); mTagCount=0;

// may be need to wait....						
						callbackContext.success("Inventory disabled");
					}
				} catch (InvalidUsageException e) {
					String s = "Inventory:InvalidUsage: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (OperationFailureException e) {
					String s = "Inventory:Operation Failure: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				}

			} // run
		}); // threadPool	
	} // zb_inventory


	// ---------------------------------------------------------
	private void zb_info(final CallbackContext callbackContext) {
		if (rfidReader == null) {
			callbackContext.error("Scanner not yet init");
			return;
		}
		if (!mConnected) {
			callbackContext.error("Scanner not yet connect");
			return;
		}
		// 2018.05.30 - although rfidReader has been init, how know if connected or not?				
		try {
			JSONObject jo = new JSONObject();
			jo.put("ID", rfidReader.ReaderCapabilities.ReaderID.getID());
			jo.put("ModelName", rfidReader.ReaderCapabilities.getModelName());
			// jo.put("CommunicationStandard" ,rfidReader.ReaderCapabilities.getCommunicationStandard().toString()); // seems value is null, cannot toString()
			jo.put("CountryCode", rfidReader.ReaderCapabilities.getCountryCode());
			jo.put("FirwareVersion", rfidReader.ReaderCapabilities.getFirwareVersion());
			jo.put("RSSIFilter", rfidReader.ReaderCapabilities.isRSSIFilterSupported());
			jo.put("TagEventReporting", rfidReader.ReaderCapabilities.isTagEventReportingSupported());
			jo.put("TagLocatingReporting", rfidReader.ReaderCapabilities.isTagLocationingSupported());
			//j.put("" ,rfidReader.ReaderCapabilities.);			
			Log.d(LOG_TAG, "before info callback");
			callbackContext.success(jo.toString());
			Log.d(LOG_TAG, "after info callback");
		} catch (JSONException e) {
			String s = "Info:Exception: " + e.getMessage();
			Log.d(LOG_TAG, s);
			callbackContext.error(s);
			return;
		}
	} //zb_info


	/* -------------------------------------
	   override of android state
	   ----------------------------------------
	*/
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		
		Log.d(LOG_TAG, "+++++++ cordova plugin first time initialize");
		rfidReader = null;
		mTriggerInfo = new TriggerInfo();
		mTriggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
		// report back all read tags after 1 round of inventory
		mTriggerInfo.setTagReportTrigger(0);

		mEventHandler = null;
		mConnected = false;
		mListen = false;		

		mBatteryContext = null;
		mBatteryInProgress = false;

		mInventoryContext = null;
		mInventoryInProgress = false;


	} // initialize

	@Override
	public void onResume(boolean multitasking) {
		Log.d(LOG_TAG, "onResume");
		super.onResume(multitasking);
	} // onResume

	@Override
	public void onPause(boolean multitasking) {
		Log.d(LOG_TAG, "onPause");
		super.onPause(multitasking);
	} // onPause

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "onDestroy");
		//removeMyListener();
		super.onDestroy();

	} // onDestroy


	/* -------------------------------------
	   Inner class for Zebra Event Handler - two kinds of event: Read and Status notify
	   ----------------------------------------
	*/
	private class MyEventHandler implements RfidEventsListener {
		// ..............................
		// Status Event Notification
		// ..............................
		public void eventStatusNotify(RfidStatusEvents evt) {
			//Log.d(LOG_TAG, "+++++++++Status Event notifying+++++");
			STATUS_EVENT_TYPE et = evt.StatusEventData.getStatusEventType();
			if (et == STATUS_EVENT_TYPE.BATTERY_EVENT) {
				Log.d(LOG_TAG, "++++++ battery_event Notify");
				if (evt.StatusEventData.BatteryData != null)
					try {
						JSONObject jo = new JSONObject();
						jo.put("level", evt.StatusEventData.BatteryData.getLevel());
						jo.put("charging", evt.StatusEventData.BatteryData.getCharging());
						jo.put("cause", evt.StatusEventData.BatteryData.getCause());
						Log.d(LOG_TAG, "+++Battery JSON:" + jo.toString());
						PluginResult r = new PluginResult(PluginResult.Status.OK, jo.toString());
						r.setKeepCallback(false); //
						mBatteryContext.sendPluginResult(r);
						mBatteryContext = null;
						mBatteryInProgress = false;
					} catch (JSONException e) {
						Log.d(LOG_TAG, "battery json exception");
					}
			} else if (et == STATUS_EVENT_TYPE.POWER_EVENT) {
				Log.d(LOG_TAG, "++++++ power event Notify");
			} else if (et == STATUS_EVENT_TYPE.TEMPERATURE_ALARM_EVENT) {
				Log.d(LOG_TAG, "++++++ temperature alarm event Notify");
			} else if (et == STATUS_EVENT_TYPE.OPERATION_END_SUMMARY_EVENT) {
				Log.d(LOG_TAG, "++++++ Operation end summary event Notify");
			} else if (et == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
				Log.d(LOG_TAG, "++++++ handheld_trigger_event Notify");
				if (evt.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
					Log.d(LOG_TAG, "++++++ button release Notify");
				}
				if (evt.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
					Log.d(LOG_TAG, "++++++ button pressed Notify");
					if (mInventoryInProgress) {
						if (mInventoryStarted) {
							try {
								rfidReader.Actions.Inventory.stop();
								mInventoryStarted = false;
								String ans = makeTagString("stop");
								if (ans != "")
								{
									PluginResult r = new PluginResult(PluginResult.Status.OK, ans);
									r.setKeepCallback(true);
									mInventoryContext.sendPluginResult(r);
								}
								else
								{
									mInventoryContext.error("Error encoding JSON string in stop trigger!");
								}								
							} catch (InvalidUsageException e) {
								String s = "InventoryStopNotify:InvalidUsage: " + e.getMessage();
								Log.d(LOG_TAG, s);
								mInventoryContext.error(s);
							} catch (OperationFailureException e) {
								String s = "InventoryStopNotify:Operation Failure: " + e.getMessage();
								Log.d(LOG_TAG, s);
								mInventoryContext.error(s);
							}
						} else {
							try {
								rfidReader.Actions.Inventory.perform();
								mInventoryStarted = true;
							} catch (InvalidUsageException e) {
								String s = "InventoryPerformNotify:InvalidUsage: " + e.getMessage();
								Log.d(LOG_TAG, s);
								mInventoryContext.error(s);
							} catch (OperationFailureException e) {
								String s = "InventoryPerformNotify:Operation Failure: " + e.getMessage();
								Log.d(LOG_TAG, s);
								mInventoryContext.error(s);
							}
						}
					}
				}

			} else if (et == STATUS_EVENT_TYPE.INVENTORY_START_EVENT) {
				Log.d(LOG_TAG, "++++++ inventory start event Notify");
			} else if (et == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT) {
				Log.d(LOG_TAG, "++++++ inventory stop event Notify");
			} else if (et == STATUS_EVENT_TYPE.ACCESS_START_EVENT) {
				Log.d(LOG_TAG, "++++++ Access start event Notify");
			} else if (et == STATUS_EVENT_TYPE.ACCESS_STOP_EVENT) {
				Log.d(LOG_TAG, "+++++++ Access stop event Notify");
			} else {
				Log.d(LOG_TAG, "++++++ Other events Notify");
			}
		} // statusNofity

		// ..............................
		// Read TAG data
		// ..............................
		public void eventReadNotify(RfidReadEvents evt) {
			Log.d(LOG_TAG, "+++++++ Read notifying");
			String tagId = "";
			Integer n = 0;
			// after perform(), get a list of tags from zebra, use getReadTagsEx for large tag population
			TagData[] readTags = rfidReader.Actions.getReadTags(100);
			if (readTags != null) {
				mTagCount++;
				for (int i = 0; i < readTags.length; i++) {
					tagId = readTags[i].getTagID();
					//Short seen = readTags[i].getTagSeenCount();
					//Short rssi = readTags[i].getPeakRSSI();
					if (mTags.containsKey(tagId)) {
						n = mTags.get(tagId);
						mTags.put(tagId, n + 1);
					} else {
						mTags.put(tagId, 1);
					}
				} // for loop

				// unique tags has been updated
				String ans = makeTagString("");
				if (ans != "")
				{
					PluginResult r = new PluginResult(PluginResult.Status.OK, ans);
					r.setKeepCallback(true);
					mInventoryContext.sendPluginResult(r);
				}
				else
				{
					mInventoryContext.error("Error encoding JSON string!");
				}

				
				/*
				JSONObject jo = null;
				JSONArray ja = new JSONArray();
				Iterator<Map.Entry<String, Integer>> it = mTags.entrySet().iterator();
				try {
					while (it.hasNext()) {
						Map.Entry<String, Integer> pair = it.next();
						jo = new JSONObject();
						jo.put("id", pair.getKey());
						jo.put("count", pair.getValue());
						ja.put(jo);
					}
					jo = new JSONObject();
					jo.put("count", mTagCount);
					jo.put("tags", ja);
					PluginResult r = new PluginResult(PluginResult.Status.OK, jo.toString());
					r.setKeepCallback(true);
					mInventoryContext.sendPluginResult(r);
				} catch (JSONException err) {
					Log.d(LOG_TAG, "json exception");
				}
				*/
			} // eventReadNotify
		} // inner class MyEventHandler

	}  // listener

	// ----------------------------------
	// convert mTags into json string for returning to mobile caller.
	// ----------------------------------	
	private String makeTagString(String startStop) {
		JSONObject jo = null;
		JSONArray ja = new JSONArray();
		Iterator<Map.Entry<String, Integer>> it = mTags.entrySet().iterator();
		String ans = "";
		try {
			while (it.hasNext()) {
				Map.Entry<String, Integer> pair = it.next();
				jo = new JSONObject();
				jo.put("id", pair.getKey());
				jo.put("count", pair.getValue());
				ja.put(jo);
			}
			jo = new JSONObject();
			jo.put("count", mTagCount);
			jo.put("tags", ja);
			jo.put("trigger", startStop);
			ans = jo.toString();			
		} catch (JSONException err) {
			Log.d(LOG_TAG, "json exception");
		}
		return ans;
	}
	
	
	// ----------------------------------
	//   related method for bar code
	// -----------------------------------
	
	// -------------------
    // common bar code routine to send remote command to RFD8500
    // -------------------
    private boolean abcSetOpcode(DCSSDKDefs.DCSSDK_COMMAND_OPCODE op) {
        String xmlin = "<inArgs><scannerID>"+scannerID+"</scannerID></inArgs>";
        StringBuilder xmlout = new StringBuilder();
        DCSSDKDefs.DCSSDK_RESULT r = this.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(op, xmlin, xmlout,this.scannerID);
        if (r==DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
        {
            Log.d(LOG_TAG, "remote command opcode fail!"+ xmlin);
            return false;
        }
        if (r==DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS)
        {
            Log.d(LOG_TAG, "remote command opcode success!"+xmlin);
            return true;
        }
		return false;
    }
	
    // -------------------
    // common bar code routine to send remote command to RFD8500 with action parameter (int)
    // -------------------
    private  boolean abcSetActionInteger(int value) {
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE op=DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION;
        String xmlin = "<inArgs><scannerID>"+scannerID+"</scannerID><cmdArgs><arg-int>"+value+"</arg-int></cmdArgs></inArgs>";
        Log.d(LOG_TAG, xmlin);
        StringBuilder xmlout = new StringBuilder();
        DCSSDKDefs.DCSSDK_RESULT r = this.sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(op, xmlin, xmlout,scannerID);
        if (r==DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
        {
            Log.d(LOG_TAG, "remote command action fail!"+ xmlin);
            return false;
        }
        if (r==DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS)
        {
            Log.d(LOG_TAG, "remote command action success!"+xmlin);
            return true;
        }
		return false;
    }
	
	// 2018.09.13
	// ---------------------------------------------------------
	private void zb_barcode_mode(final String p_mode, final CallbackContext callbackContext) {
		if (p_mode == null) {
			callbackContext.error("Not enough parameter");
			return;
		}
		if (p_mode.matches("[^01]*")) {
			callbackContext.error("Parameter should be 0 or 1");
			return;
		}		
		if (rfidReader == null) {
			callbackContext.error("RFID Scanner not yet init");
			return;
		}
		if (!mConnected) {
			callbackContext.error("RFID Scanner not yet connect");
			return;
		}
		if (p_mode.equals("1")) {
			if (sdkHandler == null) {
				callbackContext.error("BARCODE Scanner not yet init");
				return;			
			}
			if (barcodeDelegate == null) {
				callbackContext.error("BARCODE delegate not yet init");
				return;			
			}
			if (!mBarcodeConnected) {
				callbackContext.error("BARCODE Scanner not yet connect!");
				return;			
			}
			
		}
		
		if (p_mode.equals("1") && mBarcodeMode) {
			callbackContext.error("Scanner already in BARCODE mode");
			return;			
		}
		if (p_mode.equals("0") && (!mBarcodeMode)) {
			callbackContext.error("Scanner already in RFID mode");
			return;
		}		
		
		//if (!mListen)
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				SetAttribute attr = new SetAttribute();
				attr.setAttnum(1664);
				attr.setAtttype("B");
				if (p_mode.equals("1")) 
					attr.setAttvalue(1); //turn on, BARCODE mode
				else
					attr.setAttvalue(0); //turn off, RFID Trigger
				
				try 
				{
					rfidReader.Config.setAttribute(attr);
					if (p_mode.equals("1")) {
						mBarcodeMode = true;
						callbackContext.success("Scanner set to BARCODE mode");
					}
					else 
					{					
						mBarcodeMode = false;
						callbackContext.success("Scanner set to RFID mode");
					}					
					//throw new Exception("Parameter value invalid. Should be quiet, low, medium, high");										
				} 
				catch (InvalidUsageException e) 
				{
					String s = "Bar code Mode:InvalidUsage: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (OperationFailureException e) {
					String s = "Bar code Mode:Operation Failure: " + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				} catch (Exception e) {
					String s = "Bar code Mode:Other Exception: " + e.getMessage();
					Log.e(LOG_TAG, s);
					callbackContext.error(s);
				}
			} // run()
		}); //threadpool
	} // zb_barcode_mode

	// 2018.09.22
	// ---------------------------------------------------------
	private void zb_barcode_init(final String p_enable, final CallbackContext callbackContext) {
		if (sdkHandler != null) {
			callbackContext.error("Bar code scanner already init");
			return;
		}
		if (barcodeDelegate !=null) {
			callbackContext.error("Bar code delegate already exist!");
			return;			
		}
		//this.cordova.getActivity().runOnUiThread(new Runnable() {...}
		// instead of cordova.getThreadPool()
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					sdkHandler = new SDKHandler(cordova.getActivity().getApplicationContext());
					Log.d(LOG_TAG, "Bar code init - Fetch Context\n");
					// this.barcodeDelegate = new BarcodeDelegate(this.sdkHandler, callbackContext);
					barcodeDelegate = new BarcodeDelegate();
					Log.d(LOG_TAG, "Bar code init - Delegate\n");
					sdkHandler.dcssdkSetDelegate(barcodeDelegate);
					Log.d(LOG_TAG, "Bar code init - Delegate assigned\n");

					sdkHandler.dcssdkEnableAvailableScannersDetection(true);
					sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
					//sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
					//sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
					int n = 0;
					n |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BINARY_DATA.value
							| DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_IMAGE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_VIDEO.value
							| DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value
							| DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value
					;
					sdkHandler.dcssdkSubsribeForEvents(n);
					Log.d(LOG_TAG, "Bar code init - subscribe event\n");

					sdkHandler.dcssdkEnableAvailableScannersDetection(false);
					ArrayList<DCSScannerInfo> myScanners = new ArrayList<DCSScannerInfo>();
					myScanners.clear();
					DCSSDKDefs.DCSSDK_RESULT r = DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE;
					r = sdkHandler.dcssdkGetAvailableScannersList(myScanners);
					for (DCSScannerInfo myDevice : myScanners) {
						if (myDevice.getScannerName().startsWith(DEVICE_PREFIX)) {
							Log.d(LOG_TAG, "Barcode init: " + myDevice.getScannerName());
							Log.d(LOG_TAG, "Scanner id:" + myDevice.getScannerID());
							scannerID = myDevice.getScannerID();
							break;
						}
					}
					r = sdkHandler.dcssdkStopScanningDevices();
					callbackContext.success("Bar code init success.");
				} catch (Exception e) {
					String s = "Bar code init fail!" + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				}
			} // run
		}); //threadpool
	} // zb_barcode_init

	// ---------------------------------------------------------
	private void zb_barcode_connect(final String p_enable, final CallbackContext callbackContext) {
		if (p_enable == null) {
			callbackContext.error("Not enough parameter");
			return;
		}
		if (p_enable.matches("[^01]*")) {
			callbackContext.error("Parameter should be 0 or 1");
			return;
		}
		if (this.sdkHandler == null) {
			callbackContext.error("Bar code scanner not yet init");
			return;
		}
		if (this.barcodeDelegate == null) {
			callbackContext.error("Bar code delegate not yet init");
			return;
		}		
		if ( p_enable.equals("1") && mBarcodeConnected) {
			callbackContext.error("Bar code already connected");
			return;
		}
		if ( p_enable.equals("0")  && !mBarcodeConnected) {
			callbackContext.error("Bar code already disconnected");
			return;
		}		
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				String action="";
				if ( p_enable.equals("1")) 
					action=" connect ";
				else
					action=" disconnect ";
				
				try {
					DCSSDKDefs.DCSSDK_RESULT r = DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE;			
					
					if (p_enable.equals("1"))
						r = sdkHandler.dcssdkEstablishCommunicationSession(scannerID);	
					else
						r = sdkHandler.dcssdkTerminateCommunicationSession(scannerID);
							
					if (r==DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)						
					{
						String s = "Bar code"+action+"fail!";
						Log.d(LOG_TAG, s);
						callbackContext.error(s);						
						return;
					}
					
					if (r==DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS) {
						if ( p_enable.equals("1") )
							mBarcodeConnected = true;
						else
							mBarcodeConnected = false;
						
						
						String s = "Bar code"+action+"success!";
						Log.d(LOG_TAG, s);
						callbackContext.success(s);						
						return;
					}					
					callbackContext.error("Bar code connect other than success or failure!");
				
				} catch (Exception e) {
					String s = "Bar code connect exception raise:" + e.getMessage();
					Log.d(LOG_TAG, s);
					callbackContext.error(s);
				}
			} // run
		});    //threadPool
	} // zb_barcode_connect

	
	// ---------------------------------------------------------
	private void zb_barcode_trigger(final String p_enable, final CallbackContext callbackContext) {
		if (p_enable == null) {
			callbackContext.error("Not enough parameter");
			return;
		}
		if (p_enable.matches("[^01]*")) {
			callbackContext.error("Parameter should be 0 or 1!");
			return;
		}
		if (this.sdkHandler == null) {
			callbackContext.error("Bar code scanner not yet init!");
		}
		if (this.barcodeDelegate == null) {
			callbackContext.error("Bar code delegate not yet init!");
			return;
		}		
		
		if ( !mBarcodeConnected) {
			callbackContext.error("Bar code not yet connected!");
			return;
		}
		
		/*
		if ( p_enable.equals("0")  && !mBarcodeConnected) {
			callbackContext.error("Bar code already disconnected");
			return;
		}		
		*/
		
    	cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				boolean ans=false;					
				if ( p_enable.equals("1") ) {
					if (abcSetOpcode(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_ENABLE)) {
						mBarcodeContext = callbackContext;
						PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
						r.setKeepCallback(true);
						callbackContext.sendPluginResult(r);						
					}
					else {
						callbackContext.error("Bar code Trigger turn on fail!");
					}
					return;					
				}
				else
				{
					if (abcSetOpcode(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_DISABLE)) {
						mBarcodeContext = null;
						callbackContext.success("Bar code Trigger turn off success!");
					}
					else {
						callbackContext.error("Bar code Trigger turn off fail!");
					}
					return;						
				}
			} // run
		});    //threadPool
	} // zb_barcode_trigger
	

// ---------------------------------------------------------
	private void zb_antenna_setPower(final String p_message, final CallbackContext callbackContext) {
		// mTransmitPower : default value of RF8500 is 270
		try
		{
			mTransmitPower = Integer.parseInt(p_message);
		}
		catch (NumberFormatException e)
		{
			callbackContext.error("Invalid string! Integers 0 - 300 only.");
			return;
		}
		if ( mTransmitPower<0 || mTransmitPower>300)
		{
			callbackContext.error("Invalid range! Integers 0 - 300 only."); return;
		}
		if (rfidReader == null) {
			callbackContext.error("Scanner not yet init"); return;
		}
		if (!mConnected) {
			callbackContext.error("Scanner not yet connect"); return;
		}
		
    	cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try 
				{
					Antennas.AntennaRfConfig arfc = rfidReader.Config.Antennas.getAntennaRfConfig(1);
					arfc.setTransmitPowerIndex(mTransmitPower);
					rfidReader.Config.Antennas.setAntennaRfConfig(1,arfc);
					callbackContext.success("Antenna power set success!");	return;
				}
				catch (InvalidUsageException e) { Log.d(">>>",". Antenna - InvalidUsage");}
				catch (OperationFailureException e) { Log.d(">>>",". Antenna - Operation Failure"); }
				callbackContext.error("Antenna power set fail!"); return;
			} // run
		});    //threadPool
	} // zb_antenna_setPower

	// ---------------------------------------------------------
	private void zb_antenna_getPower(final String p_message, final CallbackContext callbackContext) {
		if (rfidReader == null) {
			callbackContext.error("Scanner not yet init"); return;
		}
		if (!mConnected) {
			callbackContext.error("Scanner not yet connect"); return;
		}
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try
				{
					Antennas.AntennaRfConfig arfc = rfidReader.Config.Antennas.getAntennaRfConfig(1);
					String s = String.valueOf(arfc.getTransmitPowerIndex());
					callbackContext.success(s);	return;
				}
				catch (InvalidUsageException e) { Log.d(">>>",". Antenna - InvalidUsage");}
				catch (OperationFailureException e) { Log.d(">>>",". Antenna - Operation Failure"); }
				callbackContext.error("Antenna power get fail!"); return;
			} // run
		});    //threadPool
	} // zb_antenna_getPower



} // plugin