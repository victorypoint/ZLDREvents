-keep class com.victorypoint.zldrevents.data.** { *; }
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
