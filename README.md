# FFBExecute
Set of scripts to control FFBE for TMR farming

This is a bit of a weekend project, but I learned of the power of ADB and I thought, its time to get those TMRs i'm missing.
You will need android studio and android debgging tools in order for this to function.

This is currently built for my phone, the Axon7 and will not work with any other device.  It has support for othe devices, but you will need generate the required device configuration file.

## Example

0. Have a Axon7 phone & Android Studio with Debug tools.  Make sure you phone has developer options enabled.
1. The source code comes with a working folder, you should point your working folder to this one.
2. Open FFBE and navigate to the Earth Shrine mission list.
3. run with the arguments "script earthshrine-exit-attack"

## What is it doing

0. Loading Script, Device, View, Connection & Player Information
1. Executing the main loop
2. Grabbing the current screen
3. Handle conditions and execute actions

## Run options

### Earthshrine Missions

#### Command (3 Attack)
script earthshrine-exit-attack

#### Command (Auto Attack)
script earthshrine-exit-auto

#### Command (Farm Items & Attack)
script earthshrine-exit-attack-item

### Manager

#### Command
manager [phoneName]

### GUI

#### Command (WIP)
gui

## What needs to happen

1. Map parsing
2. Gui functions
