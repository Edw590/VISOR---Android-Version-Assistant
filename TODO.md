# To-do/To-fix/To-improve/To-everything list

## Preferences on system partition and recognition of system wipe

Save EVERYTHING on the system partition, so that even if someone formats the phone, the assistant is there like nothing
happened at all. Useful for stolen phones, for example - the assistant will detect a wipe and will notify the emergency
phone number and emails stored on the preferences or something.


## Permissions

Missing to put the assistant asking the permissions in case it wants to execute something that needs the permission and
doesn't have it. This also means which is to remove all permissions periodically and see if the asistant asks them and
doesn't crash if it doesn't have them.


## Alarm system

Missing making the alarm system...

Make it a smart alarm system. If you're leaving home, he can warn you about it; if you're entering some market, he can
remind you of what to buy; if you just woke up, he can remind you of doing various things...

WHEN WRITING TO THE FILES, ALWAYS ADD RANDOM DATA RANDOMLY THROUGH THE FILE IN BLOCKS OF LESS THAN 16 BYTES!!!!!
That should help preventing understanding what's in the message in case the same IV is used.


## Speech recognition

onEndOfSpeech is always called. onError and onResults are nto. How to know when the recognition finished completely? -
StackOverflow

Maybe try to put a flag variable to know if the recogniztion ended here on Main Service and equal startListening to a
variable.
See if it remains waiting. If it does, it's put in something asynchronous. If not, see below. In last case,
StackOverflow...

TRY TO SEE FIRST IF THE ---TOTAL--- RECOGNITION WITH POCKETSPHINX IS GOOD ENOUGH!!!!!!

EDIT: not that good idea, but I used a Thread to count 6 seconds since onEndOfSpeech's call - read the explanation on
the reposicao_reconhecimento_google_erro Thread.

PUT POCKETSPHINX AND KALDI'S FILES PROTECTED IN A SYSTEM FORLDER OR SOMETHING LIKE THAT SO IT'S NOT POSSIBLE TO DELETE
THEM!!!!!


## Phone Calls Processor

Test all the call receiver... I don't kno if it's well implemented or not (calls_state was wrong, I think)


## Charging

Stop the phone from charging when it gets to 80% and get back to charge on 79%

PUT THE APP RESETTING THE NORMAL CHARGING BEFORE THE PHONE SHUTS DOWN ---OR THE APP IS UNINSTALLED--- OR IT'S DEATH!!!!!


## App uninstallation

Detect the app uninstallation

https://stackoverflow.com/questions/18692571/how-can-an-app-detect-that-its-going-to-be-uninstalled

Plan B: use the Insurance app, when it's ready.


## Dangerous commands

Dangerous commands, like forcing the phone unlock or format or something like that, only with explicit confirmation from
the user, requested from the app and with the user on camera and digital print too, since it doesn't seem to be possible
to recognize a specific voice (though, even still - put the other 2 on this).


## Separate processes

Put AudioRecorder on a separate processes and the Main Service too, in order to, in case there's some error on Main
Service, AudioRecorder will still be recording. Important things should be put in separate processes. The Protected
LockScreen is another example. The Speech module maybe too. What was left would stay in Main Service's process.

EDIT: maybe not that good idea. If the main process is killed, everything will be killed anyways. If it's only one,
well, why would anyone only stop one process and not the entire app? So keep everything on the main process. If the app
is installed as system app and is persistent, the system will keep it always running no matter what, so cool enough.


## USB Mode

Change the USB mode automatically to what's wanted as default, in case it's not possible to put some default option on
the phone (like on BV9500). See here:
https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/hardware/usb/UsbManager.java,
a função setCurrentFunctions().

Maybe a bad idea, because then anyone can access the phone files. Think about that. In any case, do this for PLS to put
the phone in charging mode every as-soon-as-possible.


## Safe Boot

If you enable Safe Boot, the app won't turn on... Isn't it supposed to start? Direct Boot Aware...? Fix that.

PS: LOCKED_BOOT_COMPLETE appears when the phone starts and hasn't been unlocked. With Safe Boot, we can't have Debug
Mode active, so no idea if also sent there or not. If it is, there's some other problem. If not, there's the problem,
possibly.


## Root commands

Think about putting root commands for everything or almost everything. If the app is not as a system app, it won't do
various things. Imagine someone removes it from being a system app. The app has root access, but it won't do anything
with it. Unless it puts itself in the system folders. An external app doing it or itself would copy itself to system
folders and when it would start as system app again, it would delete the old file (if it's even possible to do this).
Think about it.


## Speech

If the speech is interrupted and the text is big (bigger than no idea how many words), use onRangeStart() on
UtteranceProgressListener to know where he was when he stopped speaking. Better than starting all over again.

See this: https://stackoverflow.com/questions/59488998/highlighting-the-text-while-speech-is-progressing.

See also this commend from one of the answers of the thread above, which indicates that there might be a problem with
that function
https://stackoverflow.com/questions/59488998/highlighting-the-text-while-speech-is-progressing#comment114982028_59493228
If it's true (or anyways), remove all line breaks from the speech string as soon as the function receives it.


## APK Signature

See this about checking the APK signature in an external library:
https://stackoverflow.com/questions/30650006/ndk-application-signature-check


## Speech

Put the Speech module saying everything it didn't say while the phone was not in Normal ringer mode. Else, those said
things will be lost. A listener or a thread with a while or something like that. In case it's a while, enable the while
only after the first time that a speech is not spoken because of the ringer mode. Disable the thread as soon as the
while detects the Normal mode.


## Automated speeches

Good morning sir. It's 23 ºC and it's a cloudy morning/afternoon/night. It will be hot today. It's not raining.


## Emergency SMS messages

For emergencies, any message started by "emergency" coming from trusted numbers, give the alert, even in middle of
meetings, classes and stuff (Critical priority then).


## Speech

If you turn the assistant's volume down while he's speaking, he won't change it until all speeches on lists are
finished. What if, for any reason, he starts speaking right after having stopped all speeches...? Put a timer. Only if
after a time the assistant can change the volume again in the beginning of a new set of speeches.


## Speech

If I put the volume low before speaking (like before clicking a button) so it's not heard very much, he won't care. Put
a waiting time on that.


## Memorize when to shut up

"Carl is sleeping, speak lower / be quiet" --> Then he memorizes that Carl is sleeping and only when I tell him Carl is
has awake, the assistant will get back to normal volume or gets back speaking.


## Stop app through ADB

Test if you can stop the app with Debugging enabled through ADB or something, specially with the PLS enabled.


## Auto-connect to networks

Get the app connecting to ANY open Wi-Fi networks while the Anti-Theft is enabled. Check the connection. Doesn't work,
next network (there are the networks which require signing in, so next to one that doesn't).


## App as an Installer and/or Verifier, and Setup

Take a look at PACKAGE_VERIFICATION_AGENT ;-) --> seems to transform the app in a Verifier app... Grant runtime
permissions...?
Also take a look at BIND_PACKAGE_VERIFIER.
GRANT_RUNTIME_PERMISSIONS has "verifier" protection level since only Marshmallow MR1. The first Marshmallow release
doesn't have verifier in the protection level, so try to get the app as an "installer" instead.

There's another one interesting too: USE_INSTALLER_V2.

Actually... Just root the BV9500 and test if by just installing the app as a system app it becomes an Installer. If not,
try to add the PACKAGE_VERIFICAR_AGENT permission. If it's not a Verifier still, try to put the BIND_PACKAGE_VERIFIER
where the INCOMING_EXT_COMMS is. If still not, try to add USE_INSTALLER_V2 to see if it does anything (probably not,
since it's only granted to Signature or Verifier apps).

Also, try to put the app being of the type Setup, which is needed for NETWORK_SETUP_WIZARD, needed to call
setAirplaneModeEnabled on ConnectivityManager as of API 29.

The StackOverflow question about this (which is updated from time to time): https://stackoverflow.com/questions/68278536


## Log the entire app

Put the function that handles exceptions of the entire app logging them to some file or whatever and update the function
documentation to say what it does exactly.


## Persistent app

If it's for the app to be persistent, then the app itself must have a way of restarting internally, since from outside
it can't - unless it's some other system app that orders so through forceStopPackage() -, because it's not being able to
update... (Android Studio can't kill it, as a start.)


## PLS overlay not working

The system overlay of the PLS is not working on BV9500.... Why....?


## Boot animation

Make a boot and shut down animation for the assistant and make it being always checking if it's there (SHA-something?).
Make have an initial part, the loop, and then a final part.

Make also one for the shut down. A very fast one. Something.


## Unique voice for VISOR

Make the assistant have a unique voice. Like, it uses only Brian internally and nothing else can use the voice, for
example. Find out how to do that.


## Telephony

[Someone calls] / "He's not available at the moment" / "When will he be available then?"
/ "When will you be available, sir?" / "In 2 hours" / "He'll be available i 2 hours"

Or...

"Are you not answering the call? Would you like me to say something?"
/ "Yes. I'm washing my teeth now, can't answer" / [And the assistant
would say that, either on call or by SMS message]

## ??

Have a list of all bought food, store the validity of the food, and keep warning if it's going to pass or not. Like, a
few days before it ends.


## Telephony

Block phone numbers.


## Speech

If I put the volume to zero and start Vibrating mode, the assistant will keep speaking until the speech stops, and then
all next speeches are skipped. He should stop the speech immediately...

Also, the last speech that was actually spoken (with volume...) should be the last thing said, and all the others should
be on some list to then say them, starting with the one that was interrupted ("As I was saying, (...)").


## Speech

If there is some issue on the device and many broadcasts are sent at once and the assistant speaks in each of them, it
will be speaking for some time always the same thing. Like on the tablet, when the charger cable is not well put.
So get the assistant to skip infinite equal speeches and say it skipped X equal speeches to the last one.

## Speech

Put it checking for background noise before speaking. If it's very noisy, put volume at maximum to speak. If it's no
noise at all, low volume. If people are talking, for example (medium noise), put medium volume (as it's default now).


## Telephony

Answer the calls on some other device.


## Radio

Integrate a radio app into it (FM radio, not Internet radio). Also try to integrate SpiritFM into this, if the license
agrees (I don't know, the author unfortunately passed away and the paid app is no longer sold, so no idea if I could
just decompile it and put the code here, I'll have to see if that's legal).


## Speech recognition

Think of some way for VISOR to not listen to himself. If there is an error processing the commands, for example, he
can't say what was said to generate the error because he'll hear himself saying what caused the error --> wow. So...


## Notify of errors

Send errors by e-mail or at least with a notification.


## Executor

If there is not SIM card on the phone, the emergency commands (which are not ready yet) won't work and might throw an
error? Or even if it doesn't, at least would be good to remind it's not possible to call someone - only emergency
numbers.


## Speech recognition

If the Google App is not installed, Google's speech recognizer won't be available --> overall speech recognition won't
work. So...
    - Force the app to be always installed and with the speech recognition files...?
    - Put something internal to the app to recognize speech? (Note: PocketSphinx is not good enough for normal speech.)

Aside from that, put this checking if English is installed and selected (the app can only accept English).


## Speech recognition

If the Google App is not installed or selected as the speech recognizer, the GoogleRecognition service will hang
forever --> put a timer on onBeginningOfSpeech() or something.


## Executor

Put it enabling the Mobile Data in a decent way without root or system permissions if possible.


## Speech

Show a notification if the assistant can't talk.


## Values Storage

The values are not put there in the app initialization... Change that. So the last call if the phone was restarted, is
no call at all? Cool.


## Executor

Put all command executions running on a separate thread... Maybe put the Executor on a separate process?
Turn on or off the airplane mode with the root commands option and see how infinite it takes and meanwhile, the app is
not responding.


## Speech Recognition

Store the meaning of "it" and "and" with Java between the Platforms Unifier calls. This way I can say "turn the wifi on"
and seconds later "and turn it off" - or "and the bluetooth too".


## API 15

The Speech2 is not working very well. It says stuff twice, like the warning of the app not being a device administration
and it said Ready sir again after clicking to grant permissions. Fix that.


## Speech Recognition

Get him to stop all other audios if he's called - and if the audios are being played on the speakers. This is not for us
to stop hearing other stuff while we're talking. It's for him to understand what he's being told to do without other
sounds in the background (in case he has managed to hear his name with music playing or something - wow, not normal).
