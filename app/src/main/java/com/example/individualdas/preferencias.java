package com.example.individualdas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;

import com.example.individualdas.data.Accion;
import com.example.individualdas.data.AppDatabase;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class preferencias extends AppCompatActivity {


    private String nombreUsuario;
    private Bundle extras;
    private static AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").fallbackToDestructiveMigration().build();
        extras = getIntent().getExtras();
        if (extras != null) {
            nombreUsuario = extras.getString("nombre_usuario");
        }
        try {
            String modo = obtenerModo(nombreUsuario);
            if(modo.equals("Dia")){
                setTheme(R.style.IndividualDAS_appbar_noche); //aplicamos el tema antes de que cargue el resto de la actividad
            }else{
                setTheme(R.style.IndividualDAS_appbar_dia);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);


        SwitchCompat sw = findViewById(R.id.switch_compat);
        try {
            String idioma = obtenerIdioma(nombreUsuario);
            sw.setChecked(idioma.equals("Espa??ol"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        sw.setOnCheckedChangeListener((compoundButton, b) ->{
                    if(b){ //si hemos elegido espa??ol
                        Locale nuevaloc = new Locale("es"); //esto lo he sacado de los apuntes, no creo que haga falta explicarlo
                        Locale.setDefault(nuevaloc);
                        Configuration config = new Configuration();
                        config.locale = nuevaloc;
                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                        cambiarPref("Espa??ol", nombreUsuario); //guardamos el idioma
                    }else{ //si hemos elegido ingles
                        Locale nuevaloc = new Locale("en");
                        Locale.setDefault(nuevaloc);
                        Configuration config = new Configuration();
                        config.locale = nuevaloc;
                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                        cambiarPref("Ingles", nombreUsuario);

                    }
                    destroyInstance();

                    finish();
                    startActivity(getIntent());
            }

        );



        SwitchCompat sw2 = findViewById(R.id.switch_tema);

        try {
            String modo = obtenerModo(nombreUsuario);
            sw2.setChecked(modo.equals("Dia"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        sw2.setOnCheckedChangeListener((compoundButton, b) ->{
                    if(b){
                        cambiarModo("Dia", nombreUsuario);
                    }else{
                        cambiarModo("Noche",nombreUsuario);
                    }
                    destroyInstance();
                    finish();
                    startActivity(getIntent());
                }

        );


    }




    public void salir(View view) throws ExecutionException, InterruptedException {
        /*
        //no funciona correctamente ya que room da muchos fallos a la hora de borrar elementos
        https://www.google.com/search?q=room+delete+not+working+site:stackoverflow.com
         */
        if(cantidadActividades()>0){ //si hay tareas, cerramos la app y mostramos notificacion
            int reqCode = 1;
            destroyInstance();
            Intent intent = new Intent(getApplicationContext(), login.class);
            showNotification(this, getString(R.string.app_name), getString(R.string.tareas_pendientes), intent, reqCode);
        }else{ //cerramos la app tal cual
            destroyInstance();
            Intent otroIntent = new Intent(Intent.ACTION_MAIN);
            otroIntent.addCategory(Intent.CATEGORY_HOME);
            startActivity(otroIntent);
        }
    }



    public void showNotification(Context context, String title, String message, Intent intent, int reqCode) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = "channel_name";// id del canal
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// el nombre del canal
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build());
        Intent cerrarApp = new Intent(Intent.ACTION_MAIN);
        cerrarApp.addCategory(Intent.CATEGORY_HOME);
        startActivity(cerrarApp);
    }

    public String cambiarPref(String pref, String usuario) { //los metodos de la base de datos son iguales que en las otras actividades

        Callable<String> callable = () -> {
            db.preferenciasDao().actualizarIdioma(pref, usuario);
            return null;
        };

        Executors.newSingleThreadExecutor().submit(callable);
        return null;
    }

    public String cambiarModo(String pref, String usuario) {

        Callable<String> callable = () -> {
            db.preferenciasDao().actualizarModo(pref, usuario);
            return null;
        };

        Executors.newSingleThreadExecutor().submit(callable);
        return null;
    }


    public String obtenerIdioma(String nombre) throws ExecutionException, InterruptedException {

        Callable<String> callable = () -> db.preferenciasDao().getIdioma(nombre);

        Future<String> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    private String obtenerModo(String nombre) throws ExecutionException, InterruptedException {
        Callable<String> callable = () -> db.preferenciasDao().getModo(nombre);

        Future<String> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public void idioma(){ //lo mismo que arriba pero en un metodo para no repetir el codigo al reescribir onResume, onConfigurationChanged...
        try {
            String idioma = obtenerIdioma(nombreUsuario);
            if(idioma.equals("Espa??ol")){
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
    }


    public Integer cantidadActividades() throws ExecutionException, InterruptedException {

        Callable<Integer> callable = () -> db.accionDao().getAll(nombreUsuario).size();

        Future<Integer> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    /*
    Estos metodos son lo mismo que lo descrito al final de menu_principal y sirven para lo mismo
     */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        idioma();

    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        idioma();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        idioma();
    }

    @Override
    protected void onResume() {
        super.onResume();
        idioma();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        db.getOpenHelper().close();
    }

    private void destroyInstance() {

        if (db.isOpen()) {
            db.close();
        }
        db = null;
    }




}