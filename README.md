# RideVision 🚗💨 — CV-Based Pothole Detection & Proactive Road Safety Web Application

> **Civic Tech & Computer Vision Road Safety System**  
> Rewritten as a modern React application with TypeScript & Tailwind CSS (Emerald Telemetry Design System)

RideVision is an intelligent road safety platform that bridges the gap between daily commuters and municipal road authorities. RideVision uses on-device Computer Vision to detect road surface hazards in real-time, alerts drivers of approaching hazards **250m ahead** using a directional bearing cone, maintains a crowd-verified repair lifecycle with **anti-gaming cooldowns**, and automatically formats complaints with GPS links for direct dispatch to municipal channels (e.g. **MCC Mangaluru Official WhatsApp**, **BBMP Bengaluru Helpline**).

---

## 🌟 Key Features Preserved & Implemented

1. **On-Device Computer Vision Hazard Detection (`Report` tab)**:
   - Real-time road cavity and defect detection with severity categorization (`Severe`, `Moderate`, `Minor`).
   - Visual telemetry: bounding boxes, confidence score, road condition rating, and frame latency.
   - Built-in evaluation test benches with realistic road samples (Severe Hazard, Moderate Hazard, Clean Road) as well as live camera capture and photo upload.
   - High-fidelity Futuristic HUD Tactical Reticle Overlay with live coordinate telemetry, depth estimation, and calibration badges.

2. **Proactive Hazard Warning System (250m Directional Radar Cone)**:
   - Evaluates vehicle GPS position and heading relative to known hazards using Haversine distance and bearing cone matching (+/- 45° field of view).
   - Audio-visual alert banner surfacing distance, severity, street address, and safe distance guidance.

3. **Safe Route Corridor Planning & Google Maps Telemetry Grounding (`Safe Route` tab)**:
   - Vector Map Canvas rendering road arterials, glowing neon safe route ribbons, and danger route corridors.
   - Real-time Google Maps telemetry grounding analyzing road surface conditions, construction zones, and pothole clusters.
   - Interactive 3D tilt, zoom controls, waypoint inversion, and transport modes (`Ride`, `Car`, `Fleet`).

4. **Crowd-Verified Hazard Lifecycle & Anti-Gaming Cooldown (`History` tab)**:
   - 15-meter spatial deduplication: prevents duplicate entries by automatically linking nearby reports to existing hazard records.
   - 4-grid Hazard Registry analytics bento: Total Reported, Resolved percentage, Municipal Review, and Critical Risk cases.
   - Commuter actions: "+1 Still There" confirmation upvotes and "Confirm Fixed" repair logging.

5. **Municipal Authority Grievance Dispatch**:
   - Pre-formatted structured complaint text containing location address, Google Maps link, severity rating, and timestamp.
   - Direct dispatch integration with:
     - **Mangaluru City Corporation (MCC) Official WhatsApp**: `+91 9449007722`
     - **Bruhat Bengaluru Mahanagara Palike (BBMP) Sahaaya Helpline**: `080-22660000`
     - **Udupi CMC** & **Mysuru City Corporation**

6. **Sentinel Cockpit Profile & Commuter Specifications (`Profile` tab)**:
   - Authenticated sentinel profile with Gold Guardian badge, precision score, and corridor patrol stats.
   - Commuter hardware link preferences: Earbud Chime Ahead & Handlebar Haptic Pulse toggles.
   - User-editable Commuter Specification (Vehicle model, specs, home sector, direct line, emergency ICE).

---

## 🏗️ Architecture & Technology Stack

- **Target Framework**: React 19 SPA (Vite)
- **Language**: TypeScript
- **Styling**: Tailwind CSS (Emerald Telemetry Palette: `#01180C`, `#FFD56D`, `#A4D1B6`)
- **Computer Vision**: In-browser client-side canvas luminance depression and cavity contour clustering
- **Spatial Algorithms**: Haversine distance, spherical compass bearing, and directional field-of-view cone
