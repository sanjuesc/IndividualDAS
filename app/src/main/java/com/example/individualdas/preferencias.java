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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);


        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").fallbackToDestructiveMigration().build();
        extras = getIntent().getExtras();
        if (extras != null) {
            nombreUsuario = extras.getString("nombre_usuario");
        }

        SwitchCompat sw = findViewById(R.id.switch_compat);
        try {
            String idioma = obtenerIdioma(nombreUsuario);
            sw.setChecked(idioma.equals("Español"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        sw.setOnCheckedChangeListener((compoundButton, b) ->{
                    if(b){
                        Locale nuevaloc = new Locale("es");
                        Locale.setDefault(nuevaloc);
                        Configuration config = new Configuration();
                        config.locale = nuevaloc;
                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                        cambiarPref("Español", nombreUsuario);
                    }else{
                        Locale nuevaloc = new Locale("en");
                        Locale.setDefault(nuevaloc);
                        Configuration config = new Configuration();
                        config.locale = nuevaloc;
                        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                        cambiarPref("Ingles", nombreUsuario);

                    }
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
            Intent intent = new Intent(getApplicationContext(), login.class);
            showNotification(this, getString(R.string.app_name), getString(R.string.tareas_pendientes), intent, reqCode);
        }else{ //cerramos la app tal cual
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

    public String cambiarPref(String pref, String usuario) {

        Callable<String> callable = () -> {
            db.preferenciasDao().actualizarIdioma(pref, usuario);
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

    public void idioma(){
        try {
            String idioma = obtenerIdioma(nombreUsuario);
            if(idioma.equals("Español")){
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

        Callable<Integer> callable = () -> db.accionDao().getAll().size();

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
        db.close();
    }

}