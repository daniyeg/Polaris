import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.beyond5g.polaris.Test
import com.beyond5g.polaris.TestDao

@Database(entities = [Test::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
//    abstract fun testDao(): TestDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "internal_app_db"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
}
