plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.community"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.community"
        minSdk = 24
        targetSdk = 36
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

configurations.all {
    // Nuke protobuf-lite from every configuration (main, test, androidTest)
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
    resolutionStrategy {
        // Force javalite everywhere so no variant can downgrade it
        force("com.google.protobuf:protobuf-javalite:3.25.1")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.picasso:picasso:2.8")


    // Firebase BOM (keeps Firebase libs consistent)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // Firebase with explicit excludes (main APK)
    implementation("com.google.firebase:firebase-firestore") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation("com.google.firebase:firebase-auth") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    // In your dependencies block
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Use the latest version
    implementation("com.google.firebase:firebase-storage")
// Note: You likely won't need the exclude block anymore (see point #2)


    // Protobuf runtime — add explicitly to all classpaths
    implementation("com.google.protobuf:protobuf-javalite:3.25.1")
    implementation(libs.play.services.maps)
    testImplementation("com.google.protobuf:protobuf-javalite:3.25.1")
    androidTestImplementation("com.google.protobuf:protobuf-javalite:3.25.1")

    // Other libraries
    implementation("com.squareup.picasso:picasso:2.8")

    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.1.0")
    implementation("androidx.core:core:1.13.1")

    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("com.google.android.gms:play-services-tasks:18.0.2")

    // Instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.fragment:fragment-testing:1.6.2")
    androidTestImplementation("org.mockito:mockito-android:5.5.0")
    androidTestImplementation("com.google.android.gms:play-services-tasks:18.0.2")
}