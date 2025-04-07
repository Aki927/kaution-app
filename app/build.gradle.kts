import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.cs.kaution"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cs.kaution"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
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
}

dependencies {
    implementation(libs.ccp)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.firebase.auth)

    implementation(libs.play.services.location)

    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom.v3380))

    // Add the dependency for the Cloud Storage library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.storage)

    implementation(libs.google.services)

    implementation(libs.geofire.android.common)

    implementation(libs.firebase.messaging)

    implementation(libs.google.auth.library.oauth2.http)

    //implementation(libs.google.firebase.appcheck.playintegrity)
    //implementation(libs.firebase.appcheck)
    //implementation(libs.firebase.appcheck.debug)

    implementation(libs.retrofit)
    implementation(libs.converter.moshi)

    implementation(libs.firebase.ui.firestore)

    implementation(libs.glide)

    annotationProcessor(libs.compiler)



}