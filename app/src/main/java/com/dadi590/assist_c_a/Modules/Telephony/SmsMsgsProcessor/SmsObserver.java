/*package com.dadi590.assist_c_a.Modules.SmsMsgsProcessor;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SmsObserver extends ContentObserver {

	int smsCount;

	This is the main code (https://stackoverflow.com/questions/2735571/detecting-sms-incoming-and-outgoing)
		Uri content_uri = Uri.parse("content://sms");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			content_uri = Telephony.Sms.CONTENT_URI;
		}
		final ContentResolver contentResolver = context.getContentResolver();
		contentResolver.registerContentObserver(content_uri,true, myObserver);


	public void onChange(boolean selfChange){
		super.onChange(selfChange);
		readSms();
	}

	private void readSms(){
		Uri uriSMS = Uri.parse("content://sms");
		Cursor cur = context.getContentResolver().query(uriSMS, null, null, null, "_id");

		cur.moveToLast();
		int id = Integer.parseInt(cur.getString(cur.getColumnIndex("_id")));

		if(cur != null && id != smsCount && id>0){
			smsCount = id;

			int type = Integer.parseInt(cur.getString(cur.getColumnIndex("type")));

			if(type == Telephony.Sms.MESSAGE_TYPE_INBOX){
				// handle the received sms

			}
			There are various types. See what to do with each.
			else if (type == Telephony.Sms.MESSAGE_TYPE_){
				// handle the sent sms
			}
		}

		cur.close();

	}
}
*/
