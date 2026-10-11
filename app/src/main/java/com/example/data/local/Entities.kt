package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val poster: String?,
    val backdrop: String?,
    val year: String?,
    val rating: String?,
    val genres: String?,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String, // format: "movie_{id}" or "series_{id}_{season}_{episode}"
    val mediaId: String,
    val title: String,
    val type: String,
    val poster: String?,
    val backdrop: String?,
    val season: Int? = null,
    val episode: Int? = null,
    val episodeTitle: String? = null,
    val streamUrl: String?,
    val streamTitle: String?,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lastWatchedAt: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val isFinished: Boolean
        get() = durationMs > 0 && positionMs >= (durationMs * 0.95)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :id)")
    fun isInWatchlist(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAll(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE positionMs > 5000 AND (durationMs == 0 OR positionMs < (durationMs * 0.92)) ORDER BY lastWatchedAt DESC LIMIT 15")
    fun getContinueWatching(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history WHERE mediaId = :mediaId ORDER BY lastWatchedAt DESC LIMIT 1")
    suspend fun getLatestByMediaId(mediaId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Database(entities = [WatchlistEntity::class, WatchHistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}
