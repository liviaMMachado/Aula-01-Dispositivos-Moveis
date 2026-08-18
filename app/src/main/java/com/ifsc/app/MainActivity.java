package com.ifsc.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button b=findViewById(R.id.button);
        TextView tv = findViewById(R.id.tv);

        EditText edmin,edmax;
        edmin = findViewById(R.id.edMin);
        edmax = findViewById(R.id.edMax);
        tv.setText("Número Aleatório");

        b.setOnClickListener(view -> {
            String smin = edmin.getText().toString();
            String smax = edmax.getText().toString();

            if(smin.isEmpty()){
                edmin.setError("Informe um número");
                return;
            }
            if(smax.isEmpty()){
                edmax.setError("Informe um número");
                return;
            }

            int min = Integer.parseInt(edmin.getText().toString());
            int max = Integer.parseInt(edmax.getText().toString());

            if(min>max){
                Toast.makeText(this, "Informe um número Máximo maior que o Minimo", Toast.LENGTH_SHORT).show();
                return;
            }

            Random random = new Random();
            int randomN = random.nextInt(min, max);
            tv.setText(Integer.toString(randomN));
        });
    }
}