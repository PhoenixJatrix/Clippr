import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.nullinnix.clippr.database.ClipsDao
import com.nullinnix.clippr.misc.Clip

@Database(
    entities = [Clip::class],
    version = 1,
    exportSchema = true
)
abstract class ClipsDatabase: RoomDatabase() {
    abstract fun clipsDao(): ClipsDao
}