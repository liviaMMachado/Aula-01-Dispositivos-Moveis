package com.ifsc.app;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    String[] nomes = new String[]{"Helena", "Livia", "Gabi 2026", "Pedro", "Romulo 2006", "Gabriel"};
    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        lv = findViewById(R.id.listview);
        ArrayAdapter<String> adapter = new NomesAdapter(this, R.layout.item_lista,R.id.edNome,nomes);

        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(this, nomes[position],LENGTH_LONG).show();
        });
    }
}