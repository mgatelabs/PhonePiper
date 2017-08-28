# FFBExecute
Set of scripts to control FFBE for TMR farming

This is a bit of a weekend project, but I learned of the power of ADB and I though, its time to get those TMRs i'm missing.
You will need android studio and android debgging tools in order for this to function.

This is currently built for my phone, the Axon7 and will nto work with anyother phone.

# What is it doing

1. Grabbing the current screen
2. Loading the screen into memory
3. Iterate through all available actions, for the current phase
4. For each action check if it should execute by comparing pixels to known values
5. Execute the command and move forward
