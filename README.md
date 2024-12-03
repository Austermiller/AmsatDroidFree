Track satellites orbiting the Earth in real time and predict their passes over your location.

Here is an updated version of the AmsatDroidFree application (originally created by Dave Johnson G4DPZ).

New Features and Bug Fix list:
- Gradle dependencies updated.
- Celestrak kep URL updated to... "https://celestrak.org/NORAD/elements/gp.php?GROUP=weather&FORMAT=tle" it was using "http://celestrak.com/NORAD/elements/noaa.txt" which is no longer a valid URL
- Changed kep reference URLs to http(S) secured
- Added an alert feature to remind the user that a satellite AOS is coming in 15 minutes (see screenshot below).
- Added a sort feature which allows the user to sort the list of satellites (see screenshot below).
- Added a feature so the user can view the pass date/time in Local or UTC formatting (see screenshot below).
- Fixed landscape layout issue where part of the mapview would be missing on certain UI's.
- Fixed issue where MEO satellites (like IO-117 aka:Greencube) didn't always show the complete pass.
- Fixed issue where under certain conditions a LEO and MEO satellite pass prediction wouldn't show up depending on how far into the current pass you currently were.
- Added a UI-Theme menu option allowing the user to change the Dark/Light Theme for the application (defaults to dark).
- Fixed issue where on some devices the date/time picker rendered incorrectly.
- App now checks and asks user for required Android permissions on an as needed basis.

<img src="https://github.com/user-attachments/assets/f720ceee-869e-4291-beec-2edda97e1556" alt="Alt Text" width="258" height="560">
&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/9bfadf2d-0927-450a-8189-6d41bb3512bf" alt="Alt Text" width="258" height="560">
