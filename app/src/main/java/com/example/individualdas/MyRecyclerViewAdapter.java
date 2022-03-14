package com.example.individualdas;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    //metodo constructor estandar
    MyRecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    //"infla" el layout cuando es necesario
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    //Une la informacion al textview correspondiente de su linea (position)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String texto = mData.get(position);
        String accion = texto.toLowerCase();
        holder.myTextView.setText(texto);
        ImageView myImageView = holder.itemView.findViewById(R.id.card_imagen);
        //dependiendo del texto ponemos una imagen u otr
        if(accion.contains("andar")){ //deberia cambiarlo por un switch
            myImageView.setBackgroundResource(R.drawable.ic_andar);
        }else if (accion.contains("correr")){
            myImageView.setBackgroundResource(R.drawable.ic_correr);
        }else if (accion.contains("cocinar")||accion.contains("comer")){
            myImageView.setBackgroundResource(R.drawable.ic_comer);
        }else if(accion.contains("estudiar")||accion.contains("clase")){
            myImageView.setBackgroundResource(R.drawable.ic_estudiar);
        }else{
            //si no coincide nada se pone el de por defecto
            myImageView.setBackgroundResource(R.drawable.ic_otro);
        }
    }

    // Numero total de lineas (elementos)
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // Guarda y recicla las views a medida que se hace scroll por la aplicacion
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        ImageView myImageView;
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.info_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    //Con esto logramos la informacion de la linea id
    String getItem(int id) {
        return mData.get(id);
    }

    // añadimos el listener
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //se implementara este metodo para poder detectar y responder a los clics
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}