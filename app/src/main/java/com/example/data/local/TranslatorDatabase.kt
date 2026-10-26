package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "translation_template_cache")
data class TranslationCacheEntity(
    @PrimaryKey val cacheKey: String, // "${domain.name}::$templatePattern"
    val domainName: String,
    val templatePattern: String,      // e.g. "造成{0}点火焰伤害，冷却{1}秒"
    val vietnameseTemplate: String,   // e.g. "Gây {0} điểm sát thương Hỏa, hồi chiêu {1} giây"
    val rawExampleChinese: String,
    val hitCount: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dynamic_glossary")
data class GlossaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chineseTerm: String,
    val vietnameseTerm: String,
    val domainCategory: String, // WUXIA_XIANXIA, SCIFI_ANIME_RPG, ECOMMERCE_SOCIAL, SYSTEM_UI
    val notes: String = ""
)

@Entity(tableName = "cloud_vault_projects")
data class CloudVaultProjectEntity(
    @PrimaryKey val projectId: String,
    val projectTitle: String,
    val geminiAccountEmail: String,
    val syncCode6Digit: String,       // e.g. "849-206"
    val sourceDeviceBadge: String,    // "PC GIẢ LẬP (LDPlayer)" or "ĐIỆN THOẠI ANDROID"
    val clipCount: Int,
    val totalDurationMs: Long,
    val resolution: String,
    val normX: Float,
    val normY: Float,
    val fontSizeSp: Float,
    val fontFamilyName: String,
    val maskModeName: String,
    val assSubtitleContent: String,
    val srtSubtitleContent: String,
    val hvCloudBundleJson: String,
    val glossaryTermCount: Int,
    val contextHistorySummary: String,
    val syncedAtEpochMs: Long = System.currentTimeMillis()
)

@Dao
interface TranslationCacheDao {
    @Query("SELECT * FROM translation_template_cache ORDER BY updatedAt DESC")
    fun observeAllCachedTemplates(): Flow<List<TranslationCacheEntity>>

    @Query("SELECT * FROM translation_template_cache WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getByCacheKey(cacheKey: String): TranslationCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplate(entity: TranslationCacheEntity)

    @Query("UPDATE translation_template_cache SET hitCount = hitCount + 1, updatedAt = :now WHERE cacheKey = :cacheKey")
    suspend fun incrementHitCount(cacheKey: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM translation_template_cache")
    suspend fun clearAllCache()

    @Query("SELECT COUNT(*) FROM translation_template_cache")
    suspend fun countTemplates(): Int
}

@Dao
interface GlossaryDao {
    @Query("SELECT * FROM dynamic_glossary ORDER BY chineseTerm DESC")
    fun observeAllGlossary(): Flow<List<GlossaryEntryEntity>>

    @Query("SELECT * FROM dynamic_glossary")
    suspend fun getAllGlossaryNow(): List<GlossaryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlossary(entry: GlossaryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGlossary(entries: List<GlossaryEntryEntity>)

    @Query("DELETE FROM dynamic_glossary WHERE id = :id")
    suspend fun deleteGlossaryById(id: Int)
}

@Dao
interface CloudVaultDao {
    @Query("SELECT * FROM cloud_vault_projects ORDER BY syncedAtEpochMs DESC")
    fun observeAllVaultProjects(): Flow<List<CloudVaultProjectEntity>>

    @Query("SELECT * FROM cloud_vault_projects ORDER BY syncedAtEpochMs DESC")
    suspend fun getAllVaultProjectsNow(): List<CloudVaultProjectEntity>

    @Query("SELECT * FROM cloud_vault_projects WHERE REPLACE(syncCode6Digit, '-', '') = REPLACE(:code, '-', '') LIMIT 1")
    suspend fun findBySyncCode(code: String): CloudVaultProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVaultProject(project: CloudVaultProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVaultProjects(projects: List<CloudVaultProjectEntity>)

    @Query("DELETE FROM cloud_vault_projects WHERE projectId = :projectId")
    suspend fun deleteVaultProject(projectId: String)
}

@Database(
    entities = [
        TranslationCacheEntity::class,
        GlossaryEntryEntity::class,
        CloudVaultProjectEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TranslatorDatabase : RoomDatabase() {
    abstract fun translationCacheDao(): TranslationCacheDao
    abstract fun glossaryDao(): GlossaryDao
    abstract fun cloudVaultDao(): CloudVaultDao

    companion object {
        @Volatile
        private var INSTANCE: TranslatorDatabase? = null

        fun getInstance(context: Context): TranslatorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TranslatorDatabase::class.java,
                    "hanviet_flashhud_cache.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
