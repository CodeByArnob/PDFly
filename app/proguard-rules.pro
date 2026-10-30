# Add project specific ProGuard rules here.
# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepattributes *Annotation*,InnerClasses,Signature

# Keep Donation & Token Config models
-keep class com.example.domain.model.** { *; }
-keep class com.example.data.local.** { *; }

# CameraX rules
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
