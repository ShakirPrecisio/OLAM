package com.kemar.olam.bluetooth_printer.inter;

import android.content.Context;
import android.content.Intent;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ScannerInterface {

	public static final String KEY_BARCODE_ENABLESCANNER_ACTION = "android.intent.action.BARCODESCAN";
	public static final String KEY_BARCODE_STARTSCAN_ACTION = "android.intent.action.BARCODESTARTSCAN";
	public static final String KEY_BARCODE_STOPSCAN_ACTION = "android.intent.action.BARCODESTOPSCAN";

	public static final String KEY_LOCK_SCAN_ACTION = "android.intent.action.BARCODELOCKSCANKEY";
	public static final String KEY_UNLOCK_SCAN_ACTION = "android.intent.action.BARCODEUNLOCKSCANKEY";
	public static final String KEY_BEEP_ACTION = "android.intent.action.BEEP";
	public static final String KEY_FAILUREBEEP_ACTION = "android.intent.action.FAILUREBEEP";
	public static final String KEY_VIBRATE_ACTION = "android.intent.action.VIBRATE";
	public static final String KEY_OUTPUT_ACTION = "android.intent.action.BARCODEOUTPUT";
	public static final String KEY_POWER_ACTION = "android.intent.action.POWER";
	public static final String KEY_TERMINATOR_ACTION = "android.intent.TERMINATOR";

	public static final String KEY_SHOWISCANUI = "com.android.auto.iscan.show_setting_ui";

	public static final String KEY_PREFIX_ACTION = "android.intent.action.PREFIX";
	public static final String KEY_SUFFIX_ACTION = "android.intent.action.SUFFIX";
	public static final String KEY_TRIMLEFT_ACTION = "android.intent.action.TRIMLEFT";
	public static final String KEY_TRIMRIGHT_ACTION = "android.intent.action.TRIMRIGHT";
	public static final String KEY_LIGHT_ACTION = "android.intent.action.LIGHT";    
	public static final String KEY_TIMEOUT_ACTION = "android.intent.action.TIMEOUT";
	public static final String KEY_FILTERCHARACTER_ACTION = "android.intent.action.FILTERCHARACTER";
	public static final String KEY_CONTINUCESCAN_ACTION = "android.intent.action.BARCODECONTINUCESCAN";
	public static final String KEY_INTERVALTIME_ACTION = "android.intent.action.INTERVALTIME";
	public static final String KEY_RESET_ACTION = "android.intent.action.RESET";
	public static final String SCANKEY_CONFIG_ACTION = "android.intent.action.scankeyConfig";
	
	public static final String KEY_FAILUREBROADCAST_ACTION = "android.intent.action.FAILUREBROADCAST";
	

	private Context mContext;
	private static ScannerInterface androidjni;

	public ScannerInterface(Context context) {
		mContext = context;

	}

	public void ShowUI(){	
		if(mContext != null){
			Intent intent = new Intent(KEY_SHOWISCANUI);
			mContext.sendBroadcast(intent);
		}
	}

	public void open(){	
		if(mContext != null){
			Intent intent = new Intent(KEY_BARCODE_ENABLESCANNER_ACTION);
			intent.putExtra(KEY_BARCODE_ENABLESCANNER_ACTION, true);
			mContext.sendBroadcast(intent);
		}
	}

	public void  close(){
		if(mContext != null){
			Intent intent = new Intent(KEY_BARCODE_ENABLESCANNER_ACTION);
			intent.putExtra(KEY_BARCODE_ENABLESCANNER_ACTION, false);
			mContext.sendBroadcast(intent);
		}

	}

	public void  scan_start(){

		if(mContext != null){
			Intent intent = new Intent(KEY_BARCODE_STARTSCAN_ACTION);
			mContext.sendBroadcast(intent);
		}
	}

	public void scan_stop(){
		if(mContext != null){
			Intent intent = new Intent(KEY_BARCODE_STOPSCAN_ACTION);
			mContext.sendBroadcast(intent);
		}
	}

	public void  lockScanKey(){
		if(mContext != null){
			Intent intent = new Intent(KEY_LOCK_SCAN_ACTION);
			mContext.sendBroadcast(intent);
		}
	}

	public void unlockScanKey(){
		if(mContext != null){
			Intent intent = new Intent(KEY_UNLOCK_SCAN_ACTION);
			mContext.sendBroadcast(intent);
		}
	}

	/**KEY_OUTPUT_ACTION<br />
	 * mode 0: Keyboard output<br />
	 * mode 1: Broadcast with action "android.intent.action.SCANRESULT"<br /><br />
        onReceive(Context context, Intent arg1) {<br />
            String  barocode = arg1.getStringExtra("value");<br />
            int barocodelen = arg1.getIntExtra("length",0);<br />
	 		String type = arg1.getStringExtra("type");<br />
	 		byte[] data = arg1.getByteArrayExtra("bytedata");
		}<br />
	 */
	public void setOutputMode(int mode){
		if(mContext != null){
			Intent intent = new Intent(KEY_OUTPUT_ACTION);
			intent.putExtra(KEY_OUTPUT_ACTION, mode);
			mContext.sendBroadcast(intent);
		}
	}
	
	public void enablePlayBeep(boolean enable){
		if(mContext != null){
			Intent intent = new Intent(KEY_BEEP_ACTION);
			intent.putExtra(KEY_BEEP_ACTION, enable);
			mContext.sendBroadcast(intent);
		}
	}
	
	public void enableFailurePlayBeep(boolean enable){
		if(mContext != null){
			Intent intent = new Intent(KEY_FAILUREBEEP_ACTION);
			intent.putExtra(KEY_FAILUREBEEP_ACTION, enable);
			mContext.sendBroadcast(intent);
		}
	}

	public void enablePlayVibrate(boolean enable){
		if(mContext != null){
			Intent intent = new Intent(KEY_VIBRATE_ACTION);
			intent.putExtra(KEY_VIBRATE_ACTION, enable);
			mContext.sendBroadcast(intent);
		}
	}

	public void enablePower(boolean enable){
		if(mContext != null){
			Intent intent = new Intent(KEY_POWER_ACTION);
			intent.putExtra(KEY_POWER_ACTION, enable);
			mContext.sendBroadcast(intent);
		}
	}

	/**  set prefix
	 * 0 <item>NONE</item>
       1 <item>ENTER</item>
       2 <item>TAB</item>
       3 <item>NEWLINE</item>*/
	public void  enableAddKeyValue(int value){
		if(mContext != null){
			Intent intent = new Intent(KEY_TERMINATOR_ACTION);
			intent.putExtra(KEY_TERMINATOR_ACTION, value);
			mContext.sendBroadcast(intent);
		}
	}

	public void addPrefix(String text){
		if(mContext != null){
			Intent intent = new Intent(KEY_PREFIX_ACTION);
			intent.putExtra(KEY_PREFIX_ACTION, text);
			mContext.sendBroadcast(intent);
		}
	}

	public void addSuffix(String text){
		if(mContext != null){
			Intent intent = new Intent(KEY_SUFFIX_ACTION);
			intent.putExtra(KEY_SUFFIX_ACTION, text);
			mContext.sendBroadcast(intent);
		}
	}

	public void interceptTrimleft(int num){
		if(mContext != null){
			Intent intent = new Intent(KEY_TRIMLEFT_ACTION);
			intent.putExtra(KEY_TRIMLEFT_ACTION, num);
			mContext.sendBroadcast(intent);
		}
	}

	public void interceptTrimright(int num){
		if(mContext != null){
			Intent intent = new Intent(KEY_TRIMRIGHT_ACTION);
			intent.putExtra(KEY_TRIMRIGHT_ACTION, num);
			mContext.sendBroadcast(intent);
		}
	}

	public void lightSet(boolean enable){
		if(mContext != null){
			Intent intent = new Intent(KEY_LIGHT_ACTION);
			intent.putExtra(KEY_LIGHT_ACTION, enable);
			mContext.sendBroadcast(intent);
		}
	}

	public void timeOutSet(int value){
		if(mContext != null){
			Intent intent = new Intent(KEY_TIMEOUT_ACTION);
			intent.putExtra(KEY_TIMEOUT_ACTION, value);
			mContext.sendBroadcast(intent);
		}
	}

	public void filterCharacter(String text){
		if(mContext != null){
			Intent intent = new Intent(KEY_FILTERCHARACTER_ACTION);
			intent.putExtra(KEY_FILTERCHARACTER_ACTION, text);
			mContext.sendBroadcast(intent);
		}
	}

	public void continuousScan (boolean enable){
		if(mContext != null){
			Intent intent = new Intent(KEY_CONTINUCESCAN_ACTION);
			intent.putExtra(KEY_CONTINUCESCAN_ACTION, enable);
			mContext.sendBroadcast(intent);
		}
	}

	public void  intervalSet(int  value){
		if(mContext != null){
			Intent intent = new Intent(KEY_INTERVALTIME_ACTION);
			intent.putExtra(KEY_INTERVALTIME_ACTION, value);
			mContext.sendBroadcast(intent);
		}
	}
    
	public void setErrorBroadCast(boolean enable){
		if(mContext != null){
			Intent intent = new Intent(KEY_FAILUREBROADCAST_ACTION);
			intent.putExtra(KEY_FAILUREBROADCAST_ACTION, enable);
			mContext.sendBroadcast(intent);
		}
	}

	public void resultScan(){	
		if(mContext != null){
			Intent intent = new Intent(KEY_RESET_ACTION);
			mContext.sendBroadcast(intent);
		}
	}

	public void scanKeySet(int keycode, int value){
		if(mContext != null){
			Intent intent = new Intent(SCANKEY_CONFIG_ACTION);
			intent.putExtra("KEYCODE", keycode);
			intent.putExtra("value", value);
			mContext.sendBroadcast(intent);
		}
	}	
}