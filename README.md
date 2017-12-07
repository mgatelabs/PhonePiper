# FFBExecute
Set of scripts to control FFBE for TMR/Esper farming

This is a bit of a weekend project, but I learned of the power of ADB and I thought, its time to get those TMRs i'm missing.
You will need android studio and android debgging tools in order for this to function.

This is currently built for my phone, the Axon7 and will not work with any other device.  It has support for othe devices, but you will need generate the required device configuration file.

## Example

0. Have a Axon7 phone & Android Studio with Debug tools.  Make sure you phone has developer options enabled.
1. The source code comes with a working folder, you should point your working folder to this one.
2. Open FFBE and navigate to the Earth Shrine mission list.
3. run com.mgatelabs.ffbe.Runner without any arguments
4. Select "Script", "Run", "Your Device", "Leave View empty", and choose the script "earthshrine-exit-attack", then hit Start
5. In the frame, type in you're phone's IP address, player level and hit Start script

It is highly recommended to have IntelliJ Community edition to run this app.  You only need android studio, so you can have access to ADB.

## What is it doing

0. Loading Script, Device, View, Connection & Player Information
1. Executing the main loop
2. Grabbing the current screen
3. Handle conditions and execute actions

## Required Apps

1. The helper app https://github.com/mgatelabs/ImageReaderApp
2. Android Studio or Android Tools
* https://developer.android.com/studio/index.html
* The full package, with it you can also deploy to your phone
* https://developer.android.com/studio/releases/platform-tools.html
* Just the tools, like ADB
3. Intellij Community Edition, might as well use the best IDE thats free
* https://www.jetbrains.com/idea/

## What needs to happen

1. Map parsing
