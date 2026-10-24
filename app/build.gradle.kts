plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.solarsehat"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.solarsehat"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        noCompress += "tflite"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    // TensorFlow Lite interpreter (Only raw org.tensorflow:tensorflow-lite)
    // Note for viva: check Maven repository for the latest stable version of tensorflow-lite.
    implementation(libs.tensorflow.lite)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}