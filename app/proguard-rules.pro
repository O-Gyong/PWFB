# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Crashlytics 스택트레이스 복원을 위해 라인 번호 정보를 유지하고, 원본 파일명은 숨긴다.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
