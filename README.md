# SolarSehat: AI-Based Rooftop Solar Panel Defect Analyzer
> **Course Assignment**: AI-Based Mini Application Development: Integrating ML/DL Models into Android with TensorFlow Lite  
> **Original Reference Repository**: [NumberGuessingAppTflite](https://github.com/Uzair390-Del/NumberGuessingAppTflite.git)  
> **Tech Stack**: Pure Java, Android XML Layouts, TensorFlow Lite (TFLite), YOLO (v5n/v8n/v11n), MobileNetV2, SQLite, Material Components.

---

## 📌 1. Project Overview & Objectives

**SolarSehat** is an offline, AI-powered Android application designed to inspect rooftop solar panels, detect surface defects (dust accumulation, bird droppings, physical cracking, and electrical hot-spot damage), estimate power generation loss, and calculate monthly financial losses in Pakistani Rupees (PKR).

### **Key Objectives**:
1. **On-Device Inference**: Perform real-time object detection and classification locally on Android without requiring an internet connection or cloud API.
2. **Dual-AI Architecture**: Combine **YOLO** (bounding box defect detection) and **MobileNetV2** (overall condition classification) with a decision-fusion algorithm.
3. **Financial Loss Estimation**: Convert physical defect severity into actionable financial metrics ($kW \times Sun Hours \times 30 \times Loss \% \times Tariff Rate$).
4. **Local Data Persistence**: Maintain inspection reports locally using **SQLite** (`ReportDatabaseHelper`) and system configurations using `SharedPreferences`.

---

## 🔬 2. Conceptual Framework (Part A Analysis)

### **A1. Embedding ML/DL Models in Android Applications**
Embedding Machine Learning (ML) and Deep Learning (DL) models directly into mobile applications enables **on-device ML inference** (Edge AI). Instead of sending image frames over HTTP/REST to a remote cloud server, the Android application processes camera sensor data locally using hardware accelerators (GPU/NNAPI/Hexagon DSP).

* **Benefits**:
  1. **Low Latency**: Near-instantaneous predictions (15–40 ms) by avoiding network round-trip delay.
  2. **Offline Operation**: Essential for field inspections in rural or rooftop environments lacking cellular connectivity.
  3. **Privacy & Security**: Raw inspection photos remain on the user's phone, complying with strict data protection standards.
  4. **Zero Cloud Costs**: Eliminates recurring server hosting and API execution expenses.
* **Challenges**:
  1. **Hardware Constraints**: Limited RAM, thermal throttling, and battery consumption on mobile devices.
  2. **Model Size**: DL models must be compressed (quantization/pruning) to fit within mobile storage limits.
  3. **Device Variability**: Heterogeneous hardware architectures across Android OEMs.

---

### **A2. TensorFlow Lite (TFLite) vs. Standard TensorFlow**
**TensorFlow Lite (TFLite)** is Google’s lightweight runtime library engineered specifically for executing machine learning models on mobile, embedded, and IoT devices.

| Metric / Feature | Standard TensorFlow | TensorFlow Lite (TFLite) |
| :--- | :--- | :--- |
| **Primary Environment** | Cloud Servers, Workstations, High-End GPUs | Android, iOS, Embedded Edge Devices |
| **Model Format** | SavedModel / Checkpoint directory (`.pb`) | FlatBuffer Single Binary (`.tflite`) |
| **Binary Memory Footprint** | Hundreds of Megabytes | ~1 MB C++ Runtime Library |
| **Execution Engine** | Full TensorFlow C++ Operator Library | Light C++ Interpreter with selective op registration |
| **Hardware Acceleration** | CUDA, TensorRT, ROCm | NNAPI, GPU Delegate, Hexagon DSP Delegate |

#### **Justification for TFLite in SolarSehat**:
- **FlatBuffer Architecture**: `.tflite` files can be memory-mapped (`mmap`) directly from the APK asset folder without heap allocation overhead or uncompression delay.
- **Quantization Support**: FP16 and INT8 quantization reduce model size by 50%–75% while maintaining mAP accuracy.

---

### **A3. Step-by-Step TFLite Integration Process in Android**

```
[PyTorch / YOLO / Keras Model] ──► [TFLite Converter] ──► [.tflite File in assets/] ──► [TFLite Interpreter] ──► [UI Display]
```

1. **Model Conversion**: Convert trained PyTorch (`.pt`) or TensorFlow (`.pb`) weights to `.tflite` format using `tf.lite.TFLiteConverter` or `yolo export format=tflite`.
2. **Asset & Dependency Setup**:
   - Place `.tflite` files into `app/src/main/assets/`.
   - Configure `app/build.gradle.kts` with `androidResources { noCompress += "tflite" }`.
   - Declare dependency: `implementation("org.tensorflow:tensorflow-lite:2.16.1")`.
3. **Interpreter Initialization**: Load model buffer into memory using `FileChannel.map()` and instantiate `new Interpreter(byteBuffer)`.
4. **Input Preparation**:
   - Resize camera photo to model dimensions (`320x320` for YOLO, `224x224` for MobileNetV2).
   - Apply gray-114 letterboxing to preserve aspect ratio (`ImageUtils.letterboxBitmap`).
   - Extract RGB pixel values into `ByteBuffer` with `ByteOrder.nativeOrder()`.
5. **Running Inference**: Invoke `interpreter.run(inputBuffer, outputArray)`.
6. **Output Post-Processing**: Decode box coordinates (`cx, cy, w, h`), filter confidence scores ($\ge 0.25$), and execute Non-Maximum Suppression (NMS IoU $\le 0.45$).

---

## 📂 3. Comparison with Provided Reference Repository (Part B Tasks 1 & 2)

| Aspect | Reference Repo (`NumberGuessingAppTflite`) | Developed Application (`SolarSehat`) |
| :--- | :--- | :--- |
| **Use Case** | Single digit classification (`0–9`) on drawn canvas | Multi-class rooftop solar panel defect detection & financial analysis |
| **AI Models Used** | Single custom CNN MNIST model (`digit_model.tflite`) | Dual pipeline: YOLO (`v5n/v8n/v11n`) + MobileNetV2 (`panel_classifier`) |
| **Input Shape** | Single channel 28x28 Grayscale float32 (`[1, 28, 28, 1]`) | 3-Channel RGB 320x320 (`[1, 320, 320, 3]`) & 224x224 (`[1, 224, 224, 3]`) |
| **Output Format** | 1D Array of 10 class probabilities (`[1, 10]`) | 3D Array of bounding boxes + class confidences (`[1, 84, 2100]`) |
| **Post-Processing** | `argmax()` to identify highest confidence class | Anchor decoding, coordinate scaling, NMS IoU filtering |
| **Data Storage** | Transient state (no database) | **SQLite Database** (`ReportDatabaseHelper`) + `SharedPreferences` |

---

## 🛠️ 4. Practical Implementation & Features (Task 3)

### **Architecture & Screen Navigation Flow**
1. **`SplashActivity`**: 2-second splash screen, asset verifier, progress bar.
2. **`WelcomeActivity`**: Onboarding overview, *"Get Started"* action.
3. **`LoginActivity`** & **`RegisterActivity`**: Authentication options including *"Sign in as Guest"*.
4. **`HomeActivity` (Main Dashboard)**:
   - Top Bar with Hamburger Drawer icon, title *"Solar Sehat"*, and Three-Dot Overflow Menu.
   - Left Navigation Drawer (`DrawerLayout` + `NavigationView`).
   - Greeting & Solar Panel Status card (Power Generation $kW$, Health Score $92\%$, Online tag).
   - Quick Action Cards (*"Scan Panel"* & *"Health Report"*).
   - Recent Analysis card loading latest scan from SQLite database.
   - Bottom Navigation Bar (*Home, Scan, Reports, Settings*).
5. **`ScanActivity`**: Camera capture via `FileProvider`, gallery picker, YOLO model selector (`YOLOv8n`/`YOLOv5n`/`YOLO11n`), live bounding box `OverlayView`.
6. **`ResultActivity`**: Annotated image display, fused condition output, power loss $\%$, monthly PKR loss calculation, plain-English cleaning advice, *"Save Report"* to SQLite, *"Share Report"*, and *"Dial Cleaner"*.
7. **`ReportsActivity`**: SQLite-backed inspection history with filter tabs (*All, Healthy, Attention*).
8. **`SettingsActivity`**: Account settings, Light theme enforcement, AI model precision selection (`FP16`/`FP32`), and editable tariff/kW configuration.
9. **`BenchmarkActivity`**: 10-iteration latency benchmark, `metrics.csv`, and `model_info.txt` display.

---

## 🧮 5. Financial & Fusion Algorithm (`LossTable.java`)

### **Fusion Decision Rule**:
$$\text{Primary Condition} = \begin{cases} \text{Classifier Class}, & \text{if Classifier Confidence} \ge 0.50 \\ \text{Top YOLO Class}, & \text{otherwise} \end{cases}$$

### **Defect Loss Fractions**:
- **Clean**: $0\%$ output loss
- **Dusty**: $15\%$ output loss
- **Bird Drop**: $25\%$ output loss
- **Electrical Damage**: $40\%$ output loss
- **Physical Damage**: $50\%$ output loss

### **Monthly Financial Loss Formula (PKR)**:
$$\text{Monthly Loss (Rs)} = \text{System Size (kW)} \times \text{Peak Sun Hours} \times 30 \text{ days} \times \text{Max Loss Fraction} \times \text{Tariff Rate (Rs/unit)}$$

---

## 🚀 6. Step-by-Step Installation, Setup, & Execution Guide

### **Prerequisites**:
- **IDE**: Android Studio Jellyfish / Koala / Ladybug or newer.
- **JDK**: Java Development Kit 17 (included in Android Studio).
- **Android SDK**: API Level 24 (Android 7.0) minimum, API Level 34 target.
- **Hardware**: Android physical device (with USB Debugging enabled) or Android Virtual Device (AVD Emulator).

---

### **Step 1: Clone the GitHub Repository**
Open Terminal or PowerShell and run:
```bash
git clone https://github.com/your-username/SolarSehat.git
cd SolarSehat
```

---

### **Step 2: Open and Sync in Android Studio**
1. Launch **Android Studio**.
2. Click **Open** and select `C:/Users/Humna/AndroidStudioProjects/SolarSehat`.
3. Allow Gradle to automatically download dependencies and sync the project.

---

### **Step 3: Run / Deploy the Application**
- Connect your Android phone via USB (ensure USB Debugging is ON) or start an AVD Emulator.
- Select `:app` in the top toolbar run configuration.
- Click the green **Play (▶)** button or press `Shift + F10`.

---

### **Step 4: Training & Exporting Custom Models to `.tflite` (Optional)**
If you wish to re-train the YOLO object detection model or MobileNetV2 classifier:

1. **Train YOLOv8 using Ultralytics**:
   ```python
   from ultralytics import YOLO

   # Train YOLOv8n on solar panel defect dataset
   model = YOLO("yolov8n.pt")
   model.train(data="solar_defects.yaml", epochs=50, imgsz=320)

   # Export model to TensorFlow Lite FP16 format
   model.export(format="tflite", half=True)
   ```
2. **Copy to Android Project**:
   Copy the exported `yolov8n_saved_model/yolov8n_float16.tflite` file into `app/src/main/assets/yolov8n.tflite`.

---

## 🎓 7. Viva Cheat Sheet & Evaluation Q&A

1. **Q: Why is TFLite used instead of calling a cloud API?**  
   *A: On-device inference guarantees $100\%$ offline operation in remote field environments, reduces latency to $<30\text{ ms}$, and eliminates recurring server hosting fees.*

2. **Q: How are memory overflow (OOM) errors prevented when taking high-res camera photos?**  
   *A: `ImageUtils.decodeSampledBitmapFromFile()` calculates `inSampleSize` power-of-two downsampling prior to loading pixel byte buffers into RAM.*

3. **Q: How is local data stored without Firebase?**  
   *A: Inspection reports are stored in an **SQLite Database** using `ReportDatabaseHelper.java`, and user preferences are stored in **`SharedPreferences`** (`SolarSehatPrefs`).*

4. **Q: How does the YOLO output tensor work in Java?**  
   *A: YOLOv8 outputs shape `[1, 84, 2100]` where `84` represents 4 bounding box coordinates (`cx, cy, w, h`) + 80 class confidence scores across `2100` candidate anchors. The algorithm iterates through candidate boxes, extracts maximum class probability, and applies NMS filtering.*

---

## 📄 8. License & Acknowledgments
- Developed as a university mini-project for AI-based Android development.
- Built upon concepts studied from [NumberGuessingAppTflite](https://github.com/Uzair390-Del/NumberGuessingAppTflite.git).
- Powered by **TensorFlow Lite** and **Ultralytics YOLO**.
