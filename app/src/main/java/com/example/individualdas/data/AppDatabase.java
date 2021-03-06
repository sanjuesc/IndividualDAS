package com.example.individualdas.data;


import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Accion.class, Preferencias.class}, version = 10)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract AccionDao accionDao();
    public abstract PreferenciasDao preferenciasDao();
}