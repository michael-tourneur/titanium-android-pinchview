Android pinch view for Titanium Android 1.8.1
===========================================

This project should be able to be deployed in Eclipse as described in Titanium Mobile's Android Module guide (Building from Eclipse section).  You just need to update the build.properties and class paths to point to where everything is on your system.  You will need to set up your system as in the Android module guide (Environment setup).

Android module developer's guide
http://wiki.appcelerator.org/display/guides/Android+Module+Development+Guide

Description
-------------------
This module will fire 4 distinct events:  multiStart, multiMove, multiEnd, and pinch.  The reason for this is that the original start move and end events swallow the event and prevent it from reaching pinch, and because move needs to be handled a little differently.

multiStart returns e with x and y, they are identical to touchstart.

multiMove return e with x and y, where x and y are not the absolute positions but the DELTA from the last position (in order to prevent jumps after scaling).

multiEnd returns e with x and y, they are identical to touchend

pinch returns the scale that the object should be.  It remembers the scale between pinches, so you don't need to account for that.  It will clamp between minZoomValue and maxZoomValue, which can be set as arguments during the view creation, or afterwords via the dot operator.

For an example, please see example/app.js