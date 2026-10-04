# Keep Room entity and DAO class names (Room uses reflection at runtime)
-keep class com.yourfirm.autoreply.db.** { *; }

# Keep Retrofit interface methods
-keepattributes Signature
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Gson model classes (used by Retrofit for JSON parsing)
-keep class com.yourfirm.autoreply.api.models.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
