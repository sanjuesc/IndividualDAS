package com.example.individualdas.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Preferencias{

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int uId;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "idioma")
    public String idioma;

    public Preferencias(String nombre){
        this.nombre=nombre;
        this.idioma="Español";
    }
}
