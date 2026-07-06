plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yiqiu.readingquiz"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yiqiu.readingquiz"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-alpha"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.material:material:1.12.0")
    // 协程（显式声明，用于 AiSettingsScreen 异步网络请求，避免 NetworkOnMainThreadException）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // 图片加载（Coil 2.x 兼容 Kotlin 1.9.24；3.x 需要 Kotlin 2.0+，不兼容）
    implementation("io.coil-kt:coil-compose:2.7.0")
    // Markdown 渲染（0.5.8 基于 coil2；0.6.0+ 迁移到 coil3 不兼容 Kotlin 1.9.24）
    implementation("com.github.jeziellago:compose-markdown:0.5.8")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}