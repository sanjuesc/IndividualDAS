package com.example.individualdas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.individualdas.data.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class login extends AppCompatActivity {

    private static AppDatabase db;
    private ScheduledExecutorService scheduleTaskExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("sa creado", "asi es");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button botonLogin = (Button) findViewById(R.id.botonLogin);
        botonLogin.setEnabled(false);
        EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
        EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
        textUsuario.addTextChangedListener(watcher);
        textContraseña.addTextChangedListener(watcher);


        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").fallbackToDestructiveMigration().build();

        //arreglado el problema de memoria https://stackoverflow.com/questions/44244508/room-persistance-library-delete-all
        /*
        try {
            limpiar();
        } catch (Exception e) {
            e.printStackTrace();
        }

        */


    }



    public void comprobarCredenciales(View v) throws ExecutionException, InterruptedException {
        EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
        String usuario = textUsuario.getText().toString();

        EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
        String contra = textContraseña.getText().toString();
        Context context = getApplicationContext();
        CharSequence text=null;
        int duration = Toast.LENGTH_SHORT;
        if(comprobarCredenciales(usuario, contra)){
            Intent i = new Intent(login.this, menu_principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("nombre_usuario",usuario);
            finish();
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }else{
            text = "Sorry :(";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) {
            EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
            EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
            Button botonLogin = (Button)findViewById(R.id.botonLogin);
            if (textContraseña.getText().toString().length()==0||textUsuario.getText().toString().length()==0) {
                botonLogin.setEnabled(false);
            } else {
                botonLogin.setEnabled(true);
            }
        }
    };

    public User insertUser(User user) throws ExecutionException, InterruptedException {

        Callable<User> callable = new Callable<User>() {
            @Override
            public User call() throws Exception {
                db.userDao().insertUno(user);
                return null;
            }
        };

        Future<User> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }





    public User limpiar() throws ExecutionException, InterruptedException {

        Callable<User> callable = new Callable<User>() {
            @Override
            public User call() throws Exception {
                db.clearAllTables();
                return null;
            }
        };

        Future<User> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public Boolean comprobarCredenciales(String usuario, String contraseña) throws ExecutionException, InterruptedException {

        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int cuantos = db.userDao().comprobarCredenciales(usuario, contraseña);
                return cuantos ==1;
            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    public void abrirRegistrarse(View view){



        Intent i = new Intent(login.this, registrarse.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onStart(){
        super.onStart();
    }




}

