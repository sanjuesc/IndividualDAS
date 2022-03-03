package com.example.individualdas.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

    @PrimaryKey
    @NonNull
    public String usuario;

    @ColumnInfo(name = "contraseña")
    public String contraseña;

    public User(String usuario, String contraseña){
        this.usuario=usuario;
        this.contraseña=contraseña;
    }
}