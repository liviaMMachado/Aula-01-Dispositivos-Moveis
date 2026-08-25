package com.ifsc.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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
        Toast.makeText(this, "OnCreate", Toast.LENGTH_LONG).show();
        Log.d("ciclodavida", "onCreate");

        Button b = findViewById(R.id.button);
        b.setOnClickListener(v -> {
            Intent i = new Intent(this, ActivityB.class);
            startActivity(i);
        });
        Button b2 = findViewById(R.id.button2);
        b2.setOnClickListener(v -> {
            EditText editText=findViewById(R.id.edText);
            String s = editText.getText().toString();

            Intent i = new Intent(this, MainActivity2.class);
            i.putExtra("msn", s);
            startActivity(i);
        });
    }
    @Override
        protected void onStart () {
            super.onStart();
            Toast.makeText(this, "onStart", Toast.LENGTH_LONG).show();
        Log.d("ciclodavida", "onStart");
    }
    @Override
    protected void onResume () {
        super.onResume();
        Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
        Log.d("ciclodavida", "onResume");
    }
    @Override
    protected void onPause () {
        super.onPause();
        Toast.makeText(this, "onPause", Toast.LENGTH_LONG).show();
        Log.d("ciclodavida", "onPause");
    }
    @Override
    protected void onStop () {
        super.onStop();
        Toast.makeText(this, "onStop", Toast.LENGTH_LONG).show();
        Log.d("ciclodavida", "onStop");
    }
    @Override
    protected void onDestroy () {
        super.onDestroy();
        Toast.makeText(this, "onDestroy", Toast.LENGTH_LONG).show();
        Log.d("ciclodavida", "onDestroy");
    }
}