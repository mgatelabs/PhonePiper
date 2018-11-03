# Phone-Piper
Set of user editable scripts to control android apps.

## Features
* Scripting with a simple JSON language
* Device agnostic, the script is separate from the view
* Logging, it prints what it's doing
* Editing, built in method to make or edit views
* Extensible, views are built upon other views
* Screen saver to stop burn-in

## Getting Started

### Real Device

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
    * https://github.com/mgatelabs/PhonePiperHelper/releases
    * Build from source
    * https://github.com/mgatelabs/PhonePiperHelper
4. Get this app
    * Release version
    * https://github.com/mgatelabs/PhonePiper/releases
    * Build from source (Use Intellij Community edition)
5. Make sure ADB works
    * Plug in your device
    * Enable developer mode
    * https://developer.android.com/studio/debug/dev-options.html
    * Open a cmd window
    * type "adb device-state", press enter and see what is returned.  If it's working you should see "device"
6. Run the app
    * From Source with JetBrains IntelliJ
        * you need to setup a Java 8 configuration to execute **com.mgatelabs.piper.Runner**
        * Set the program argument to **SERVER**
        * Set the VM argument to **-Dserver.port=8090**
        * Change the working directory to the **working** folder included with the source
    * From the zip release
        * Run server.bat
7. Open your browser to **http://localhost:8090/piper**

### MEmu

1. Install Java 8
    * http://www.oracle.com/technetwork/java/javase/downloads/index.html
    * JRE is to only run the app
    * SDK lets you develop your own code, or build from source
2. Download a released version
    * https://github.com/mgatelabs/PhonePiper/releases
3. Unzip the release
4. Get the helper app, and install it onto Memu
    * Released version
    * https://github.com/mgatelabs/PhonePiperHelper/releases
    * Build from source
    * https://github.com/mgatelabs/PhonePiperHelper
5. If you're running the latest version, open Settings > Network and change NAT to Bridge
    * If your version does not have bridging as an option please see Memu notes in the bottom
6. Run the helper app on Memu, minimize the app and open FFBE
7. Open the extracted PhonePiper release and run server.bat
8. Open your browser to **http://localhost:8090/piper**
9. Click the create new configuration button
10. Set the name to **MEMU Farming**
11. The Device is either **MEMU-1080** or **MEmu7-1080**.
    * Use MEMU-1080 if you're on the default install.
    * If it's android 7.0 or beyond use MEmu7-1080.
    * If you choose the wrong one, the app may appear to lockup
12. Set the View to **1080x1920**
13. Set the Script to **farming**
    * If you have more scripts to load press the **+** button to choose another.  Script order does matter.
14. Set the IP
    * **NAT Mode**: 127.0.0.1
    * **Bridge Mode**: See helper app for IP address
15. Set the ADB variable
    * For me it was **C:\Program Files\Microvirt\MEmu\adb**
16. Direct, leave this blank, unless you have multiple instances of MEmu running.  It would typically be the IP:PORT of the instance or serial number.
17. Connection
    * **NAT Mode**: USB
    * **Bridge Mode**: WIFI
18. Throttle is used to slow down processing.  Set it to 25 for a faster run, but it will work harder.
19. Hit save config so you don't lose what you typed.
    * All settings are saved to your local browser
20. Tap **Run Script**
21. If everything is good you should be on the **Run** tab
22. Before you start anything visit the Variables tab and adjust a few things.  Also always hit update after changing something.
    * Set your **Player Level** for energy calculation purposes
    * Disable **Raid** & **Arena**, you don't have scripts for those yet.
    * Choose your **Farm Party**, the unit group that you TMR farm with.
23. Goto the setting tab and see if Update Preview does anything.  If you get a image back of the screen, you have a connection to the helper app.
24. It's time to start, from the home screen navigate to the earth shrine menu.  Later you won't need to do this.
25. From the Run tab press Play/Pause.  And it should start to TMR farm.

## Common Configurations

## Server setup

For running on an Raspberry PI

0. git clone https://github.com/mgatelabs/PhonePiper.git
1. cd PhonePiper
2. mvn clean compile package
3. cd working
4. java -Dserver.port=8090 -jar ../target/phone-piper.jar server 

## Memu notes

### Bridge
For Bridge routing there is no additional configuration needed

### NAT
For NAT routing you need to do the following.

1. Make sure MEmu is closed
2. Edit the file **\MEmu install directory\MemuHyperv VMs\MEmu\MEmu.memu**
3. And these lines under Adapter[slot=0], NAT

`<Forwarding name="ADB" proto="1" hostip="127.0.0.1" hostport="21503" guestip="10.0.2.15" guestport="5555"/>`
`<Forwarding name="PPH" proto="1" hostip="127.0.0.1" hostport="8080" guestip="10.0.2.15" guestport="8080"/>`
