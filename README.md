Track satellites orbiting the Earth in real time and predict their passes over your location.

Here is an updated version of the AmsatDroidFree application (originally created by Dave Johnson G4DPZ).

New Features and Bug Fix list:
- Gradle dependencies updated.
- Celestrak kep URL updated to... "https://celestrak.org/NORAD/elements/gp.php?GROUP=weather&FORMAT=tle" it was using "http://celestrak.com/NORAD/elements/noaa.txt" which is no longer a valid URL
- Changed kep reference URLs to http(S) secured
- Added an alert feature to remind the user that a satellite AOS is coming in 15 minutes.
- Added a sort feature which allows the user to sort the list of satellites
- Added a feature so the user can view the pass date/time in Local or UTC formatting.
- Fixed landscape layout issue where part of the mapview would be missing on certain UI's.
- Fixed issue where MEO satellites (like IO-117 aka:Greencube) didn't always show the complete pass.
- Fixed issue where under certain conditions a LEO and MEO satellite pass prediction wouldn't show up depending on how far into the current pass you currently were.
- Added theme styling so that the application displays in dark OR light theme UI based on how the user sets it in the Android UI settings.
- Fixed issue where on some UI's the date/time picker rendered incorrectly.
- App now checks and asks user for required Android permissions on an as needed basis.

If you're not interested in the source code and only want to install and use the Android application then simply download the file, AmsatDroidFree.apk and install it on your phone.
