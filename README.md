# 🌿 Marley Maps – Kotlin Android App

**Marley Maps** is a Kotlin-based Android application that helps users find nearby cannabis dispensaries using the **Google Places API**.  
The app provides location details, reviews, and contact actions such as **call** or **get directions** — all within a clean, mobile-friendly UI.

---
Github link: https://github.com/LiamSteyn/MapsPOE.git

## 🧠 Features

- **Google Places API Integration** – Displays real-world dispensary data with ratings and reviews.
- **Dynamic Sorting** – Sort dispensaries by *distance* or *rating* via Material Design chips.
- **Details Screen** – Shows each dispensary’s logo, address, star rating, and latest review.
- **Interactive Actions** – Buttons to call the dispensary or open Google Maps for navigation.
- **Local Repository** – Offline-safe local mock data structure in `DataRepository.kt`.
- **Polished UI** – Material-inspired layout with consistent color palette and responsive scaling.

---

## 📁 Project Structure

| Directory | Description |
|------------|-------------|
| `data/` | Contains `DataRepository`, `Dispensary`, and API helper classes |
| `ui.theme/` | Activity logic and layout connections |
| `res/layout/` | XML UI files (`activity_dashboard`, `activity_dispensary_details`, etc.) |
| `strings.xml` | Localized text and button labels |
| `AndroidManifest.xml` | Permissions and activity declarations |

---

## ⚙️ Technologies Used

- **Language:** Kotlin  
- **IDE:** Android Studio (Giraffe+ version)  
- **Architecture:** Simple MVC pattern  
- **APIs:** Google Places API, Google Maps Intent  
- **UI:** Material Components, ConstraintLayout / LinearLayout  
- **Testing:** Android Emulator (Pixel 6 API 34)

---

## 🚀 How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/<yourusername>/MarleyMaps.git
   cd MarleyMaps

	2.	Open in Android Studio.
	3.	Add your Google API Key:
	•	Open PlacesClient.kt
	•	Replace YOUR_API_KEY_HERE with your real key.
	4.	Run on an emulator or connected Android device.

🎬 Video Demonstration
https://youtu.be/OPLa8pqZdq4

🧩 References
	•	Google Places API Documentation (https://developers.google.com/maps/documentation/places/web-service/overview)
	•	Android Developer Docs (https://developer.android.com)
	•	Material Design Guidelines (https://m3.material.io)
	•	Kotlin Language Reference (https://kotlinlang.org/docs/home.html)

🤖 AI Assistance Statement

This project was independently coded and designed by Reuven-Jon Kadalie, Ethan Smith, Ethan Buck, Liam Coetze.
ChatGPT was used only for assistance with:
	•	refining XML styling and UI layouts,
	•	debugging certain Kotlin functions, and
	•	guidance on implementing the Google Places API integration pattern.

All logic and code structure were implemented and tested manually.

  
