package com.example.individualdas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.individualdas.data.AppDatabase;
import com.example.individualdas.data.Preferencias;
import com.example.individualdas.data.User;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class registrarse extends AppCompatActivity {


    private static AppDatabase db; //static para que solo haya una
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);
        /*
        Al igual que en la pantalla de login, el boton de registrarse solo se activara cuando ambos campos
        tengan valores validos
        Ver los comentarios que hay en la clase login para saber mas sobre como funciona
         */
        Button botonLogin = (Button) findViewById(R.id.crearCuenta);
        botonLogin.setEnabled(false);
        EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
        EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
        textUsuario.addTextChangedListener(watcher);
        textContraseña.addTextChangedListener(watcher);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").fallbackToDestructiveMigration().build();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { //ver metodo de mismo nombre en clase login
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private final TextWatcher watcher = new TextWatcher() {//ver metodo de mismo nombre en clase login
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
            Button botonRegistro = (Button)findViewById(R.id.crearCuenta);
            if (textContraseña.getText().toString().length()==0||textUsuario.getText().toString().length()==0) {
                botonRegistro.setEnabled(false);
            } else {
                botonRegistro.setEnabled(true);
            }
        }
    };

    public void registrarse(View view) throws ExecutionException, InterruptedException {
        EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
        String usuario = textUsuario.getText().toString();
        if(comrprobarNombre(usuario)){ //si el usuario está en uso, mostraremos un toast para indicarlo
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            CharSequence text = getString(R.string.usuario_en_uso);
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }else{ //en cambio, si no esta en uso
            EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
            String contra = textContraseña.getText().toString();
            User nuevoUser = new User(usuario, contra); //definimos un nuevo usuario
            insertUser(nuevoUser); //y lo añadimos a la base de datos

            Preferencias pref = new Preferencias(usuario);
            insertPref(pref);
            Context context = getApplicationContext(); //despues mostramos un mensaje toast para indicar que el usuario
                                                        //se ha creado
            int duration = Toast.LENGTH_SHORT;
            CharSequence text = getString(R.string.usuario_creado);
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish(); //y finalmente volvemos a la pantalla de login
            //podria cambiarlo para que no nos devuelva  a la pantalla de login
            //(a lo mejor el usuario quiere registrarse mas de una vez?), pero por como funciona la aplicacion
            //no sirve de nada crear mas usuario (ademas de para las preferencias de cada uno)
        }
    }


    public User insertUser(User user) throws ExecutionException, InterruptedException {
        /*
        Ver metodo comprobarCredenciales de la clase login
         */
        Callable<User> callable = new Callable<User>() {
            @Override
            public User call() throws Exception {
                db.userDao().insertUno(user);
                return null; //en este caso como hacemos un insert y no un select nos da igual lo que se devuelva
            }
        };

        Future<User> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get(); //aunque nos de igual por como fuciona el metodo es necesario devolverlo
    }


    public Boolean comrprobarNombre(String user) throws ExecutionException, InterruptedException {
        /*
        Ver metodo comprobarCredenciales de la clase login
         */

        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int cuantos = db.userDao().findByName(user);
                return cuantos ==1;
            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    public Preferencias insertPref(Preferencias pref) throws ExecutionException, InterruptedException {
        /*
        Ver metodo comprobarCredenciales de la clase login
         */
        Callable<Preferencias> callable = new Callable<Preferencias>() {
            @Override
            public Preferencias call() throws Exception {
                db.preferenciasDao().insertUno(pref);
                return null; //en este caso como hacemos un insert y no un select nos da igual lo que se devuelva
            }
        };

        Future<Preferencias> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get(); //aunque nos de igual por como fuciona el metodo es necesario devolverlo
    }
}
