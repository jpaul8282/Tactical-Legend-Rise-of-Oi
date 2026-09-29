# ============================================================================
#  The Rise of the Officer Intelligence  -  R8 / ProGuard rules
#  Publisher: oistars&data (Amsterdam)
#  Generated: 2026-09-29
#
#  Place this file in:  app/proguard-rules.pro
# ============================================================================

# ----------------------------------------------------------------------------
# 1. GENERAL OPTIMIZATION & OBFUSCATION SETTINGS
# ----------------------------------------------------------------------------
-optimizationpasses 5
-allowaccessmodification
-repackageclasses 'o'
-overloadaggressively
-dontusemixedcaseclassnames
-verbose

# Keep useful attributes (generics, annotations, inner classes, etc.)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# Readable crash reports (Play Console / Crashlytics) after deobfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile


# ----------------------------------------------------------------------------
# 2. STRIP DEBUG LOGGING FROM RELEASE BUILDS
# ----------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}


# ----------------------------------------------------------------------------
# 3. ANDROID FRAMEWORK ESSENTIALS & ROOM DATABASE
# ----------------------------------------------------------------------------
# Native methods (JNI)
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Views referenced from XML with onClick
-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

# Custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Application entry points
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# R class fields
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Room Database models and schemas
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase

# Security and Cryptography
-keep class androidx.security.crypto.** { *; }
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }


# ----------------------------------------------------------------------------
# 4. KOTLIN & COROUTINES
# ----------------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**


# ----------------------------------------------------------------------------
# 5. GAME DATA MODELS (JSON / save games / config)
#    Keep every class that is serialized or deserialized by name.
# ----------------------------------------------------------------------------
-keep class com.example.data.** { *; }
-keep class com.example.data.model.** { *; }
-keep class com.example.data.db.** { *; }
-keepclassmembers class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.db.** { *; }

# Legacy and mapped aliases
-keep class com.oistarsdata.officerintelligence.** { *; }

# Keep classes that use @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}


# ----------------------------------------------------------------------------
# 6. GSON (uncomment if used)
# ----------------------------------------------------------------------------
#-keep class com.google.gson.** { *; }
#-keep class * extends com.google.gson.TypeAdapter
#-keep class * implements com.google.gson.TypeAdapterFactory
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer
#-keepclassmembers,allowobfuscation class * {
#    @com.google.gson.annotations.SerializedName <fields>;
#}


# ----------------------------------------------------------------------------
# 7. MOSHI / KOTLINX SERIALIZATION
# ----------------------------------------------------------------------------
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}


# ----------------------------------------------------------------------------
# 8. NETWORKING (OkHttp / Retrofit)
# ----------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepattributes Signature, Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response


# ----------------------------------------------------------------------------
# 9. GOOGLE PLAY SERVICES / BILLING / FIREBASE
# ----------------------------------------------------------------------------
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.** { *; }
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.play.core.** { *; }


# ----------------------------------------------------------------------------
# 10. ADS (uncomment if used)
# ----------------------------------------------------------------------------
#-keep class com.google.android.gms.ads.** { *; }
#-dontwarn com.google.android.gms.ads.**


# ----------------------------------------------------------------------------
# 11. UNITY (uncomment ONLY if the game is built with Unity)
# ----------------------------------------------------------------------------
#-keep class com.unity3d.player.** { *; }
#-keep class com.unity3d.plugin.** { *; }
#-keep class bitter.jnibridge.** { *; }
#-keep class org.fmod.** { *; }
#-dontwarn com.unity3d.**


# ----------------------------------------------------------------------------
# 12. LIBGDX (uncomment ONLY if the game is built with libGDX)
# ----------------------------------------------------------------------------
#-keep class com.badlogic.gdx.** { *; }
#-dontwarn com.badlogic.gdx.**
#-keep class com.badlogic.gdx.backends.android.** { *; }


# ----------------------------------------------------------------------------
# 13. WEBVIEW JAVASCRIPT INTERFACE (uncomment if used)
# ----------------------------------------------------------------------------
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}


# ----------------------------------------------------------------------------
# 14. SILENCE COMMON WARNINGS
# ----------------------------------------------------------------------------
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn sun.misc.Unsafe
