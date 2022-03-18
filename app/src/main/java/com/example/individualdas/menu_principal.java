package com.example.individualdas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
    private Bundle extras;

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
                setTheme(R.style.IndividualDAS_appbar_noche);
            }else{
                setTheme(R.style.IndividualDAS_appbar_dia);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        idioma(); //cargamos el idioma de la preferencias del usuario


        setContentView(R.layout.activity_menu_principal);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        closeFABMenu();
        fab.bringToFront();
        fab.setOnClickListener(view -> {
            if(!isFABOpen){
                showFABMenu();
            }else{
                closeFABMenu();
            }
        });


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
        builder.setMessage(getString(R.string.borrar_accion) +adapter.getItem(position)+"?");

        builder.setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> {
            String valor = adapter.getItem(position);
            Log.d(String.valueOf(position), "aaaa");
            animalNames.remove(position);
            try {
                Accion acc = new Accion(valor, nombreUsuario);
                borrarConDelete(acc);
                borrar(position);

                /*
                llamo a ambos metodos de borrar ya que al parecer Room da muchos fallos a la hora de borrar sin motivo aparente
                https://www.google.com/search?q=room+delete+not+working+site:stackoverflow.com

                Seguramente no se borren de la base de datos pero si de la lista, asi que al reiniciar la aplicación deberian seguir existiendo
                 */
            } catch (Exception e) {
                e.printStackTrace();
            }
            adapter.notifyItemRemoved(position);
        });
        builder.setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> {
            //realmente no hay que hacer nada
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
        builder.setMessage(getString(R.string.pregunta_cerrar_app));

        //si quiere cerrar la app pues le cerramos la app
        builder.setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> {
            try {
                if(cantidadActividades()>0) { //si hay tareas, cerramos la app y mostramos notificacion
                    int reqCode = 1;
                    Intent intent = new Intent(getApplicationContext(), login.class);
                    showNotification(this, getString(R.string.app_name), getString(R.string.tareas_pendientes), intent, reqCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        });
        builder.setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> {
            //realmente no hay que hacer nada
        });
        AlertDialog dialog = builder.create();
        dialog.show(); //creamos y mostramos el dialogo

    }

    public void nuevo(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.nueva_tarea));

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);


        builder.setPositiveButton(getString(R.string.insertar), (dialog, which) -> {
            m_Text = input.getText().toString();
            animalNames.add(m_Text);
            try {
                insertar(m_Text);
            } catch (Exception e) {
                e.printStackTrace();
            }
            adapter.notifyItemInserted(animalNames.size() - 1);
        });
        builder.setNegativeButton(getString(R.string.cancelar), (dialog, which) -> dialog.cancel());
        builder.show();

    }



    public List<Accion> obtenerTodos() throws ExecutionException, InterruptedException {

        Callable<List<Accion> > callable = () -> {
            List<Accion>  cuantos = db.accionDao().getAll(nombreUsuario);
            return cuantos;
        };

        Future<List<Accion> > future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    public String insertar(String nombre) {

        Callable<String> callable = () -> {
            db.accionDao().insertUno(new Accion(nombre, nombreUsuario));
            return null;
        };

        Executors.newSingleThreadExecutor().submit(callable);
        return null;
    }


    public String borrar(int uid){

        Callable<String> callable = () -> {
            db.accionDao().deleteById(uid);
            return null;
        };

        Executors.newSingleThreadExecutor().submit(callable);
        return null;
    }

    public String borrarConDelete(Accion pref){

        Callable<String> callable = () -> {
            db.accionDao().delete(pref);
            return null;
        };

        Executors.newSingleThreadExecutor().submit(callable);
        return null;
    }

    public void abrirPreferencias(View view){
        Intent i = new Intent(menu_principal.this, preferencias.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("nombre_usuario",nombreUsuario); //guardamos el nombre de usuario para poder usarlo en el resto de actividad
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    public String obtenerIdioma(String nombre) throws ExecutionException, InterruptedException {

        Callable<String> callable = () -> db.preferenciasDao().getIdioma(nombre);

        Future<String> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public Integer cantidadActividades() throws ExecutionException, InterruptedException {

        Callable<Integer> callable = () -> db.accionDao().getAll(nombreUsuario).size();

        Future<Integer> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    private String obtenerModo(String nombre) throws ExecutionException, InterruptedException {
        Callable<String> callable = () -> db.preferenciasDao().getModo(nombre);

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

    /*
        He encontrado varios posts en los cuales se describe que a veces tras rotar la pantalla
        el idioma de la aplicacion se volvia loco, y tras probar a rotar la pantalla varias veces
        me ha saltado tambien, asi que he implementado los metodos de aqui abajo para evitarlo
        en la medida de lo posible
        https://stackoverflow.com/questions/33541923/android-language-changes-after-rotation
        https://stackoverflow.com/questions/19765527/after-the-screen-rotation-the-language-of-my-application-will-be-changed
        https://stackoverflow.com/questions/70233726/language-of-android-app-changes-after-screen-rotation
        https://stackoverflow.com/questions/42502003/after-rotation-activity-re-set-default-locale
        ...
     */


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        idioma();
        modo();
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
        //modo()
        //no descomentar la linea de arriba, por que se va a ejecutar sin parar
    }

    private void modo() {
        try {
            String modo = obtenerModo(nombreUsuario);
            if(modo.equals("Dia")){
                setTheme(R.style.IndividualDAS_appbar_noche);
            }else{
                setTheme(R.style.IndividualDAS_appbar_dia);
            }
            finish();
            startActivity(getIntent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        db.close();
    }

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode) { //el metodo que uso para crear las notificaciones
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //para evitar errores con algunas versiones
            CharSequence name = "Channel Name";// el nombre del canal
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build());

        Intent otroIntent = new Intent(Intent.ACTION_MAIN);
        otroIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(otroIntent);
    }

}