package com.ifsc.app;

import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int[] COLORS = {
            Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.DKGRAY
    };
    private static final String[] COLOR_NAMES = {
            "Preto", "Vermelho", "Azul", "Verde",
            "Amarelo", "Roxo", "Ciano", "Cinza escuro"
    };

    private PaintCanvasView canvasView;
    private MaterialButtonToggleGroup toolsGroup;
    private Spinner layerSpinner;
    private final List<String> layerNames = new ArrayList<>();
    private ArrayAdapter<String> layerAdapter;
    private boolean updatingLayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int contentPadding = dp(16);
            view.setPadding(systemBars.left + contentPadding, systemBars.top + contentPadding,
                    systemBars.right + contentPadding, systemBars.bottom + contentPadding);
            return insets;
        });

        canvasView = findViewById(R.id.paintCanvas);
        toolsGroup = findViewById(R.id.toolsGroup);
        toolsGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.toolFreehand) {
                canvasView.setTool(PaintCanvasView.Tool.FREEHAND);
            } else if (checkedId == R.id.toolRectangle) {
                canvasView.setTool(PaintCanvasView.Tool.RECTANGLE);
            } else if (checkedId == R.id.toolCircle) {
                canvasView.setTool(PaintCanvasView.Tool.CIRCLE);
            }
        });

        createColorPalette();
        setupLayers();

        findViewById(R.id.customColorButton).setOnClickListener(view -> showCustomColorPicker());
        findViewById(R.id.clearButton).setOnClickListener(view -> canvasView.clearActiveLayer());
        findViewById(R.id.addLayerButton).setOnClickListener(view -> addLayer());
    }

    private void createColorPalette() {
        LinearLayout palette = findViewById(R.id.colorPalette);
        for (int i = 0; i < COLORS.length; i++) {
            final int color = COLORS[i];
            MaterialButton swatch = new MaterialButton(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(40), dp(40));
            params.setMargins(dp(4), dp(2), dp(4), dp(2));
            swatch.setLayoutParams(params);
            swatch.setText("");
            swatch.setContentDescription("Cor " + COLOR_NAMES[i]);
            swatch.setMinWidth(0);
            swatch.setMinimumWidth(0);
            swatch.setInsetTop(0);
            swatch.setInsetBottom(0);
            swatch.setCornerRadius(dp(20));
            swatch.setBackgroundTintList(ColorStateList.valueOf(color));
            swatch.setStrokeWidth(dp(i == 0 ? 2 : 0));
            swatch.setStrokeColor(ColorStateList.valueOf(i == 0 ? Color.WHITE : Color.TRANSPARENT));
            swatch.setOnClickListener(view -> {
                canvasView.setStrokeColor(color);
                updateSelectedSwatch(palette, swatch);
            });
            palette.addView(swatch);
            if (i == 0) swatch.setTag("selected");
        }
    }

    private void updateSelectedSwatch(LinearLayout palette, MaterialButton selected) {
        for (int i = 0; i < palette.getChildCount(); i++) {
            View child = palette.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton swatch = (MaterialButton) child;
                boolean isSelected = swatch == selected;
                swatch.setStrokeWidth(dp(isSelected ? 3 : 0));
                swatch.setStrokeColor(ColorStateList.valueOf(isSelected ? Color.WHITE : Color.TRANSPARENT));
            }
        }
    }

    private void showCustomColorPicker() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        content.setPadding(pad, dp(8), pad, 0);

        int current = canvasView.getStrokeColor();
        SeekBar red = createColorSlider(content, "Vermelho", Color.red(current));
        SeekBar green = createColorSlider(content, "Verde", Color.green(current));
        SeekBar blue = createColorSlider(content, "Azul", Color.blue(current));
        TextView preview = new TextView(this);
        preview.setText("Prévia da cor");
        preview.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        previewParams.topMargin = dp(8);
        content.addView(preview, previewParams);

        Runnable updatePreview = () -> {
            int color = Color.rgb(red.getProgress(), green.getProgress(), blue.getProgress());
            preview.setBackgroundTintList(ColorStateList.valueOf(color));
            preview.setBackgroundColor(color);
        };
        red.setOnSeekBarChangeListener(colorChangeListener(updatePreview));
        green.setOnSeekBarChangeListener(colorChangeListener(updatePreview));
        blue.setOnSeekBarChangeListener(colorChangeListener(updatePreview));
        updatePreview.run();

        new AlertDialog.Builder(this)
                .setTitle("Escolha uma cor")
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aplicar", (dialog, which) -> {
                    int color = Color.rgb(red.getProgress(), green.getProgress(), blue.getProgress());
                    canvasView.setStrokeColor(color);
                })
                .show();
    }

    private SeekBar createColorSlider(LinearLayout parent, String label, int initialValue) {
        TextView title = new TextView(this);
        title.setText(label);
        parent.addView(title);
        SeekBar slider = new SeekBar(this);
        slider.setMax(255);
        slider.setProgress(initialValue);
        parent.addView(slider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(40)));
        return slider;
    }

    private SeekBar.OnSeekBarChangeListener colorChangeListener(Runnable onChanged) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onChanged.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        };
    }

    private void setupLayers() {
        layerSpinner = findViewById(R.id.layerSpinner);
        layerNames.add("Camada 1");
        layerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, layerNames);
        layerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layerSpinner.setAdapter(layerAdapter);
        layerSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                                int position, long id) {
                if (!updatingLayers) canvasView.setActiveLayer(position);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void addLayer() {
        String name = "Camada " + (layerNames.size() + 1);
        canvasView.addLayer();
        layerNames.add(name);
        layerAdapter.notifyDataSetChanged();
        updatingLayers = true;
        layerSpinner.setSelection(layerNames.size() - 1);
        updatingLayers = false;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
