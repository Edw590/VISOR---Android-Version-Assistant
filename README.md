# V.I.S.O.R. - A real assistant [Android/Client]

Secondary project name: Assist_C_A (Assistant Client Android)

## Table of Contents
- [Pictures](#pictures)
- [Notice](#notice)
- [Background](#background)
- [Explanation of the assistant](#explanation-of-the-assistant)
- [Current modules (features)](#current-modules-features)
- [Installation/Usage](#installationusage)
- [For developers](#for-developers)
- - [To compile the app](#--to-compile-the-app)
- [About](#about)
- - [Roadmap](#--roadmap)
- - [Project status](#--project-status)
- - [License](#--license)
- [Support](#support)
- [Final notes](#final-notes)

## Pictures
![alt text](Pictures/Dev_Mode.png)
![alt text](Pictures/Modules_Status.png)
![alt text](Pictures/Global_values.png)

## Notice
This project is a part of a bigger project, consisting of the following:
- [V.I.S.O.R. - A real assistant [Android/Client]](https://github.com/DADi590/V.I.S.O.R.---A-real-assistant--Android-Client)
- [V.I.S.O.R. - A real assistant [Platforms Unifier]](https://github.com/DADi590/V.I.S.O.R.---A-real-assistant--Platforms-Unifier)

## Background
Hi all. This Android version is a project I started in January 2020 when I broke my phone's screen and glass (so I could see and do exactly nothing with the screen and touch - only Vysor and TeamViewer helped/help, but only with a PC nearby) - the PC version started in 2017 a month after I learned what programming was, but it's too no-code to publish XD (I didn't know what a function was by then...). Anyways. As I wasn't gonna switch phone so quickly (bought a new one a year later), I decided to make an assistant for it that would do anything I'd need without the need for me to have a working screen and touch (basically an app for a blind person, I guess). Could only use the Power, Vol Up and Vol Down buttons.
I've now finally decided to make it public, after vastly improving its coding style (<3 IntelliJ's Inspections...) and translating the code to English (was in Portuguese, even though speeches and recognition were already in English).

## Explanation of the assistant
Its command recognition submodule is not a simple recognition (have a look on the Platforms Unifier module) - you don't have to say the exact command for it to recognize it. It's not smart either though (it's not AI - yet?). You can say any words inbetween some hard-coded command words and it will still recognize the action(s). You can even tell it to don't do something you just told it to do. It's supposed to be an assistant for real-life use.

It's also supposed to work 100% offline. It can use online features, but preferably, only if they're not available offline. If Internet connection goes down, app goes down - doesn't seem a good idea... xD

For now it's also an app that has everything hard-coded, so no options to customize in the UI. Feel free to change whatever you'd like and use it yourself, for example. If I decide to publish it on some store, an UI will be made for users to be able to choose as many things as I can make choosable.

I should also note that I'm making this app to "think" it's a God. That's why you might see some "abusive" parts on it, like my atempts to force permissions to be granted (which I could not do yet). It's supposed to be as secure as I can make it. Check it for yourself though. It has nothing that steals data (decompile the APK if you want, there are tools online for that; or compile it yourself). I might try to keep the app without the Internet permission if I release it to a store, so people can be relaxed about it (it will probably cut features if I do it though).

The app "supports" API 15 at minimum (that's Android 4.0.3). That's because I like to support as many devices as I can (GoMobile, a library I'll use, is only available from API 15 onwards). Though, I'm only testing the app on Lollipop 5.1, on Oreo 8.1 (my 2 phones), and on Lollipop 4.4.2 (my tablet). Other Android versions only with the emulator, and more rarely.

The app is also able to work with root access and system permissions. The prefered way to install the app is with root permissions, installed as a privileged system app, and Device Administration enabled for it (absolute control XD). The app must work without them perfectly, but if some features are ONLY available with one or more of the 3 mentioned ways, they will be added. So to have full functionality, install it that way. Btw, you could also install it as a system-signature app (signed with the system key) to have even more features - except you won't have more, because I'm not making the app supposing that case, because as a start, I could not test it. So the maximum is what I wrote above. In the app I suppose that's the maximum "God level".

## Current modules (features)
Hopefully I don't forget to keep adding the modules here. Here's a list of the modules the assistant currently have and what they do (module names are in bold for ease of read):
- **Audio Recorder** --> Records audio from a given audio source (like phone calls, microphone...). The audio will be recorded in background and in a good quality, and will be saved to a folder in the external storage, named V.I.S.O.R.. No notifications, nothing. And in case of an error, "Error 1" or "Error 2" only will be said. Nothing related to being recording... To get it to record the microphone audio, use the appropiate command from Platforms Unifier's Command Recognition submodule. To stop, long press the power button, or write "stop" on the text to send box.
- **Battery Processor** --> Processes the device battery level and warns if it's outside of normal ranges. For now, 5% or below, extremelly low battery; 20% or below, low battery; above 80%, battery charged enough to the recommended Lithum-Ion batteries percentage (always keep it from 20% to 80%, and varying as little as possible); 100%, battery completely charged.
- **Protected Lock Screen** --> [NOT READY] An alternate lock screen that is supposed to mimic ESET Mobile Security's lock screen on version 3.3 - it would lock users out of the normal one and let them do nothing until they inserted the correct code or, in case Internet connection was available, the device was removed from the protected mode in ESET's Anti-Theft website. If you'd like to see it working, give the app Device Administration privileges and then remove them. The supposed only way to get back to the device (without restarting - didn't take care of that yet) is to click Unlock.
- **Speech module** --> Gets the assistant to speak. It can have customizable priorities (currently, Low, Medium, User Action, High and Critical) and can skip speeches. Some important notes here:
- - About the Critical priority: it will take your phone out of Do Not Disturb mode, get it to full volume, and say what it has to say (currently only if you disable the Device Administration Mode).
- - Another important thing is: the assistant will NOT speak if the phone's ringer mode is not the Normal one (which means, only if you have sound enabled for calls and messages). If it's on Vibrating or Do Not Disturb, it won't speak - unless again, Critical speech priority, which bypasses everything I found that could be bypassed to speak xD.
- - Also another notes: if you have sound enabled and the assistant speaks...
- - - It may raise your calls/messages volume to speak, and will stop other apps' audio. After it's done speaking, all will be back to normal, if anything was changed (volume, other apps' audio and Do Not Disturb) - unless it changed the volume to speak and while it was speaking, you changed it (in that case, the volume will not be touched again and will remain the one you chose).
- - - Also, on High priority and above, all speeches will be spoken in both speakers and headphones. Below High, either headphones or speakers will be used, not both.
- Telephony:
- - Note: all the modules here that need to get the contact name of a phone number will do it like this. If the phone number is only numeric (like +351 123 456 789 in case of Portugal), it will get the name related to that number. If the "number" has letters on it, it will warn it's an alphanumeric number (like "PayPal"). If it's a private number, it will say it's a private number. If it found different names for the same phone number, it will warn it detected multiple matches on that number.
- - **Phone Calls Processor** --> Processes any phone calls made by and to the phone and warns about incoming, waiting and lost calls, currently. For now, sometimes it may warn that a call was lost only after all calls have been terminated - that's a problem of the current implementation of the call state detection, which shall be improved some time (could take some time, as it's not a big deal to me).
- - **SMS messages Processor** --> Processes any messages received on the phone and warns who is sending the message (it won't say what the message is - if I implement that, I'll have to put that to a limited list or something. I won't put it to all contacts).
- **Speech Recognition** --> This is a module which contains 2 different speech recognizers: the Google one and PocketSphinx from the CMUSphinx project. PocketSphinx is used for hotword recognition (to call the assistant by saying his name), which then calls Google speech recognition to recognize normal speech (in which you can say commands - like "turn on the wifi and the bluetooth and what time is it").
- **Commands Executor** --> Executes commands given to it - after having the speech been given to the Commands Detection submodule of the Platforms Unifier for this last to return the list of detected commands, this list is given to this module, which then executes each one in the given order.

## Installation/Usage
Install the app either as a perfectly normal app, or as a privileged system app (below KitKat 4.4, in /system/app; on 4.4 and above, in /system/priv-app/). Grant it root access, if you'd like, and also enable Device Administration for it, if you want. Privileged system app + root access + Device Administration will give you full functionality of the app.

After that, currently just click on the permissions button and accept everything (or what you'd like to accept - though I'm not/will not steal anything, so you can accept everything, if you want), and that's it. The app will do everything automatically (there's not much to do manually with it yet - only recording audio is manual for now).

## For developers
### - To compile the app
- I'm not sure if other IDEs other than Android Studio can be used to compile an Android app, but that's the one I used. So if you want, use it too.
- Also, to be able to compile this app, you'll need hidden/internal APIs available on Android Studio, or it will throw errors. I decided to compile this app with those libraries since they allow some interesting and useful things to be used here. Have a look here to download and install those libraries: https://github.com/anggrayudi/android-hidden-api.
- Another thing needed to run the app without signing it with my certificate is to go to UtilsServices and comment the signature check on startMainService().
- I have also private constants and stuff used on the app (for example MAC addresses, or might have phone numbers), which are in files excluded from Git. Those things must be replaced when compiling the app. Hopefully the variable/constant name should help. If it does not, just tell me and I'll explain it (and improve for next time).

Now a small explanation of the app structure:
- All modules are inside the Modules folder. Each module has a folder of its own and all directly related things to it should be on that folder.
- All utility methods are inside classes started with "Utils". All main utilities are on GlobalUtils. Other utilities may be one specific packages of modules or other folders (depending if the utilities are for global app usage or only for that specific package).
- The main broadcast receivers are on BroadcastRecvs package. I say "main" because other broadcast receivers may be anywhere else on the app code. For example inside classes, like on Speech2 or on MainSrv.
- The HiddenMethods would be used if I had not discovered about a static way of getting Context. I'm keeping it in case it's needed again.

## About
### - Roadmap
Have a look on the "TODO.md" file.

### - Project status
Ongoing, but possibly slowly since I'm a student, so I may not have that much time to work on this (even though I'd love to have more time) - except on Holidays xD.

### - License
This project is licensed under Apache 2.0 License - http://www.apache.org/licenses/LICENSE-2.0.

## Support
If you have any questions, try the options below:
- Create an Issue here: https://github.com/DADi590/V.I.S.O.R.---A-real-assistant--Android-Client/issues
- Create a Discussion here: https://github.com/DADi590/V.I.S.O.R.---A-real-assistant--Android-Client/discussions

## Final notes
Hope you like the app! Any new ideas are welcomed! (I just may or may not implement them that fast - student)
