# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.android.org.conscrypt.** { *; }
-keep class org.conscrypt.** { *; }
# Mantiene le classi annotate con @Expose
-keep class com.google.gson.annotations.Expose { *; }

# Mantiene le classi annotate con @SerializedName
-keep class com.google.gson.annotations.SerializedName { *; }

# Mantiene tutte le classi del pacchetto specificato (sostituisci 'tuo.pacchetto' con il nome effettivo)
-keep class com.example.digitml.** { *; }
-keepclassmembers class com.example.digitml.** { *; }
-dontwarn com.android.org.conscrypt.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.openjsse.net.ssl.OpenJSSE