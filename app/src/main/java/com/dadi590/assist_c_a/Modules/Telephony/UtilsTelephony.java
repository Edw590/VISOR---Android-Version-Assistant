/*
 * Copyright 2021 DADi590
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.dadi590.assist_c_a.Modules.Telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>Global telephony-related utilities.</p>
 */
public final class UtilsTelephony {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsTelephony() {
	}

	/**
	 * <p>The URI to be used to retrieve contacts from SIM cards (or how Google calls them - ICC cards, which would
	 * stand for Integrated Circuits Card).</p>
	 * <br>
	 * <p>Note: this was found on StackOverflow and seems to work on Lollipop 5.1 at least.</p>
	 * <br>
	 * <p>Started to be used on Android 1.6 (API 4), according to
	 * <a href="https://androidforums.com/threads/sim-contacts.374255/">this</a>. On Android 1.5 (API 3) it was
	 * "content://sim/adn".</p>
	 */
	private static final Uri ICC_URI_API4PLUS = Uri.parse("content://icc/adn");

	public static final int ALL_CONTACTS = 0;
	public static final int CONTACTS_SIM = 1;
	public static final String NO_MATCHES = "3234_NO_MATCHES";
	public static final String MULTIPLE_MATCHES = "3234_MULTIPLE_MATCHES";
	public static final String CONTENT_URI_ERROR = "3234_CONTENT_URI_ERROR";
	/**
	 * <p>Gets the name of a contact through its phone number.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #ALL_CONTACTS} --> for {@code location_search}: search everywhere for the contact</p>
	 * <p>- {@link #CONTACTS_SIM} --> for {@code location_search}: search only on the SIM contacts for the contact</p>
	 * <p>- {@link #NO_MATCHES} --> returned when no number was found for the given phone number</p>
	 * <p>- {@link #MULTIPLE_MATCHES} --> returned when multiple contacts were found for the given phone number</p>
	 * <p>- {@link #CONTENT_URI_ERROR} --> returned if a wrong value was used the parameter {@code location_search}</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param number the phone number of the contact
	 * @param location_search location where to look for the contact
	 *
	 * @return the name of the contact or one of the constants
	 */
	@NonNull
	public static String getNameFromNum(@NonNull final String number, final int location_search) {
		final Uri uri_to_use;
		switch (location_search) {
			case (ALL_CONTACTS):
				uri_to_use = ContactsContract.Contacts.CONTENT_URI;
				// This seems to give the contacts of the entire phone: SIM card, phone storage and accounts like Google,
				// Whatsapp...
				break;
			case (CONTACTS_SIM):
				uri_to_use = ICC_URI_API4PLUS;
				break;
			default:
				return CONTENT_URI_ERROR;
		}

		final ContentResolver contentResolver = UtilsGeneral.getContext().getContentResolver();

		int num_matches = 0;
		String last_name_found = "";
		try (final Cursor cursor = contentResolver.query(uri_to_use, null, null, null, null)) {
			final Collection<String> matches = new ArrayList<>(10); // 10 matches at most, I guess?

			// todo Put this in a thread!!! --> "Skipped 135 frames!"

			if (location_search == ALL_CONTACTS) {
				if ((cursor != null ? cursor.getCount() : 0) > 0) {
					while (cursor.moveToNext()) {
						final String id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
						final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						//System.out.println("Name: " + name);

						if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
							try (
									final Cursor cursor1 = contentResolver.query(
									ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
									null,
									ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
									new String[]{id}, null)
							) {
								while (cursor1.moveToNext()) {
									final String phoneNo = cursor1.getString(cursor1.getColumnIndex(
											ContactsContract.CommonDataKinds.Phone.NUMBER));
									//System.out.println("Number: " + phoneNo);

									if (PhoneNumberUtils.compareStrictly(number, phoneNo)) {
									/*System.out.println("---Name: " + name);
									System.out.println("---Number: " + phoneNo);*/
										boolean already_found = false;
										for (final String match : matches) {
											if (match.equals(name)) {
												already_found = true;
												break;
											}
										}
										if (!already_found) {
											matches.add(name);
											last_name_found = name;
											++num_matches;
										}
									}
								}
							}
						}
					}
				}
			} else if (location_search == CONTACTS_SIM) {
				// Leave it even being "always true" - until another location is added... Might prevent bugs.
				while (cursor.moveToNext()) {
					final String name = cursor.getString(cursor.getColumnIndex("name"));
					//listContactId.add(cursorSim.getString(cursorSim.getColumnIndex("_id")));
					final String phoneNo = cursor.getString(cursor.getColumnIndex("number"));
					/*System.out.println("Name: " + name);
					System.out.println("Phone Number: " + phoneNo);*/

					if (PhoneNumberUtils.compareStrictly(number, phoneNo)) {
						// Note: the compareStrictly() methods are only available with the hidden/internal classes showing
						/*System.out.println("---Name: " + name);
						System.out.println("---Number: " + phoneNo);*/
						boolean already_found = false;
						for (final String match : matches) {
							if (match.equals(name)) {
								already_found = true;
								break;
							}
						}
						if (!already_found) {
							matches.add(name);
							last_name_found = name;
							++num_matches;
						}
					}
				}
			}
		}

        /*System.out.println("OOOOOOOOOOOOOOOOOOO");
        System.out.println(num_matches);
        System.out.println(last_name_found);
        System.out.println("OOOOOOOOOOOOOOOOOOO");*/

		if (num_matches == 0) {
			return NO_MATCHES;
		} else if (num_matches == 1) {
			return last_name_found;
		} else {
			return MULTIPLE_MATCHES;
		}
	}

	/**
	 * <p>Checks if a number is a private number.</p>
	 *
	 * @param number the phone number got directly from the incoming call
	 *
	 * @return true if it's a private number, false otherwise
	 */
	public static boolean isPrivateNumber(@Nullable final String number) {
		// Note: looked up how the ISPs normally represent private numbers and the final code I made was this.
		// Has been detecting correctly - if the number is really private (go to Phone settings change that), this
		// says it's a private number.
		// Hasn't said a normal number is a private one, so all seems alright.

		if (number != null && !number.isEmpty()) {
			// This below is to check if it's a valid phone number, since it's neither null nor an empty string.
			// When PayPal sends a message, the "number" is "PayPal". In that case, converting to a long throws an error.
			// In that case, it's because it's some name (not a private number then).
			// If the conversion works, then check if the number is greater or equal to 0 ("Private numbers are sent to
			// the phone as -1 or -2.", according with a StackOverflow user), and in that case, it's not a private number.
			try {
				if (Long.parseLong(number) >= 0L) {
					return false;
				}
			} catch (final NumberFormatException ignored) {
				return false;
			}
		}

		return true;
	}

	/**
	 * <p>Returns what the assistant should say about the given number.</p>
	 * <br>
	 * <p>For example, for a phone number, it will separate all digits, so the assistant can spell the number. For
	 * an alphanumeric number (like PayPal), it will return a string to warn it's a alphanumeric number. Will also warn
	 * about multiple matches on the number.</p>
	 *
	 * @param number the number of the contact
	 *
	 * @return what the assistant should say
	 */
	@NonNull
	public static String getWhatToSayAboutNumber(@NonNull final String number) {
		final String ret = getNameFromNum(number, ALL_CONTACTS);
		if (ret.equals(NO_MATCHES)) {
			if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
				// In case it's a phone number, separate each digit and spell. So +351123456789 will be rewritten as
				// + 3 5 1 1 2 3 4 5 6 7 8 9. Instead of saying 23 trillions and 47 billions (lol), it will say number by
				// number. This is in case the TTS engine doesn't recognize the number pattern (Ivona for Android doesn't
				// recognize Portugal numbers, for example, but recognizes US numbers - don't care anymore then).
				return number.replace("", " ").trim();
			} else {
				// In case the "number" is "PayPal", for example, don't spell. Instead, warn about it and say the name
				// that came.
				return "an alphanumeric number: " + number;
			}
		} else if (ret.equals(MULTIPLE_MATCHES)) {
			return "Attention - Multiple matches on " + number.replace("", " ").trim();
		}

		return ret;
	}

	public static final String ERROR = "3234_ERROR_1";
	public static final String NO_CALLS_DETECTED = "3234_NO_CALLS_DETECTED";
	/**
	 * <p>Gets the number and type of last call detected on the phone.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #ERROR} --> returned if there was an error</p>
	 * <p>- {@link #NO_CALLS_DETECTED} --> returned if no calls were detected on the phone</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @return a String[] of 2 indexes: in index 0 the phone number, in index 1 one of the {@link CallLog.Calls#TYPE}s
	 * of the call. On both indexes at the same time only might be one of the constants.
	 */
	@NonNull
	public static String[] getLastCall() {
		final ContentResolver contentResolver = UtilsGeneral.getContext().getContentResolver();
		try (final Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null)) {
			if (cursor == null) {
				return new String[]{ERROR, ERROR};
			}

			if (cursor.moveToLast()) { //starts pulling logs from last - you can use moveToFirst() for first logs
				final String phNumber = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
				final String type = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
				cursor.close();

				return new String[]{phNumber, type};
			}
		}

		return new String[]{NO_CALLS_DETECTED, NO_CALLS_DETECTED};
	}

	/**
	 * <p>Gets the {@link CallLog.Calls#TYPE} of the last call of the given phone number.</p>
	 *
	 * @param number the phone number
	 *
	 * @return one of the {@link CallLog.Calls#TYPE}s of call
	 */
	public static int getTypeLastCallByNum(@NonNull final String number) {
		final ContentResolver contentResolver = UtilsGeneral.getContext().getContentResolver();
		try (final Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null)) {

			if (cursor == null) {
				return -2;
			}

			do {
				final String phNumber = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
				if (PhoneNumberUtils.compareStrictly(phNumber, number)) {
					return Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)));
				}
			} while (cursor.moveToPrevious());
		}

		return -1;
	}

	/**
	 * <p>Checks if a phone number is an emergency number.</p>
	 *
	 * @param phone_number the phone number to check
	 *
	 * @return true if it's an emergency number, false otherwise
	 */
	public static boolean isEmergencyNumber(@NonNull final String phone_number) {
		final Context context = UtilsGeneral.getContext();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			return telephonyManager.isEmergencyNumber(phone_number);
		} else {
			return PhoneNumberUtils.isEmergencyNumber(phone_number);
		}
	}
}
