# RideVision 🚗💨 — CV-Based Pothole Detection & Proactive Road Safety Android App

> **Civic Tech & Computer Vision Road Safety System**  
> Built with Kotlin & Jetpack Compose for Android (Material Design 3)

RideVision is an intelligent road safety platform that bridges the gap between daily commuters and municipal road authorities. RideVision uses on-device Computer Vision to detect road surface hazards in real-time, alerts drivers of approaching hazards **250m ahead** using a directional bearing cone, maintains a crowd-verified repair lifecycle with **anti-gaming cooldowns**, and automatically formats complaints with GPS links for direct dispatch to municipal channels (e.g. **MCC Mangaluru Official WhatsApp**, **BBMP Bengaluru Helpline**).

---

## 🌟 Key Features

1. **On-Device Computer Vision Hazard Detection**:
   - Real-time road cavity and defect detection with severity categorization (`Severe`, `Moderate`, `Minor`).
   - Visual telemetry: bounding boxes, confidence score, road condition rating, and frame latency.
   - Built-in evaluation test benches with realistic road samples (Severe Hazard, Moderate Hazard, Clean Road) as well as live camera capture and photo import.

2. **Proactive Hazard Warning System (250m Directional Radar Cone)**:
   - Evaluates vehicle GPS position and heading relative to known hazards using Haversine distance and bearing cone matching (+/- 45° field of view).
   - Audio-visual alert banner surfacing distance, severity, street address, and safe distance guidance.
   - 360° interactive radar canvas with distance rings (100m, 200m, 300m) and heading alignment.

3. **Crowd-Verified Hazard Lifecycle & Anti-Gaming Cooldown**:
   - 15-meter spatial deduplication: prevents duplicate entries by automatically linking nearby reports to existing hazard records.
   - 7-day anti-gaming cooldown guard: prevents spam and repetitive verification votes.
   - Commuter actions: "+1 Still There" and "Mark Fixed" (transitions to verified safe status after repair threshold).

4. **Commuter Drive HUD Mode**:
   - Real-time digital speedometer, distance traveled, and road quality safety score.
   - Live route simulation along key commuter corridors (e.g., NH 73 Mangaluru / Vamanjoor).
   - Automatic 25m proximity detection flagging passed hazards for post-trip verification.

5. **Municipal Authority Grievance Dispatch**:
   - Pre-formatted structured complaint text containing location address, Google Maps link (`https://maps.google.com/?q=lat,lon`), severity rating, and timestamp.
   - Direct dispatch integration with:
     - **Mangaluru City Corporation (MCC) Official WhatsApp**: `+91 9449007722`
     - **Bruhat Bengaluru Mahanagara Palike (BBMP) Sahaaya Helpline**: `080-22660000`
     - **Udupi CMC** & **Mysuru City Corporation**
   - One-tap WhatsApp launch, phone dialer integration, clipboard copy, and system share.

---

## 🏗️ Architecture & Technology Stack

- **Target OS**: Android (Min SDK 26, Target SDK 35)
- **Language**: Kotlin 2.1
- **UI Framework**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Computer Vision**: On-device native edge luminance gradient & road cavity contour analysis
- **Spatial Algorithms**: Haversine distance, spherical compass bearing, and directional field-of-view cone
