import jdk.tools.jlink.resources.plugins

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.framework.crash"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":framework-core"))
    implementation(project(":framework-logger"))

    // ACRA 崩溃上报核心
    implementation(libs.acra.core)
    // HTTP 上报（上报到自建服务器）
    implementation(libs.acra.http)
    // Debug 时用 Toast 提示崩溃
    implementation(libs.acra.toast)
    // 可选：邮件上报
    // implementation(libs.acra.mail)
    // 可选：通知栏展示崩溃信息
    // implementation(libs.acra.notification)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

