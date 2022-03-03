package com.example.individualdas.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Accion{

        @PrimaryKey(autoGenerate = true)
        @NonNull
        public int uId;

        @ColumnInfo(name = "nombre")
        public String nombre;

        public Accion(String nombre){
            this.nombre=nombre;

        }
}
