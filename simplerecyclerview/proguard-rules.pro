# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/jaychang/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-keep,allowobfuscation,allowshrinking class com.jaychang.srv.Utils
#-keepattributes Signature
-keep class java.lang.reflect.** { *; }
-keep class java.lang.reflect.** {   static public public private *; }
-keep class com.jaychang.srv.Utils { *; }

-dontwarn kotlin.reflect.jvm.internal.**

-keep class kotlin.reflect.jvm.internal.** { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Glide
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep public class * extends com.bumptech.glide.module.AppGlideModule

#-keepclassmembers public final class com.jaychang.srv.Utils {
#    public *;
#    static public *;
#}
#
#-keepclassmembers public class com.jaychang.srv.SimpleRecyclerView {
#    public *;
#    static public *;
#}
#
#-keepclassmembers class com.jaychang.srv.* {
#    <fields>;
#    <init>();
#    <methods>;
#}

#-keepclasseswithmembers class com.jaychang.srv.** {*;}
#-keepclasseswithmembernames class com.jaychang.srv.** {*;}