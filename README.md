Track satellites orbiting the Earth in real time and predict their passes over your location.

Here is an updated version of the AmsatDroidFree application (originally created by Dave Johnson G4DPZ).

New Features and Bug Fix list:
1. Added Native General Perturbation (GP) (JSON) Support
   Integrated support for CelesTrak’s gp.php endpoints using FORMAT=json.
   Parses CCSDS/OMM-style GP data with explicit key-value fields instead of fixed-width TLE strings.
   Implemented a new parser:
   TLE.importSatFromGPJSON(InputStream)
   Uses Gson for robust JSON parsing.
2. In‑Memory GP → TLE Reconstruction
   GP elements are converted in memory into classic 3‑line TLE structures.
   This preserves compatibility with the existing predict4java SGP4 engine, which relies on fixed‑column TLE parsing.
   No changes were made to orbital math, propagators, or pass prediction logic.
3. Dynamic Parser Routing
   Download and file‑loading logic now dynamically routes data based on source format:
   General Perturbation (GP) JSON → importSatFromGPJSON
   Legacy TLE text → existing importSat
   Enables seamless switching between:
   CelesTrak (General Perturbation-GP JSON)
   AMSAT (traditional TLE text)
4. Future‑Proofing for 6‑Digit NORAD Catalog IDs
   Added a safeguard for catalog numbers > 69,999:
   Uses a temporary 5‑digit placeholder in reconstructed TLE strings (to satisfy fixed-width parsing).
   Reinjects the true numeric NORAD ID directly into the TLE object after construction.
   Prevents crashes caused by TLE field width limits while preserving correct sorting and display.
5. Robust Epoch Parsing
   Replaced substring-based timestamp parsing with delimiter‑based parsing.
   Handles variable‑precision ISO‑8601 timestamps from GP feeds (e.g., fractional seconds). 
6. Backward Compatibility Preserved
   AMSAT nasabare.txt TLE downloads continue to function unchanged because AMSAT is still using TLE JSON data
   and not true GP JSON data.
   Existing cache serialization (ObjectInputStream / ObjectOutputStream) remains compatible.
   UI components (Spinner, RecyclerView, pass prediction workflow) required no changes.
7. Fixed GEOSTATIONARY INFINITE LOOP.  GEO satellites such as GOES 17 previously would lock the application.
   Because GEO sats remains fixed over a single spot on the equator, it does not have predictable rising or setting passes.  
   A future version of this app will display the elevation and azimuth for GEO Sats.

Benefits
✅ Compatible with CelesTrak’s General Perturbation (GP) transition and deprecation of legacy TLE feeds.
✅ Safe handling of future 6‑digit NORAD IDs.
✅ No regression to existing AMSAT or offline workflows.
✅ Safe handling of GEO satellites, without locking the application.
✅ Zero impact to SGP4 math, predictions, or UI behavior.

- Gradle dependencies updated.
- Changed kep reference URLs to http(S) secured.
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
<img src="https://github.com/user-attachments/assets/46c06ff0-7989-40b8-b8c8-620d81573216" alt="Alt Text" width="258" height="560">

