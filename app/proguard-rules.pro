# Keep Room schema and entities
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep models for JSON serialization during P2P sync
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
