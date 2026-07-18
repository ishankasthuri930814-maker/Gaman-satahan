package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.PlannedTrip
import com.example.data.model.TripLog
import com.example.data.model.VehicleSetting
import com.example.data.model.DriverSetting
import com.example.data.model.AssistantSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_logs ORDER BY dateTimeMillis DESC")
    fun getAllTripLogs(): Flow<List<TripLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripLog(tripLog: TripLog): Long

    @Delete
    suspend fun deleteTripLog(tripLog: TripLog)

    @Query("DELETE FROM trip_logs WHERE id = :id")
    suspend fun deleteTripLogById(id: Int)
}

@Dao
interface PlannedTripDao {
    @Query("SELECT * FROM planned_trips ORDER BY scheduledTimeMillis ASC")
    fun getAllPlannedTrips(): Flow<List<PlannedTrip>>

    @Query("SELECT * FROM planned_trips ORDER BY scheduledTimeMillis ASC")
    suspend fun getPlannedTripsList(): List<PlannedTrip>

    @Query("SELECT * FROM planned_trips WHERE id = :id")
    suspend fun getPlannedTripById(id: Int): PlannedTrip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedTrip(plannedTrip: PlannedTrip): Long

    @Update
    suspend fun updatePlannedTrip(plannedTrip: PlannedTrip)

    @Delete
    suspend fun deletePlannedTrip(plannedTrip: PlannedTrip)

    @Query("DELETE FROM planned_trips WHERE id = :id")
    suspend fun deletePlannedTripById(id: Int)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM vehicles ORDER BY plateNumber ASC")
    fun getAllVehicles(): Flow<List<VehicleSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleSetting): Long

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicleById(id: Int)

    @Query("DELETE FROM vehicles WHERE plateNumber = :plateNumber")
    suspend fun deleteVehicleByPlate(plateNumber: String)

    @Query("SELECT * FROM drivers ORDER BY name ASC")
    fun getAllDrivers(): Flow<List<DriverSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverSetting): Long

    @Query("DELETE FROM drivers WHERE id = :id")
    suspend fun deleteDriverById(id: Int)

    @Query("DELETE FROM drivers WHERE name = :name")
    suspend fun deleteDriverByName(name: String)

    @Query("SELECT * FROM assistants ORDER BY name ASC")
    fun getAllAssistants(): Flow<List<AssistantSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssistant(assistant: AssistantSetting): Long

    @Query("DELETE FROM assistants WHERE id = :id")
    suspend fun deleteAssistantById(id: Int)

    @Query("DELETE FROM assistants WHERE name = :name")
    suspend fun deleteAssistantByName(name: String)

    @Query("DELETE FROM vehicles")
    suspend fun deleteAllVehicles()

    @Query("DELETE FROM drivers")
    suspend fun deleteAllDrivers()

    @Query("DELETE FROM assistants")
    suspend fun deleteAllAssistants()
}

@Database(
    entities = [TripLog::class, PlannedTrip::class, VehicleSetting::class, DriverSetting::class, AssistantSetting::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun plannedTripDao(): PlannedTripDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vahana_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // No pre-population, start completely empty as requested
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

