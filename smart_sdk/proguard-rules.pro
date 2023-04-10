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

# Parceler library
-keep interface org.parceler.Parcel
-keep @org.parceler.Parcel class * { *; }
-keep class **$$Parcelable { *; }

-keep class kg.onoi.smart_sdk.models.** { *; }
-keep class kg.onoi.smart_sdk.recognition.** { *; }


-keepclasseswithmembers class kg.onoi.smart_sdk.SmartID { *; }
-keepclasseswithmembers class kg.onoi.smart_sdk.utils.SdkConfig { *; }
-keepclasseswithmembers class kg.onoi.smart_sdk.utils.SdkHelper { *; }
-keepclasseswithmembers class kg.onoi.smart_sdk.utils.Config { *; }
-keepclasseswithmembers enum kg.onoi.smart_sdk.utils.SignType { *; }
-keepclasseswithmembers enum kg.onoi.smart_sdk.utils.ResponseWay { *; }

-keepclasseswithmembers enum kg.onoi.smart_sdk.network.Status { *; }
-keepclasseswithmembers class kg.onoi.smart_sdk.network.Response { *; }

-libraryjars libs/jniSmartIdEngineJar.jar
-libraryjars libs/native-libs.jar

-keep class biz.smartengines.** { *; }