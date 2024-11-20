import java.util.Properties

val secretsProps = Properties()
val secretsFile = file("../secret.properties")
secretsFile.inputStream().use { secretsProps.load(it)}

plugins {
    id("com.android.application")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.detectapplication2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.detectapplication2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val accessKeyID = secretsProps["Access_Key_ID"] as? String
        val accessKeySecret = secretsProps["Access_Key_Secret"] as? String
        buildConfigField("String", "ACCESS_KEY_ID", "\"accessKeyID\"")
        buildConfigField("String", "ACCESS_KEY_SECRET", "\"accessKeySecret\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            buildConfigField("String", "ACCESS_KEY_ID", "\"accessKeyID\"")
            buildConfigField("String", "ACCESS_KEY_SECRET", "\"accessKeySecret\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"), "exclude" to listOf("*mock*.jar"))))
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)
    implementation(libs.volley)
    implementation(libs.picasso)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}