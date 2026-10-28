// Top-level build file where you can add configuration options common to all sub-projects/modules.
// 버전은 gradle/libs.versions.toml에서 관리한다.
plugins {
    alias(libs.plugins.android.application) apply false
    // 모듈에는 적용하지 않음. AGP 내장 Kotlin이 사용할 Kotlin(KGP) 버전을 지정하기 위한 선언.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
