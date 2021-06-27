NOTE - PLEASE READ BEFORE MAKING ANY CHANGES!!!

I don't think all the warnings about requiring newer APIs are true...
For example, RemoteException.rethrowFromSystemServer() says it requires API 30 at minimum. I ran it on API 28...
Even forgetting that, why would it work on older phones?
Also, Android Studio says Context.getOpPackageName() needs API 29 and it ran on 28 too.

Sum up: if it's something internal to the hidden method and the public method doesn't say it requires a newer API,
just ignore the API warning.
Example: AudioManager.isWiredHeadsetOn() is here since API 1. But RemoteException.rethrowFromSystemServer() only
works on API 29, supposedly, which is not true - in these cases, ignore the warning.

NOTE - PLEASE READ BEFORE MAKING ANY CHANGES!!!
