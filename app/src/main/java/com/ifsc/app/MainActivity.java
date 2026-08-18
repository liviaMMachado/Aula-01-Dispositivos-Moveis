package com.ifsc.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView TextView;

    int contador=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button b=findViewById(R.id.button);
        TextView tv = findViewById(R.id.tv);

        tv.setText(Integer.toString(contador));
        b.setOnClickListener(view -> {
            contador++;
            tv.setText(Integer.toString(contador));
        });

        /*View textView = findViewById((R.id.main));
        textView.setText(contador);
        textView.setOnClickListener(V -> {
            textView.setText("BLA");
        });


        /*setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button = findViewById(R.id.button);
        TextView TextView = findViewById(R.id.textView);

        button.setOnClickListener(view -> {
            contador++;

            textView.setText(Integer.toString(contador));

        });*/
    }
}