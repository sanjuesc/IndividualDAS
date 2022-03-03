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
        if(comrprobarNombre(usuario)){
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            CharSequence text = "El nombre de usuario ya esta en uso";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }else{
            EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
            String contra = textContraseña.getText().toString();
            User nuevoUser = new User(usuario, contra);
            insertUser(nuevoUser);
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            CharSequence text = "Usuario creado correctamente";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish();
        }
    }


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


    public Boolean comrprobarNombre(String user) throws ExecutionException, InterruptedException {

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

}