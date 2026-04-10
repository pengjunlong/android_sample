
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// 支持从命令行参数注入版本号（GitHub Actions Release 工作流使用）
// 例：./gradlew assembleRelease -PversionName=1.2.3 -PversionCode=10203
val ciVersionName: String = findProperty("versionName")?.toString() ?: "1.0.0"
val ciVersionCode: Int = findProperty("versionCode")?.toString()?.toIntOrNull() ?: 1

android {
    namespace = "com.pengjunlong.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.pengjunlong.app"  // TODO: 必须全局唯一，决定 App 安装覆盖/共存，接入新项目时务必修改
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = ciVersionCode
        versionName = ciVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ── TODO: 接入新项目时按需修改以下配置常量 ──────────────────────────
        // API 服务地址（可按 buildType 分别配置 debug/release 不同地址）
        buildConfigField("String", "API_BASE_URL", "\"https://jsonplaceholder.typicode.com/\"")
        // GitHub 仓库信息（用于 BaseActivity 菜单中的「检查更新」功能）
        buildConfigField("String", "UPDATE_REPO_OWNER", "\"pengjunlong\"")   // TODO: 改为自己的 GitHub 用户名/组织
        buildConfigField("String", "UPDATE_REPO_NAME",  "\"android_sample\"") // TODO: 改为自己的仓库名
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        viewBinding = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    // ── APK 输出命名：自动从 strings.xml 读取 app_name ─────────────────────────
    // Debug  → {app_name}-debug.apk
    // Release → {app_name}-release.apk
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            // 读取 app/src/main/res/values/strings.xml 中的 app_name
            val stringsXml = file("src/main/res/values/strings.xml")
            val appName = if (stringsXml.exists()) {
                val content = stringsXml.readText()
                Regex("<string name=\"app_name\">(.*?)</string>").find(content)?.groupValues?.get(1) ?: "app"
            } else "app"
            output.outputFileName = "${appName}-${variant.buildType.name.lowercase()}.apk"
        }
    }
}

dependencies {
    // 框架模块
    implementation(project(":framework-core"))
    implementation(project(":framework-crash"))
    implementation(project(":framework-logger"))
    implementation(project(":framework-network"))
    implementation(project(":framework-storage"))
    implementation(project(":framework-ui"))

    // 下拉刷新
    implementation(libs.androidx.swiperefreshlayout)

    // 图片加载
    implementation(libs.glide)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)
}

