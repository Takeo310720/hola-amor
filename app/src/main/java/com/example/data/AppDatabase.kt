package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RecadoItem::class, ChatMessage::class, CouplePhoto::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recadoDao(): RecadoDao
    abstract fun chatDao(): ChatDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tuyyo_couple_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial warm sample messages & recados
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            seedInitialData(database)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val fiveMinAgo = now - 5 * 60 * 1000
            val threeMinAgo = now - 3 * 60 * 1000
            val oneMinAgo = now - 1 * 60 * 1000

            // Initial WhatsApp-style chat messages between César & Ella
            db.chatDao().insertMessage(
                ChatMessage(
                    sender = "Ella",
                    text = "¡Hola mi amor! ❤️ ¿Te acordaste de comprar el pancito dulce que nos gusta?",
                    timestamp = fiveMinAgo,
                    reaction = "🥰"
                )
            )
            db.chatDao().insertMessage(
                ChatMessage(
                    sender = "César",
                    text = "¡Sii! Ya pasé por la panadería, y además te traje una sorpresa para la cena 🥐✨",
                    timestamp = threeMinAgo,
                    reaction = "❤️"
                )
            )
            db.chatDao().insertMessage(
                ChatMessage(
                    sender = "Ella",
                    text = "Awww eres el mejor novio del mundo 🥰 Te dejé un recado en la sección de pendientes para el fin de semana.",
                    timestamp = oneMinAgo
                )
            )
            db.chatDao().insertMessage(
                ChatMessage(
                    sender = "César",
                    text = "¡Mira nuestra foto favorita en la cafetería! ☕💑",
                    imageUri = "android.resource://com.example/drawable/couple_polaroid",
                    timestamp = now,
                    reaction = "🔥"
                )
            )

            // Initial Recados
            db.recadoDao().insertRecado(
                RecadoItem(
                    title = "Noche de pelis y palomitas 🍿",
                    details = "Elegir entre comedia romántica o serie pendiente juntos",
                    category = "Cita",
                    author = "Ella",
                    target = "Para los dos",
                    dueDate = "Viernes 8:30 PM",
                    isPinned = true
                )
            )
            db.recadoDao().insertRecado(
                RecadoItem(
                    title = "Comprar helado de pistacho y fresa 🍦",
                    details = "En la tiendita de la esquina",
                    category = "Compras",
                    author = "César",
                    target = "Para ti",
                    dueDate = "Hoy",
                    isCompleted = false
                )
            )
            db.recadoDao().insertRecado(
                RecadoItem(
                    title = "Recordatorio: ¡Te amo con todo mi corazón! 💌",
                    details = "Nunca olvides lo especial que eres para mí todos los días.",
                    category = "Amor",
                    author = "Ella",
                    target = "Para ti",
                    isPinned = true
                )
            )

            // Initial couple photo
            db.photoDao().insertPhoto(
                CouplePhoto(
                    imageUri = "android.resource://com.example/drawable/couple_polaroid",
                    caption = "Nuestra cita de café favorita ☕❤️",
                    tag = "Cita",
                    addedBy = "César",
                    isFavorite = true
                )
            )
        }
    }
}
