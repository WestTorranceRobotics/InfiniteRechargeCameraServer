==================
Programming the Pi
==================

--------
Building
--------

Java 11 is required to build.  Set your path and/or JAVA_HOME environment
variable appropriately. Then run the build function of gradlew. These two
steps have been combined for most FRC-enabled Windows systems in the file
BUILDJAR.bat.

---------
Deploying
---------

On the rPi web dashboard:

1) Make the rPi writable by selecting the "Writable" tab
2) In the rPi web dashboard Application tab, select the "Uploaded Java jar"
   option for Application
3) Click "Browse..." and select the "java-multiCameraServer-all.jar" file in
   your desktop project directory in the build/libs subdirectory
4) Click Save

The application will be automatically started.  Console output can be seen by
enabling console output in the Vision Status tab.

====================
Features and Updates
====================

=== 2 March 2020: First Functional Version ===

Rewriting the camera switcher from the top to the bottom has proved successful. Although there likely exist potential optimizations and cleanings in the code, and even potential feature enhancements, this version is being merged for its completeness of vital functionality.

Documentation of interface to first completed camera server version:

Network tables:
It uses the table "rpi" and has two keys
"aimbot" which is 1 or 0, depending on whether or not we want the vision tracking to be running. This is just to make sure the rpi knows when vision is running so that if the driver requests limelight camera view, it won't overwrite the network table settings that turn on the limelight. If "aimbot" is set to zero, or if it doesn't exist, the rpi assumes it is allowed to turn off the limelight.
"camera" which is a number between 0 and the NUMBER_CAMERAS code constant (which is equal to the number of cameras plugged into the pi by usb). Allowed range is inclusive. This number chooses which camera will be displayed on the shuffleboard.

Viewing:
Open SmartDashboard. Add a new Camera Server Stream Viewer Widget to the dashboard. Set the view to be editable, and set the stream to a reasonable size in the context of the rest of the SmartDashboard display. Return the view to its uneditable mode. The stream is not currently known to be accessible from Shuffleboard.
