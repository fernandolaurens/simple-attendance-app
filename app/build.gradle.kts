plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.laurens.absensiapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.laurens.absensiapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("androidx.recyclerview:recyclerview:1.1.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.core:core-ktx:1.3.2")
//    implementation ( "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
//    implementation ("fileTree(dir: "libs", include: ["*.jar"])")


    implementation ("androidx.room:room-runtime:2.2.6")
    annotationProcessor ("androidx.room:room-compiler:2.2.6")
    kapt ("androidx.room:room-compiler:2.2.6")

    implementation ("com.google.android.material:material:1.0.0")
    implementation ("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.intuit.sdp:sdp-android:1.0.6")
}