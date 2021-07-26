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

package com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor;

import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.telephony.PreciseCallState;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.MainSrv.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.Telephony.UtilsTelephony;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;

/**
 * <p>Processes all phone calls made/received on the phone.</p>
 */
public class PhoneCallsProcessor {

	private final List<ArrayList<String>> calls_state = new ArrayList<>(0);

	/**
	 * <p>A map with the {@link CallLog.Calls#TYPE}s on its keys, and on its values, the corresponding CALL_PHASEs.
	 * Example:</p>
	 * <p>- mapCallLogToCALL_PHASE.put(INCOMING_TYPE, CALL_PHASE_ANSWERED)</p>
	 * <p>- mapCallLogToCALL_PHASE.put(MISSED_TYPE, CALL_PHASE_LOST)</p>
	 */
	private final LinkedHashMap<Integer, Integer> mapCallLogToCALL_PHASE;

	/**
	 * <p>Main class constructor.</p>
	 */
	public PhoneCallsProcessor() {
		mapCallLogToCALL_PHASE = new LinkedHashMap<>(2);
		mapCallLogToCALL_PHASE.put(CallLog.Calls.INCOMING_TYPE, CALL_PHASE_ANSWERED);
		mapCallLogToCALL_PHASE.put(CallLog.Calls.MISSED_TYPE, CALL_PHASE_LOST);
		// Don't have use for this below, at least for now that I have Lollipop, so can't test any of this below.
		// todo Now you can... ^^^^
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mapCallLogToCALL_PHASE.put(CallLog.Calls.REJECTED_TYPE, CALL_PHASE_REJECTED_USER);
            mapCallLogToCALL_PHASE.put(CallLog.Calls.BLOCKED_TYPE, CALL_PHASE_BLOCKED_USER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                mapCallLogToCALL_PHASE.put(CallLog.Calls.ANSWERED_EXTERNALLY_TYPE, CALL_PHASE_ANSWERED_EXTERNALLY);
            }
        }*/
	}

	/**
	 * <p>Receives the state and phone number of a call and gets it ready to be processed by
	 * {@link #whatToDo(NumAndPhase)}.</p>
	 *
	 * @param state one of the {@code CALL_STATE}s in {@link TelephonyManager} or one of the {@code PRECISE_CALL_STATE}s
	 *                 in {@link PreciseCallState}
	 * @param phoneNumber the phone number retrieved directly from the extra of the intent
	 * @param precise_call_state true if it's a {@link PreciseCallState}, false if it's a {@link TelephonyManager} call
	 *                              state
	 */
	public final void phoneNumRecv(final int state, @Nullable final String phoneNumber, final boolean precise_call_state) {
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println(phoneNumber);
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%");

		if (precise_call_state) {
			// todo There's no PRECISE_CALL_STATE_LOST... Implement that somehow
			// Also don't forget there was some deprecated thing and PhoneStateListener should be used instead. Look
			// for that in the hidden/internal APIs here.
			final NumAndPhase sub_ret = new NumAndPhase(phoneNumber, state);
			whatToDo(sub_ret);
		} else {
			final NumAndPhase[] ret = getCallPhase(state, phoneNumber);
			if (ret == null) {
				return;
			}
			for (final NumAndPhase sub_ret : ret) {
				whatToDo(sub_ret);
			}
		}
	}

	/**
	 * <p>Decides what the assistant should say to warn about the detected call.</p>
	 * @param sub_ret the sub_ret variable from {@link #PhoneCallsProcessor()}
	 */
	private static void whatToDo(final NumAndPhase sub_ret) {
		if (sub_ret == null) {
			return;
		}

		final String number = sub_ret.phone_number;
		switch (sub_ret.call_phase) {
			case (PreciseCallState.PRECISE_CALL_STATE_INCOMING):
			case (CALL_PHASE_RINGING_NEW): {
				if (UtilsTelephony.isPrivateNumber(number)) {
					final String speak = "Sir, sir, attention! Incoming call from a private number! Incoming " +
							"call from a private number!";
						UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_HIGH, null);
				} else {
					final String number_name = UtilsTelephony.getWhatToSayAboutNumber(number);
					final String speak = "Sir, sir, incoming call from " + number_name + ". Incoming call from " +
							number_name + ".";
					UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_HIGH, null);
				}
				break;
			}

			case (PreciseCallState.PRECISE_CALL_STATE_WAITING):
			case (CALL_PHASE_RINGING_WAITING): {
				if (UtilsTelephony.isPrivateNumber(number)) {
					final String speak = "Sir, sir, attention! Call waiting from a private number! Call " +
							"waiting from a private number!";
					UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_HIGH, null);
				} else {
					final String number_name = UtilsTelephony.getWhatToSayAboutNumber(number);
					final String speak = "Sir, sir, call waiting from " + number_name + ". Call waiting from " +
							number_name + ".";
					UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_HIGH, null);
				}
				break;
			}

			case (CALL_PHASE_LOST):
			case (CALL_PHASE_LOST_LATE): {
				if (UtilsTelephony.isPrivateNumber(number)) {
					final String speak = "Missed call from a private number.";
					UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_MEDIUM, null);
				} else {
					final String number_name = UtilsTelephony.getWhatToSayAboutNumber(number);
					final String speak = "Missed call from " + number_name + ".";
					UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_MEDIUM, null);
				}
				break;
			}
		}
	}

	private static final int CALL_PHASE_OUTGOING = 3234_0;
	private static final int CALL_PHASE_RINGING_NEW = 3234_1;
	private static final int CALL_PHASE_LOST = 3234_2;
	private static final int CALL_PHASE_LOST_LATE = 3234_3;
	private static final int CALL_PHASE_RINGING_WAITING = 3234_4;
	//private static final int CALL_PHASE_ON_HOLD = 3234_5;
	private static final int CALL_PHASE_ANSWERED = 3234_6;
	private static final int CALL_PHASE_ANSWERED_LATE = 3234_7;
	private static final int CALL_PHASE_FINISHED = 3234_8;
	private static final int CALL_PHASE_FINISHED_LATE = 3234_9;
	/**
	 * <p>This gets the phase of the call when a new phone state ({@link TelephonyManager}'s state) is detected.</p>
	 * <br>
	 * <p>There are constants ending in "_LATE". Those are so because they're only detected after the end of all calls
	 * are over and after the phone gets to IDLE state. Which means, they already happened some time ago (1 second,
	 * 10 minutes, unpredictable).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #CALL_PHASE_OUTGOING} --> returned in case an outgoing call was just started (whether it is answered
	 * or not by the other party - it's not possible to detect that easily)</p>
	 * <p>- {@link #CALL_PHASE_RINGING_NEW} --> returned in case it's a new incoming call</p>
	 * <p>- {@link #CALL_PHASE_LOST} --> returned in case the call has just been lost</p>
	 * <p>- {@link #CALL_PHASE_LOST_LATE} --> returned in case the call was lost some time ago already</p>
	 * <p>- {@link #CALL_PHASE_RINGING_WAITING} --> returned in case there's a new call which is waiting to be answered
	 * (some call is already active then)</p>
	 * <p>- {@link #CALL_PHASE_ANSWERED} --> returned in case the call has just been answered</p>
	 * <p>- {@link #CALL_PHASE_ANSWERED_LATE} --> returned in case the call was answered some time ago already</p>
	 * <p>- {@link #CALL_PHASE_FINISHED} --> returned in case the call was just finished (after having been answered -
	 * if it wasn't answered, it was LOST or LOST_LATE).</p>
	 * <p>- {@link #CALL_PHASE_FINISHED_LATE} --> returned in case the call was finished some time ago already (the same
	 * in parenthesis for FINISHED applies here).</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param state {@link TelephonyManager#CALL_STATE_IDLE}, {@link TelephonyManager#CALL_STATE_RINGING}, or
	 * {@link TelephonyManager#CALL_STATE_OFFHOOK}
	 * @param incomingNumber phone number that came with the state change
	 *
	 * @return a double array of Strings in which each element contains the number (index 0) and the phase call (index 1)
	 * to which the number has just changed to. There can be more than one event in a state change. The events will always
	 * be in the actual event order. If a call was lost before another was answered, then the order will be exactly that
	 */
	@Nullable
	private NumAndPhase[] getCallPhase(final int state, @Nullable final String incomingNumber) {
		final String BETTER_CALL_STATE_OUTGOING = "BETTER_CALL_STATE_OUTGOING";
		final String BETTER_CALL_STATE_INCOMING = "BETTER_CALL_STATE_INCOMING";
		final String BETTER_CALL_STATE_WAITING = "BETTER_CALL_STATE_WAITING";
		final String BETTER_CALL_STATE_DISCONNECTED = "BETTER_CALL_STATE_FINISHED";
		//final String BETTER_CALL_STATE_ON_HOLD = "BETTER_CALL_STATE_ON_HOLD";
		final String BETTER_CALL_STATE_ACTIVE = "BETTER_CALL_STATE_ACTIVE";

		//--- What has been tried and for what end, but that didn't work for one or more things ---
		//        - Detect if call recording is possible or not (with or without root - works in both cases):
		//        	It's not enough, since as soon as an outgoing call is made, the recording is possible, even without
		//        	the call having been answered by the other party.
		//        	Also no good to know if the call is on hold or not, since the recording remains possible.
		//        	Between hang up a call and answering another waiting to be answered, the recording is also possible,
		//        	so also no good for that.
		//        	Basically this appears to be "recording_possible = (callState == OFFHOOK)".
		//        	Sum up: no good for anything.

		switch (state) {
			case (CALL_STATE_RINGING): {
				//System.out.println("RINGING - " + incomingNumber);

				// New incoming call (there are no calls in the current processing call list).
				if (calls_state.isEmpty()) { // Ou seja, estava em IDLE.
					final ArrayList<String> arrayList = new ArrayList<>(0);
					arrayList.add(incomingNumber);
					arrayList.add(BETTER_CALL_STATE_INCOMING);
					calls_state.add(arrayList);

					System.out.println(CALL_PHASE_RINGING_NEW + " -> " + incomingNumber);
					return new NumAndPhase[]{new NumAndPhase(incomingNumber, CALL_PHASE_RINGING_NEW)};
				} else {
					// New incoming call waiting
					final int calls_state_size = calls_state.size();
					for (int i = 0; i < calls_state_size; i++) {
						if (calls_state.get(i).get(1).equals(BETTER_CALL_STATE_ACTIVE)) {
							// If any call was already active and another one came, then that other one is waiting to be
							// answered.
							// This also works with 3 calls, even on case 8, since the state of the 1st call only changes
							// on IDLE. Until then it remains ACTIVE, even having been already disconnected (don't know
							// a way to detect it was disconnected).
							final ArrayList<String> arrayList = new ArrayList<>(0);
							arrayList.add(incomingNumber);
							arrayList.add(BETTER_CALL_STATE_WAITING);
							calls_state.add(arrayList);

							System.out.println(CALL_PHASE_RINGING_WAITING + " -> " + incomingNumber);
							return new NumAndPhase[]{new NumAndPhase(incomingNumber, CALL_PHASE_RINGING_WAITING)};
							//break;
						}
					}
				}

				break;
			}

			case (CALL_STATE_OFFHOOK): {
				//System.out.println("OFFHOOK - " + incomingNumber);
				NumAndPhase to_return = null;
                /*if (calls_state.size() == 0) {
                    // If there are no calls in processing (for example, the app was started with at least one call
                    // already in course), abort and do nothing at all.
                    // EDIT: Can't have this here... Or it won't detect an outgoing call, which puts the phone in this
                    // state in the beginning.
                    break;
                }*/

				// Check if it's an outgoing call.
				if (calls_state.isEmpty()) { // Which means, was in IDLE.
					final ArrayList<String> arrayList = new ArrayList<>(0);
					arrayList.add(incomingNumber);
					arrayList.add(BETTER_CALL_STATE_OUTGOING);
					calls_state.add(arrayList);

					System.out.println(CALL_PHASE_OUTGOING + " -> " + incomingNumber);
					to_return = new NumAndPhase(incomingNumber, CALL_PHASE_OUTGOING);
				} else {
					// Check if the 1st or only call was answered.
					final int calls_state_size = calls_state.size();
					for (int i = 0; i < calls_state_size; i++) {
						if (PhoneNumberUtils.compareStrictly(calls_state.get(i).get(0), incomingNumber)) {
							if (calls_state.get(i).get(1).equals(BETTER_CALL_STATE_INCOMING)) {
								// If the number was in INCOMING (not WAITING, because I don't know how to detect a call
								// waiting that is answered)
								// and we are now in the OFFHOOK state, then the call was answered.
								calls_state.get(i).set(1, BETTER_CALL_STATE_ACTIVE);

								System.out.println(CALL_PHASE_ANSWERED + " -> " + incomingNumber);
								return new NumAndPhase[]{new NumAndPhase(incomingNumber, CALL_PHASE_ANSWERED)};
							}
						}
					}
				}

				// Add the number to the list with the state OFFHOOK, or update the state in case the number is already
				// on the list. This is in case the cases above don't apply --> WAITING to OFFHOOK (don't know what to
				// do with that - can't be rejected or answered). Then in that case, I leave the state CALL_STATE_OFFHOOK
				// on the list.
				final int calls_state_size = calls_state.size();
				for (int i = 0; i < calls_state_size; i++) {
					if (PhoneNumberUtils.compareStrictly(calls_state.get(i).get(0), incomingNumber)) {
						calls_state.get(i).set(1, String.valueOf(CALL_STATE_OFFHOOK));
						break;
					}
				}
				return new NumAndPhase[]{to_return};
				//break;
			}

			case (CALL_STATE_IDLE): {
				//System.out.println("IDLE - " + incomingNumber);
				final ArrayList<NumAndPhase> final_return = new ArrayList<>(0);
				if (calls_state.isEmpty()) {
					// If there are no calls in processing (for example, the app was started with at least one cal
					// already in course), abort and do nothing at all.
					break;
				}

				System.out.println("Here:");
				int calls_state_size = calls_state.size();
				for (int i = 0; i < calls_state_size; i++) {
					System.out.println(calls_state.get(i).get(0) + " | " + calls_state.get(i).get(1));
				}

				//////////////////////////////////////
				// Beginning of the LATE events

				// We begin by the LATE events for the correct order to go inthe return array of all events, the closes
				// to reality possible.

				// Below is the handling of all numbers that didn't came with the state IDLE. Only one can come with
				// the state and is the one for which the call was finished right now or lost right now. The other would
				// get no treatment. Therefore, this tried to understand what may have happened. All here will be of the
				// LATE type because of exactly that (what happened --> past).

				// If it's more than a call, we can apply a "trick" to know the state of the first and of the last -
				// answered or lost. This is an example of the cases 1 and 6 (2 calls) and 7 to 10 (3 calls).
				if (calls_state.size() > 1) {
					// If the first call would have been lost, this would have gone to IDLE directly, and the list would
					// have gotten empty. Then the call coming next would be the first call again. If there is a 2nd,
					// the 1st must have been answered. And if it was answered, it was finished in some moment.

					// In case the 1st call wasn't the one that came in IDLE, then it was finished some time ago already.
					if (!calls_state.get(0).get(0).equals(incomingNumber)) {
						// In 2 calls, if the 2nd comes on IDLE, then it means the 1st one was already finished a some
						// time ago (because, again, if the 1st wasn't answered, there would be no 2nd). And this sets
						// that state in the call and returns it.
						// This can be applied for 3 or more calls too. In that case, if any call not the 1st gets on
						// IDLE, the 1st was already finished some time ago.
						System.out.println(CALL_PHASE_FINISHED_LATE + " -> " + calls_state.get(0).get(0));
						final_return.add(new NumAndPhase(calls_state.get(0).get(0), CALL_PHASE_FINISHED_LATE));
						calls_state.get(0).set(1, BETTER_CALL_STATE_DISCONNECTED);
					}
				}

				calls_state_size = calls_state.size();
				for (int i = 0; i < calls_state_size; i++) {
					if (PhoneNumberUtils.compareStrictly(calls_state.get(i).get(0), incomingNumber)) {
						if (!(calls_state.get(i).get(1).equals(BETTER_CALL_STATE_INCOMING) ||
								calls_state.get(i).get(1).equals(BETTER_CALL_STATE_WAITING))) {
							// In case the call didn't come from INCOMING or WAITING, then check if it was answered some
							// time ago or not. Which means, if it wasn't detected the call was answered in the right
							// moment (so it's not in ACTIVE state)...
							if (!calls_state.get(i).get(1).equals(BETTER_CALL_STATE_ACTIVE)) {
								System.out.println(CALL_PHASE_ANSWERED_LATE + " -> " + calls_state.get(i).get(0));
								final_return.add(new NumAndPhase(calls_state.get(i).get(0), CALL_PHASE_ANSWERED_LATE));
								// ^^^^ ... then it was answered some time ago already.
							}
						}
						break;
					}
				}

				// For 3 or more calls, for all calls in the middle of the 1st and last, it's not possible to know their
				// state without, at least, a way of knowing if any ended in the middle or not. There, it would be
				// possible to know that the 2nd one was answered, for example (in the case of 3 calls). But without that,
				// there's no way of knowing.
				// So, in that case, for the remaining calls, we're forced to go to the phone's call history.
				// This unless the case 9 happens. In that case, we can know the state of the 3 calls.
				// Sum up: this is done for all calls, except the 1st and last, and the one that got to IDLE. Supposing
				// it's the 2nd in the case of 3 calls, nothing it's done. In other cases, the calls that get here, we
				// got get the state from the call history.
				// And this is done here to go in the correct order in the return array. After the handling of the 1st
				// call and before the handling of the last call. And on the LATE events.
				if (calls_state.size() >= 3) {
					calls_state_size = calls_state.size();
					for (int i = 1; i < calls_state_size -1; i++) {
						if (calls_state.get(i).get(0).equals(incomingNumber)) {
							continue;
						}

						final int type_call = UtilsTelephony.getTypeLastCallByNum(calls_state.get(i).get(0));
						if (type_call == CallLog.Calls.INCOMING_TYPE || type_call == CallLog.Calls.MISSED_TYPE) {
							System.out.println(mapCallLogToCALL_PHASE.get(type_call) + " -> " +
									calls_state.get(calls_state.size() - 1).get(0));
							final_return.add(new NumAndPhase(calls_state.get(calls_state.size() - 1).get(0),
									mapCallLogToCALL_PHASE.get(type_call)));
							// Won't put any measures here. Nothing here will happen. Will only make the code confusing
							// (more xD).
						}
						calls_state.get(calls_state.size() - 1).set(1, BETTER_CALL_STATE_DISCONNECTED);
					}
					// TODO Imagine the same person calls, gives up, calls again, I hang up who I was talking with and
					//  answered this person. This will detect the state as the last one (answered), when I didn't
					//  answered the 1st time.
					// This should be in real-time detecting the exact states from the call history if it can't get them
					// directly, but I don't have time to think on that. Anyway, I don't need all the states when the
					// happen exactly. Only missed calls in the end and incoming calls in the beginning.
				}

				if (calls_state.size() > 1 && !calls_state.get(calls_state.size() - 1).get(0).equals(incomingNumber)) {
					// The detection of the last call, in case it was lost some time ago, works both for 2 as for any
					// other superior number of calls.
					// For only one call it's not necessary, because on that case, we know exactly when it's lost.

					// PS: The call that got to IDLE is never LOST_LATE - either lost right now or finished right now.

					// If the last call on the list got the OFFHOOK state (which means, without knowing if it was
					// answered or lost in the right moment), and didn't get here on IDLE, then by the cases 1 and 6 for
					// 2 calls, and by the cases 7 to 10 for 3 calls, it wasn't answered either, or it would have been
					// that call getting to IDLE itself.
					// Getting the 1st one here on IDLE in the case of 2 calls or any other call in the case of 3 calls,
					// then this last one was lost some time ago.
					if (calls_state.get(calls_state.size() - 1).get(1).equals(String.valueOf(CALL_STATE_OFFHOOK))) {

						System.out.println(CALL_PHASE_LOST_LATE + " -> " + calls_state.get(calls_state.size() - 1).get(0));
						final_return.add(new NumAndPhase(calls_state.get(
								MainSrv.getPhoneCallsProcessor().calls_state.size() - 1).get(0), CALL_PHASE_LOST_LATE));
						calls_state.get(calls_state.size() - 1).set(1, BETTER_CALL_STATE_DISCONNECTED);
					}
				}

				// End of LATE events
				//////////////////////////////////////

				// Now processing of the immediate events, so they get all in order (the late ones happened before the
				// immediate ones).
				calls_state_size = calls_state.size();
				for (int i = 0; i < calls_state_size; i++) {
					if (PhoneNumberUtils.compareStrictly(calls_state.get(i).get(0), incomingNumber)) {
						if (calls_state.get(i).get(1).equals(BETTER_CALL_STATE_INCOMING) ||
								calls_state.get(i).get(1).equals(BETTER_CALL_STATE_WAITING)) {
							// If it came directly from INCOMING or WAITING states to IDLE, then the call was lost right now.
							System.out.println(CALL_PHASE_LOST + " -> " + incomingNumber);
							final_return.add(new NumAndPhase(incomingNumber, CALL_PHASE_LOST));
						} else {
							// If the state is not INCOMING or WAITING, this in case will be OFFHOOK or ANSWERED. Which
							// means, the call was finished right now (means was alreaedy answered some ago - or would
							// have been lost).
							// In no case where a call goes from OFFHOOK to IDLE means the call was lost some time ago,
							// from the testing. So the only option is the call having been finished, or lost, right now
							// (which is handled on the above IF).
							System.out.println(CALL_PHASE_FINISHED + " -> " + incomingNumber);
							final_return.add(new NumAndPhase(incomingNumber, CALL_PHASE_FINISHED));
						}
						calls_state.get(i).set(1, BETTER_CALL_STATE_DISCONNECTED);
						break;
					}
				}

				calls_state.clear();
				return final_return.toArray(new NumAndPhase[15]); // 15 things at most, hopefully
				//break;
			}
		}
		return null;
	}
}
