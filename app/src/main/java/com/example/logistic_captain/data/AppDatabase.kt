package com.example.logistic_captain.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "location_updates")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "pod_uploads")
data class PodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deliveryId: String,
    val signature: String?,
    val imagePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationEntity)

    @Query("SELECT * FROM location_updates WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedLocations(): List<LocationEntity>

    @Query("UPDATE location_updates SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)

    @Query("DELETE FROM location_updates WHERE isSynced = 1")
    suspend fun deleteSynced()
}

@Dao
interface PodDao {
    @Insert
    suspend fun insert(pod: PodEntity)

    @Query("SELECT * FROM pod_uploads WHERE isSynced = 0")
    suspend fun getUnsyncedPods(): List<PodEntity>

    @Query("UPDATE pod_uploads SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}

@Database(entities = [LocationEntity::class, PodEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun podDao(): PodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "logistic_captain_db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
