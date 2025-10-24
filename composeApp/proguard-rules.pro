-dontwarn androidx.**
-dontwarn kotlinx.**
-dontwarn kotlin.**
-dontwarn org.jetbrains.**
-dontwarn org.intellij.**
-dontwarn org.openjdk.**
-dontwarn com.sun.**
-dontwarn java.awt.**
-dontwarn javax.**
-dontwarn org.slf4j.**
-dontwarn org.apache.**

# Ignore missing optional ImageMagick bindings
-dontwarn magick.**
-dontwarn com.twelvemonkeys.image.**
-dontwarn com.twelvemonkeys.**

# Ignore SLF4J optional bindings
-dontwarn org.slf4j.impl.**

# Keep public ImageIO plugins
-keep class com.twelvemonkeys.imageio.** { *; }
-keep class com.twelvemonkeys.io.** { *; }

# Keep SLF4J interfaces
-keep class org.slf4j.** { *; }

# --- Room Database ---
-keep class androidx.room.** { *; }

# Keep generated database implementations
-keep class **_Impl { *; }

# Sometimes needed when reflection is used in Room queries
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

-keep class com.nullinnix.clippr.misc.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }
-keep class com.sun.jna.** { *; }
-keepattributes InnerClasses, EnclosingMethod, Signature