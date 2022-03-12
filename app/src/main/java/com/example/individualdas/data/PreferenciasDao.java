package com.example.individualdas.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PreferenciasDao {
    @Query("SELECT * FROM preferencias")
    List<Preferencias> getAll();

    @Query("SELECT idioma FROM preferencias where nombre LIKE :miUser")
    String getIdioma(String miUser);

    @Insert
    void insertUno(Preferencias pref);

    @Insert
    void insertAll(Preferencias... prefs);

    @Delete
    void delete(Preferencias pref);

    @Query("UPDATE preferencias SET idioma = :idioma WHERE nombre = :usuario")
    int actualizarIdioma(String idioma, String usuario);
}