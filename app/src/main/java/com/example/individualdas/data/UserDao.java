package com.example.individualdas.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> getAll();



    @Query("SELECT count(*) FROM user WHERE usuario LIKE :first" +
            " LIMIT 1")
    int findByName(String first);

    @Query("SELECT count(*) FROM user WHERE usuario LIKE :miUser" +
            " and contraseña LIKE :miPass LIMIT 1")
    int comprobarCredenciales(String miUser, String miPass);

    @Insert
    void insertUno(User user);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}