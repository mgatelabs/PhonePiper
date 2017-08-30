# FFBExecute
Set of scripts to control FFBE for TMR farming

This is a bit of a weekend project, but I learned of the power of ADB and I thought, its time to get those TMRs i'm missing.
You will need android studio and android debgging tools in order for this to function.

This is currently built for my phone, the Axon7 and will not work with any other device.

## Example

0. Have a Axon7 phone & Android Studio with Debug tools.  Make sure you phone has developer options enabled.
1. Setup a working directory
2. Open FFBE and navigate to the Earth Shrine mission list.
3. run with the arguments "run run earthshrine-exit"

## What is it doing

1. Grabbing the current screen
2. Loading the screen into memory
3. Iterate through all available actions, for the current phase
4. For each action check if it should execute by comparing pixels to known values
5. Execute the command and move forward

## What needs to happen

1. Map parsing
2. Better action handling with branches, pushing, popping switching
3. Implement swipes
4. Better way to analyze screens for initial setup, maybe with electron