import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


// Definición de la base de datos con Room
@Database(entities = [UserReview::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userReviewDao(): UserReviewDao

    companion object {
        @Volatile
        // Instancia única
        private var INSTANCE: AppDatabase? = null

        // Obtener base de datos
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Nombre del archivo DB
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
