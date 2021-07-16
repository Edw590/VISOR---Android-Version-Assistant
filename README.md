# LEGION - A real assistant [Android/Client]

Table of Contents:
- [Background](#background)
- [Explanation of the assistant](#explanation-of-the-assistant)
- [Current modules (features)](#current-modules-features)
- [Installation/Usage](#installationusage)
- [For developers](#for-developers)
- - [Compile the app](#compile-the-app)
- - [License](#license)
- [About](#about)
- - [Project status](#project-status)
- - [Roadmap](#roadmap)
- [Support](#support)
- [Final notes](#final-notes)

## Background
Hi all. This is a project I started in January 2020 when I broke my phone's screen and glass (so I could see and do exactly nothing with the screen and touch - only Vysor and TeamViewer helped/help, but only with a PC nearby). As I wasn't gonna switch phone so quickly (bought a new one a year later), I decided to make an assistant for it that would do anything I'd need without the need for me to have a working screen and touch (basically an app for a blind person, I guess). Could only use the Power, Vol Up and Vol Down buttons.
I've now finally decided to make it public, after vastly improving its coding style (<3 IntelliJ's Inspections...) and translating the code to English (was in Portuguese, even though speeches and recognition were already in English).

Note: I know the name may sound weird, since Legion I think it's for plural stuff, but it's the name of my computer (Lenovo Legion) and seems better than calling it J.A.R.V.I.S., which is already taken. When I get speech recognition back on this, I'll test some names and see what are the best recognized ones.

## Explanation of the assistant
As of now (2021-06-27), the assistant does not do all the things it used to on its earlier versions. As time passes by, I'll add the old and new functionality to it.
Its command recognition module (not present here yet) is not a simple recognition - you don't have to say the exact command for it to recognize it. It's not smart either though (it's not AI - yet?). You can say any words inbetween some hard-coded command words and it will still recognize the action(s). You can even tell it to don't do something you just told it to do. It's supposed to be an assistant for real-life use.

It's also supposed to work 100% offline. It can use online features, but preferably, only if they're not available offline. If Internet connection goes down, app goes down - hmm, nah... xD.

For now it's also an app that has everything hard-coded, so no options to customize in the UI. Feel free to change whatever you'd like and use it yourself, for example. If I decide to publish it on some store, an UI will be made for users to be able to choose as many things as I can make choosable.

I should also note that I'm making this app to "think" it's a God. That's why you might see some "abusive" parts on it, like my atempts to force permissions to be granted (which I could not do yet). It's supposed to be as secure as I can make it. Check it for yourself though. It has nothing that steals data (decompile the APK if you want, there are tools online for that; or compile it yourself). I might try to keep the app without the Internet permission if I release it to a store, so people can be relaxed about it.

The app "supports" API 15 at minimum (that's Android 4.0.3). That's because I like to support as many devices as I can (GoMobile, a library I'll use, is only available from API 15 onwards). I'm only testing the app on Lollipop 5.1 and on Oreo 8.1 with my 2 phones though. Other Android versions only with the emulator, and more rarely.

The app is also able to work with root access and system permissions. The prefered way to install the app is with root permissions, installed as a system app, and Device Administration enabled for it (absolute control XD). The app must work without them perfectly, but if some features are ONLY available with one or more of the 3 mentioned ways, they will be added. So to have full functionality, install it that way.

## Current modules (features)
Hopefully I don't forget to keep adding the modules here. Here's a list of the modules the assistant currently have and what they do (module names are in bold for ease of read):
- **Audio Recorder** --> Records audio from a given audio source (like phone calls, microphone...). The audio will be recorded in background and in a good quality, and will be saved to a folder in the external storage, named LEGION. No notifications, nothing. And in case of an error, "Error 1" or "Error 2" only will be said. Nothing related to being recording... To get it to record the microphone audio, currently just write "start" on the text to send box, and to stop, write "stop".
- **Battery Processor** --> Processes the device battery level and warns if it's outside of normal ranges. For now, 5% or below, extremelly low battery; 20% or below, low battery; above 80%, battery charged enough to the recommended Lithum-Ion batteries percentage (always keep it from 20% to 80%, and varying as little as possible); 100%, battery completely charged.
- **Protected Lock Screen** --> [NOT READY] An alternate lock screen that is supposed to mimic ESET Mobile Security's lock screen on version 3.3 - it would lock users out of the normal one and let them do nothing until they inserted the correct code or, in case Internet connection was available, the device was removed from the protected mode in ESET's Anti-Theft website.
- **Speech module** --> Gets the assistant to speak. It can have customizable priorities (currently, Low, Medium, User Action, High and Critical) and can skip speeches. Some important notes here:
- - About the Critical priority: it will take your phone out of Do Not Disturb mode, get it to full volume, and say what it has to say (currently only if you disable the Device Administration Mode).
- - Another important thing is: the assistant will NOT speak if the phone's ringer mode is not the Normal one (which means, only if you have sound enabled for calls and messages). If it's on Vibrating or Do Not Disturb, it won't speak - unless again, Critical speech priority, which bypasses everything I found that could be bypassed to speak xD.
- - Also another notes: if you have sound enabled and the assistant speaks...
- - - It may raise your calls/messages volume to speak, and will stop other apps' audio. After it's done speaking, all will be back to normal, if anything was changed (volume, other apps' audio and Do Not Disturb) - unless it changed the volume to speak and while it was speaking, you changed it (in that case, the volume will not be touched again and will remain the one you chose).
- - - Also, on High priority and above, all speeches will be spoken in both speakers and headphones. Bellow High, depends either headphones or speakers will be used, not both.
- Telephony:
- - Note: all the modules here that need to get the contact name of a phone number will do it like this. If the phone number is only numeric (like +351 123 456 789 in case of Portugal), it will get the name related with that number. If the "number" has letters on it, it will warn it's an alphanumeric number (like "PayPal"). If it's a private number, it will say it's a private number. If it found different names for the same phone number, it will warn it detected multiple matches on that number.
- - **Phone Calls Processor** --> Processes any phone calls made by and to the phone and warns about incoming, waiting and lost calls, currently. For now, sometimes it may warn that a call was lost only after all calls have been terminated - that's a problem of the current implementation of the call state detection, which shall be improved some time (could take some time, as it's not a big deal to me).
- - **SMS messages Processor** --> Processes any messages received on the phone and warns who is sending the message (it won't say what the message is - if I implement that, I'll have to put that to a limited list or something. I won't put it to all contacts).

## Installation/Usage
Install the app either as a perfectly normal app, or as a system app. Grant it root access, if you'd like, and also enable Device Administration for it, if you want. System app + root access + Device Administration will give you full functionality of the app.

After that, currently just click on the permissions button and accept everything (or what you'd like to accept - though I'm not/will not steal anything, so you can accept everything, if you want), and that's it. The app will do everything automatically (there's not much to do manually with it yet - only record audio is manual for now).

## For developers
### Compile the app
I'm not sure if other IDEs other than Android Studio can be used to compile an Android app, but that's the one I used. So if you want, use it too.
Also, to be able to compile this app, you'll need hidden/internal APIs available on Android Studio, or it will throw errors. I decided to compile this app with those libraries since they allow some interesting and useful things to be used here. Have a look here to download and install those libraries: https://github.com/anggrayudi/android-hidden-api.

## About
### Roadmap
Have a look on the "TODO.txt" file.

### Project status
Ongoing, but possibly slowly, since I'm a student, so I may not have that much time to work on this (even though I'd love to have more time) - except on Holidays xD.

### License
This project is licensed under Apache 2.0 License - http://www.apache.org/licenses/LICENSE-2.0.

## Support
If you have any questions, just create an Issue here: https://github.com/DADi590/LEGION---A-real-assistant--Android-Client/issues.

## Final notes
Anything you see about a DADi EMPRESAS, Inc. or DE, Inc., forget about it XD. It's a non-existent company. I made it up on High-School when I bought a CASIO fx-CG20 and it asked for a company name. So I made this one up xD. Thought why not keep using it, so I have some company name to put on stuff xD.

Hope you like the app! Any new ideas are welcomed! (I may not implement them that fast - depends, as I'm studying.)
