# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Security Hardening: Preserve cryptographic and security providers
-keep class androidx.security.crypto.** { *; }
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }

# Preserve Room database models and schemas
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase

# Preserve data models and JSON serialization
-keepclassmembers class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.db.** { *; }

# Network & TLS security rules
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

