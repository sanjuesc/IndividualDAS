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

        @ColumnInfo(name = "usuario")
        public String usuario;

        public Accion(String nombre, String usuario){
            this.nombre=nombre;
            this.usuario=usuario;


        }
}
