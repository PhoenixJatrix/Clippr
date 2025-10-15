import androidx.room.Database
import androidx.room.RoomDatabase
import com.nullinnix.clippr.database.clips.ClipsDao
import com.nullinnix.clippr.misc.ClipEntity

@Database(
    entities = [ClipEntity::class],
    version = 3,
    exportSchema = true
)
abstract class ClipsDatabase: RoomDatabase() {
    abstract fun clipsDao(): ClipsDao
}