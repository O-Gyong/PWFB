import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

// 릴리즈 서명 정보가 모두 있을 때만 release 서명을 설정 (없으면 debug 빌드/테스트는 그대로 가능)
val hasReleaseSigning = listOf("storeFile", "keyAlias", "keyPassword", "storePassword")
    .all { !localProperties.getProperty(it).isNullOrBlank() }

val adMobApiKey: String? = localProperties.getProperty("AdMobAPI_KEY")?.takeIf { it.isNotBlank() }

// Google 공식 테스트 앱 ID (크래시 방지)
val adMobTestAppId = "ca-app-pub-3940256099942544~3347511713"

// 릴리즈 빌드는 서명 정보와 AdMob 키가 없으면 빌드를 막음 (서명 안 된 APK, 광고 ID 없는 앱 배포 방지)
gradle.taskGraph.whenReady {
    val isReleaseBuild = allTasks.any {
        it.project == project && (it.name.startsWith("assembleRelease") || it.name.startsWith("bundleRelease"))
    }
    if (isReleaseBuild) {
        if (!hasReleaseSigning) {
            throw GradleException("릴리즈 빌드에는 local.properties의 storeFile, keyAlias, keyPassword, storePassword가 필요합니다.")
        }
        if (adMobApiKey == null) {
            throw GradleException("릴리즈 빌드에는 local.properties의 AdMobAPI_KEY가 필요합니다.")
        }
    }
}

android {
    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                keyAlias = localProperties.getProperty("keyAlias")
                keyPassword = localProperties.getProperty("keyPassword")
                storePassword = localProperties.getProperty("storePassword")
                storeFile = file(localProperties.getProperty("storeFile"))
            }
        }
    }
    namespace = "com.pwfb"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pwfb"
        minSdk = 29
        targetSdk = 36
        versionCode = 16
        versionName = "1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            manifestPlaceholders["AdMobAPI_KEY"] = adMobApiKey ?: adMobTestAppId
        }
        release {
            manifestPlaceholders["AdMobAPI_KEY"] = adMobApiKey ?: ""
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
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
        dataBinding = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        checkReleaseBuilds = false
    }
}

val hiltVersion = 2.46

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")

    // CalendarView
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")

    // CalendarView 년 월 표시
    implementation("com.jakewharton.threetenabp:threetenabp:1.2.0")

    // 애드몹
    implementation("com.google.android.gms:play-services-ads:22.6.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation("com.google.android.play:app-update-ktx:2.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}