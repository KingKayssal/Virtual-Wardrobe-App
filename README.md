# Virtual Wardrobe App

## Overview
This is a Java Swing application that allows users to manage a virtual wardrobe, create outfits, and receive fashion advice. The main interface is graphical, featuring a mannequin that displays selected clothing items.

## How It Works
- **Wardrobe Management:**
  - Users can add clothing items to their wardrobe, each with details like name, category, size, color, image, and season.
  - Clothing items are displayed in a grid for easy browsing.

- **Outfit Creation:**
  - Users can select items from their wardrobe to create an outfit.
  - The current outfit is shown both as a list and visually on a mannequin.
  - Special handling is provided for dresses, which are drawn larger on the mannequin.

- **Seasonal Suggestions:**
  - The app can suggest outfits based on the current season (e.g., Fall, Winter).
  - Users can switch seasons to get different outfit recommendations.

- **Fashion Advice:**
  - A dedicated area displays fashion tips and advice based on the selected outfit and season.

- **Mannequin Rotation:**
  - The mannequin can be rotated to view outfits from different angles using an animated rotation feature.

## How to Run
1. Ensure you have Java installed (JDK 8 or higher).
2. Compile the `WardrobeAppUI.java` file:
   ```
   javac WardrobeAppUI.java
   ```
3. Run the application:
   ```
   java WardrobeAppUI
   ```

## Features
- Add, view, and manage clothing items
- Create and visualize outfits
- Get seasonal outfit suggestions
- Receive fashion advice
- Interactive mannequin display

## Requirements
- Java JDK 8 or higher
- No external dependencies required


## Notes
- Images for clothing items should be provided as `ImageIcon` objects in the code.
- The application is for demonstration and educational purposes.

## Technical Details
- The app is built using Java Swing for the graphical user interface.
- All wardrobe data is stored in memory while the app is running; there is no persistent storage by default.
- Clothing items are represented by the `ClothingItem` class, which includes properties for name, category, size, color, image, and season.
- The mannequin is custom drawn using Java 2D graphics, and outfits are rendered visually on it.

## User Interface
- The main window displays:
  - A grid of wardrobe items (with images and details)
  - A mannequin panel showing the current outfit
  - An outfit panel listing selected items
  - A text area for fashion advice
- Users interact with the app using buttons, selection panels, and can rotate the mannequin for a better view.

## Extending the App
- You can add new categories, seasons, or features by modifying the `ClothingItem` class and related UI components.
- To add persistent storage, consider integrating file I/O or a database to save wardrobe data.
- For advanced graphics, you may use JavaFX or external libraries.

## Troubleshooting
- If the app does not start, ensure you have the correct version of Java installed and your environment variables are set.
- If images do not display, check that the image paths or resources are correctly referenced in your code.

---
For more details, refer to `Ultimate_Wardrobe_Guide.docx`.
