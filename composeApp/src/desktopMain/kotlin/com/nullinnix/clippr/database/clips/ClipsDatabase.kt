import androidx.room.Database
import androidx.room.RoomDatabase
import com.nullinnix.clippr.database.clips.ClipsDao
import com.nullinnix.clippr.misc.Clip

@Database(
    entities = [Clip::class],
    version = 2,
    exportSchema = true
)
abstract class ClipsDatabase: RoomDatabase() {
    abstract fun clipsDao(): ClipsDao
}