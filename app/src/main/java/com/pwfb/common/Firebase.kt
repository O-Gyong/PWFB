package com.pwfb.common

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

object Firebase {
    val firebaseReference = Firebase.storage.reference
    val crashlytics = FirebaseCrashlytics.getInstance()
}