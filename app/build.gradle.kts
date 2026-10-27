import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
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
    compileSdk = 37

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        // 레이아웃에 바인딩 표현식(@{})을 쓰지 않으므로 DataBinding 대신 ViewBinding 사용 (kapt 불필요)
        viewBinding = true
    }

    lint {
        // baseline 없이 운영한다. 경고는 CI 로그로 확인하고, 오류만 빌드를 막는다.
        abortOnError = true
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Splash Screen
    implementation(libs.androidx.splashscreen)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Preferences DataStore
    implementation(libs.androidx.datastore.preferences)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // 애드몹
    implementation(libs.play.services.ads)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.storage)

    implementation(libs.app.update.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
