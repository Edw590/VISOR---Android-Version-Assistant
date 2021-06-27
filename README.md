

# LEGION - Android Assistant [Client]

## Introduction
Hi all. This is a project I started in January 2020 when I broke my phone's screen and glass (so I could see exactly nothing - only TeamViewer and Vysor helped, but only with a PC nearby). As I wasn't gonna switch phone so quickly, I decided to make an assistant for it that would do anything I'd need without the need for me to have a working screen and touch (basically an app for a blind person, I guess). Could only use the Power, Vol Up and Vol Down buttons. I've now finally decided to make it public, after improving vastly its coding style and translating the code to English (was in Portuguese).

Note: I know the name may sound weird, since Legion I think it's for plural stuff, but it's the name of my computer (Lenovo LEGION) and seems better than calling it JARVIS, which is already taken. When I get speech recognition back on this, I'll test some names and see what are the best recognized ones.

## Basic explanation of the assistant
As of now (2021-06-27), the assistant does not do all the things it used to on its earlier versions. It used to do various things more than it's doing now (basically only does automatic things and records audio from the microphone). As time passes by, I'll add the old and new functionality to it.
Its command recognition module (not present here yet) is not a simple recognition, which means you don't have to say the exact command for it to recognize it. It's not smart (it's not AI - yet?), but it's not like that. You can say any words inbetween some hard-coded command words and it will still recognize the action. It's supposed to be an assistant for real-life use (as it was for the year I used it, until recently I bought a new phone).

It's also supposed to work 100% offline (can use online features though, but I prefer it to be 100% usable offline, as opposite to recent trends, which are not really of my liking - Internet connection goes down, app goes down --> not cool at all).

For now it's also an app that has everything hard-coded (probably harder to hack that way). If I decide to publish it on Play Store, for example, I'll make an UI for users to be able to choose a s many things as I can make choosable. But for now it's all hard-coded to my likings (feel free to download it, change whatever you'd like, compile it and use it yourself, for example).

I should also note this: I'm making this app to "think" it's a God. It's supposed to think it can control the entire phone. That's why you might see some "abusive" parts on it, like my atempts to force permissions to be granted (which I could not do). It will have Anti-Theft features, so it's supposed to be as secure as I can make it (the reason of various things I have on the Manifest).

## Current modules
Hopefully I don't forget to keep adding the modules here. Here's a list of the modules the assistant currently have and what they do:
- Audio Recorder --> Does what it says - records audio from a given audio source (like phone calls, microphone...). The audio will be recorded completely in background and in a very good quality (for me, at least). No notifications, nothing. And in case of an error, "Error 1" or "Error 2" only will be said. Nothing related to being recording... To get it to record audio, currently just write "start" on the text to send box, and to stop, write "stop" (at least until the Executor module is ready again).
- Battery Processor --> Processes the device battery level and warns if it's outside of normal ranges. For now, at least, 5% or below, extremely low battery; 20% or below, low battery; above 80%, battery charged enough to the recommended Lithum-Ion batteries percentage (always keep it from 20% to 80%); 100%, battery completely charged.
- Protected Lock Screen --> [NOT READY] An alternate lock screen that is supposed to mimic ESET Mobile Security's lock screen on version 2.3 - it would lock users out of the normal one and let them do exactly nothing until they inserted the correct code or, in case Internet connection was available, the device was removed out of the protected mode in ESET's Anti-Theft website.
- Speech module --> Gets the assistant to speak. There's the old version too, but that's only because I didn't want to delete it. This new one can have customizable speech priorities (as oposite to the first one, which could only have 2, and still not as well as the new one can) and skip currently speaking speeches. The old one I used for a year. This is the one I've been using which is much better. This is the module which has given me the most work so far xD. Some important notes here:
- - I should warn though, about the Critical priority: it will take your phone out of Do Not Disturb mode, get it to full volume, and say what it has to say (currently only if you disable the Device Administration Mode).
- - Another important thing is: the assistant will NOT speak if the phone's ringer mode is not the Normal one (which means, only if you have sound enabled for calls and messages). If it's on Vibrating or Do Not Disturb, it won't speak - unless again, Critical speech priority, which bypasses everything I found that could be bypassed to speak xD.
- - Also another note: if you have sound enabled and the assistant speaks, it will get the phone to half the volume to speak, unless the volume is already half or more, and it that case, it won't touch the volume. It will also stop other apps' audio while it's speaking. After it's done speaking it will return the other app(s) audio to continue to play. It will also set the volume back to as it was (ONLY in case it needed to change it) - unless you changed the volume while the assistant was speaking AND the audio stream used by the assistant to speak was being used when it started to speak. Aside from that case, the volume will be returned to as it was before starting to speak.
- Telephony:
- - Note: all the modules here that need to get the contact name of a phone number will do it like this. If the phone number is numeric (like +351123456789 in case of Portugal), it will get the name related with that number. If the "number" has letters on it, it will warn it's an alphanumeric number (like "PayPal", when you receive SMS codes). If it's a private number, it will say it's a private number.
- - Phone Calls Processor --> Processes any phone calls made by and to the phone and warns about it. It will warn about incoming calls, waiting calls and lost calls, currently. For now, sometimes it may warn that a call was lost only after all calls (or at least some) have been made - that's a problem of the current implementation of the call state detection (which shall be improved some time).
- - SMS messages Processor --> Processes any messages received on the phone and warns who is sending the message (it won't say what the message is - if I implement that, I'll have to put that to a limited list or something. I won't put it to everyone).

## Note for compiling the app
In order to compile this app, you need to have hidden/internal classes available on Android Studio, or it will throw errors. I decided to compile this app with those libraries since they allow interesting things to be used here.

## Final notes
None for now, except hope you like it! Any new ideas are welcomed! (I may not implement them that fast - depends, as I'm studying.)
