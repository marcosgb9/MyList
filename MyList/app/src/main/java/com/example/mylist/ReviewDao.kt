import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update


// Tabla reviews
@Entity(tableName = "reviews") // Tabla
data class UserReview(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val titulo: String,
    val genero: String,
    val comentario: String
)

@Dao
interface UserReviewDao {  // Métodos

    // Obtener toda la lista de reviews
    @Query("SELECT * FROM reviews")
    suspend fun getAll(): List<UserReview>

    // Filtrar por género
    @Query("SELECT * FROM reviews WHERE genero = :genero")
    suspend fun getByGenero(genero: String): List<UserReview>

    // Insertar
    @Insert
    suspend fun insert(review: UserReview)

    // Actualizar
    @Update
    suspend fun update(review: UserReview)

    // Eliminar
    @Delete
    suspend fun delete(review: UserReview)
}
