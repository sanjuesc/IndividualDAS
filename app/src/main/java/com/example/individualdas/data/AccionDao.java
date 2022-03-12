package com.example.individualdas.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AccionDao {

    @Query("SELECT * FROM accion")
    List<Accion> getAll();

    @Query("SELECT nombre from accion where uId=:first")
    String getNombre(int first);

    @Insert
    void insertUno(Accion accion);

    @Insert
    void insertAll(Accion... acciones);

    @Delete
    void delete(Accion accion);

    @Query("DELETE from accion where uId=:uId")
    int deleteById(int uId);
}
