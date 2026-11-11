package com.imdadburki.noctra.core.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** A saved wallpaper design (built-in favourite, download or user project). */
@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val collection: String,
    val creator: String,
    val backgroundHex: Long,
    val backgroundHex2: Long?,
    val gradientAngle: Float,
    val clockKind: String,
    val digitalStyle: String,
    val analogStyle: String,
    val is24Hour: Boolean,
    val showSeconds: Boolean,
    val colorHex: Long,
    val accentHex: Long,
    val clockScale: Float,
    val isFavorite: Boolean,
    val isDownloaded: Boolean,
    val isMyDesign: Boolean,
    val createdAtMs: Long,
    val lastViewedMs: Long
)

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY lastViewedMs DESC")
    fun getAll(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY lastViewedMs DESC")
    fun getFavorites(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isDownloaded = 1 ORDER BY createdAtMs DESC")
    fun getDownloaded(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isMyDesign = 1 ORDER BY createdAtMs DESC")
    fun getMyDesigns(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getById(id: String): WallpaperEntity?

    @Query("SELECT * FROM wallpapers WHERE title LIKE '%' || :q || '%' OR collection LIKE '%' || :q || '%' ORDER BY lastViewedMs DESC")
    fun search(q: String): Flow<List<WallpaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WallpaperEntity)

    @Update
    suspend fun update(item: WallpaperEntity)

    @Delete
    suspend fun delete(item: WallpaperEntity)

    @Query("DELETE FROM wallpapers WHERE id = :id")
    suspend fun deleteById(id: String)
}




@Database(
    entities = [WallpaperEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao
}

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "noctra.db"
            ).fallbackToDestructiveMigration(dropAllTables = true).build().also { instance = it }
        }
    }
}
