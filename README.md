# FFBExecute
Set of user editable scripts to control FFBE for TMR/LB/Esper farming.

Some scripts are a bit outdated, and need to be re-evaluated again.

## Features
* Scriptable, simple JSON language
* Device agnostic, the script is seperate from the view
* Logging, it prints what it's doing
* Editing, built in method to make or edit views
* Extensible, views are built upon other views
* Screen saver.  Every 500 iterations, it switches to the Camera and then back again, so avoid screen burn-in.

## Getting Started

0. Have the right phone/resolution
* Samsung S7 (1080P)
* More devices are possible, but I only have a limited # of phones.  If I was provided screen shots for every needed screen, I could build another definition.
1. Install Java 8
* http://www.oracle.com/technetwork/java/javase/downloads/index.html
* JRE is to only run the app
* SDK lets you develop your own code, or build from source
2. Install a way to communicate with your Android device
* Android Tools (Basic) Use this just to connect
* https://developer.android.com/studio/releases/platform-tools.html
* Android Studio (Advanced) This lets you also deploy from src, the helper app
* https://developer.android.com/studio/index.html
3. Get the helper app, and install it onto your device
* Released version
* https://github.com/mgatelabs/ImageReaderApp/releases
* Build from source
* https://github.com/mgatelabs/ImageReaderApp
4. Get this app
* Release version (This is really out of date)
* https://github.com/mgatelabs/FFBExecute/releases
* Build from source (Use Intellij Community edition)
5. Make sure ADB works
* Plug in your device
* Enable developer mode
* https://developer.android.com/studio/debug/dev-options.html
* Open a cmd window
* type "adb device-state", press enter and see what is returned.  If it's working you should see "device"
6. Run the app
* From JetBrains IntelliJ
** you need to setup a Java 8 configuration to execute "com.mgatelabs.ffbe.Runner"
** Set the program argument to SERVER
** Change the working directory to the working folder included with the app

## Common Configurations

## TMR farming
Make sure your already at the earth shrine menu.  The TMR team by default is slot 2.  You can override the default slot with scripts.
-1. Run the helper app on the phone
0. Run the app with "server" as a parameter
1. Open your web browser to localhost:8080/piper
2. Setup your run
* Device: Samsung-S7-1080
* Views: 1080x1920
* Scripts: farming
* More scripts can be added to tune your run
2. Hit load
3. Goto the device tab and make sure the IP is right, then hit connect to link ADB to your device.
4. On the run tab hit play/pause
5. Watch the screen, it should start to move.  If not check the console for errors.

## Server setup

For running on an Raspberry PI

0. git clone https://github.com/mgatelabs/FFBExecute.git
1. cd FFBExecute
2. mvn clean package
3. cd working
4. java -jar ../target/FFBExecute-0.0.5-SNAPSHOT.jar server

## What needs to happen

1. Map parsing
2. Finish built-in script editor
