package com.example.individualdas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.individualdas.data.Accion;
import com.example.individualdas.data.AppDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class menu_principal extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    Boolean isFABOpen;
    FloatingActionButton fab ;
    FloatingActionButton fab1;
    FloatingActionButton fab2;
    MyRecyclerViewAdapter adapter;
    ArrayList<String> animalNames;
    private String m_Text = "";
    private static AppDatabase db;
    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nombreUsuario = extras.getString("nombre_usuario");
            //The key argument here must match that used in the other activity
        }
        try {
            String idioma = obtenerIdioma(nombreUsuario);
            if(idioma=="dsdfdsf"){
                Locale nuevaloc = new Locale("es");
                Locale.setDefault(nuevaloc);
                Configuration config = new Configuration();
                config.locale = nuevaloc;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            }else{
                Locale nuevaloc = new Locale("en");
                Locale.setDefault(nuevaloc);
                Configuration config = new Configuration();
                config.locale = nuevaloc;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        setContentView(R.layout.activity_menu_principal);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        closeFABMenu();
        fab.bringToFront();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").fallbackToDestructiveMigration().build();

        try {
            List<Accion> acciones = obtenerTodos();
            animalNames = new ArrayList<>();
            for(int i = 0; i<acciones.size();i++){
                animalNames.add(acciones.get(i).nombre);
            }
            // set up the RecyclerView
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new MyRecyclerViewAdapter(this, animalNames);
            adapter.setClickListener(this);
            recyclerView.setAdapter(adapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onItemClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage("¿Desear borrar la acción "+adapter.getItem(position)+"?");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                animalNames.remove(position);
                try {
                    borrar(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.notifyItemRemoved(position);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //realmente no hay que hacer nada
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showFABMenu(){
        isFABOpen=true;
        fab1.setClickable(true);
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2.setClickable(true);
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));

    }

    private void closeFABMenu(){
        isFABOpen=false;
        fab1.animate().translationY(0);
        fab1.setClickable(false);
        fab2.animate().translationY(0);
        fab2.setClickable(false);
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage("¿Quieres cerrar la app?");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //realmente no hay que hacer nada
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void nuevo(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.nueva_tarea));

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);


        builder.setPositiveButton("Insertar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                animalNames.add(m_Text);
                try {
                    insertar(m_Text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.notifyItemInserted(animalNames.size() - 1);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }



    public List<Accion> obtenerTodos() throws ExecutionException, InterruptedException {

        Callable<List<Accion> > callable = new Callable<List<Accion> >() {
            @Override
            public List<Accion>  call() throws Exception {
                List<Accion>  cuantos = db.accionDao().getAll();
                return cuantos;
            }
        };

        Future<List<Accion> > future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    public String insertar(String nombre) throws ExecutionException, InterruptedException {

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                db.accionDao().insertUno(new Accion(nombre));
                return null;
            }
        };

        Future<String> future = Executors.newSingleThreadExecutor().submit(callable);
        return null;
    }


    public String borrar(int uid) throws ExecutionException, InterruptedException {

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                db.accionDao().deleteById(uid);
                return null;
            }
        };

        Future<String> future = Executors.newSingleThreadExecutor().submit(callable);
        return null;
    }

    public void abrirPreferencias(View view){
        Intent i = new Intent(menu_principal.this, preferencias.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    public String obtenerIdioma(String nombre) throws ExecutionException, InterruptedException {

        Callable<String> callable = new Callable<String>() {
            @Override
            public String  call() throws Exception {
                String  idioma = db.preferenciasDao().getIdioma(nombre);
                return idioma;
            }
        };

        Future<String> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }
}