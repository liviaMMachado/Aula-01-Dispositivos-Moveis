package com.ifsc.app;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.jspecify.annotations.NonNull;
import org.w3c.dom.Text;

public class NomesAdapter extends ArrayAdapter<String> {

    public NomesAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);

    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.item_lista, null);

        String nome = getItem(position);
        TextView textNome = convertView.findViewById(R.id.textNome);
        EditText editNome = convertView.findViewById(R.id.edNome);
        ImageView imageView = convertView.findViewById(R.id.imageView);

        textNome.setText(Integer.toString(position));
        editNome.setText(nome);
        return convertView;
    }

}
