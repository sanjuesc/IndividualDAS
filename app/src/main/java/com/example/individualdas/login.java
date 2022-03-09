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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button botonLogin = (Button) findViewById(R.id.botonLogin);
        botonLogin.setEnabled(false); //Obtenemos el boton y lo deshabilitamos para que no se pueda hacer login
                                      // (mas adelante lo habilitaremos)

        EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
        EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);

        textUsuario.addTextChangedListener(watcher); //obtenemos el campo del usuario y de la contraseña y los añadimos
        textContraseña.addTextChangedListener(watcher); //un listener para que cuando ambos campos sean validos
                                                        //se habilite el boton de login


        db = Room.databaseBuilder(getApplicationContext(), //iniciamos la base de datos
                AppDatabase.class, "database-name").fallbackToDestructiveMigration().build();

        //El codigo de abajo (y el metodo al que se llama) se usan para limpiar la base de datos
        //ha sido util durante el desarrollo del trabajo
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
        String contra = textContraseña.getText().toString(); //obtenemos el usuario y la contraseña introducidas


        Context context = getApplicationContext();
        CharSequence text=null;
        int duration = Toast.LENGTH_SHORT;
        if(comprobarCredenciales(usuario, contra)){ //si el usuario y la contraseña corresponden a algun usuario de la
                                                    //base da datos
            Intent i = new Intent(login.this, menu_principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("nombre_usuario",usuario); //guardamos el nombre de usuario para poder usarlo en el resto de actividad
            finish(); //cerramos esta actividad
            startActivity(i); //y empezamos la nueva
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); //cambiamos la animacion que ocurre cuando se abre una nueva actividad
        }else{
            text = "Sorry :("; //Si no son correctos, mostraremos un mensaje indicandolo
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

    private final TextWatcher watcher = new TextWatcher() { //El listener que hemos puesto antes a ambos campos de texto
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) { //si algun campo de texto es cambiado comprobaremos si los valores son validos
            EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
            EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
            Button botonLogin = (Button)findViewById(R.id.botonLogin);
            if (textContraseña.getText().toString().length()==0||textUsuario.getText().toString().length()==0) {
                //en este caso solo hemos comprobado si su longitud es mayor de 0, pero podriamos poner que el usuario tenga un @
                // o una longitud minima si quisieramos
                botonLogin.setEnabled(false);
            } else {
                botonLogin.setEnabled(true); //si la longitud era mayor que 0, habilitamos el boton de iniciar sesion
            }
        }
    };






    public User limpiar() throws ExecutionException, InterruptedException { //el metodo que se encarga de limpiar
                                                                            //la base de datos, se ha mencionado arriba

        Callable<User> callable = new Callable<User>() {
            @Override
            public User call() throws Exception {
                db.clearAllTables();
                return null; //devolvemos null por que en este caso no estamos haciendo un select asi que nos da igual
                            //lo que se devuelva
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
                return cuantos ==1; //devolvemos el valor que se desee obtener
            }
        }; //definimos un callable que se encarga de llamar a la base de datos y devuelve un Future (una promesa de Javascript)

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);
        /*
        Ejecutamos en un nuevo thread por que de ejecutarlo en el thread principal de la aplicacion esta quedaria bloqueada
        hasta que el futuro se resolviera

        Y usamos futuros por que la base de datos puede tardar en devolver los datos
         */
        return future.get(); //recogemos el futuro y devolvemos el valor
    }


    public void abrirRegistrarse(View view){

        Intent i = new Intent(login.this, registrarse.class);
        startActivity(i); //abrimos la actividad de registrarse
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); //y cambiamos la animacion con la que se abre
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

