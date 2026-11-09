package com.ifsc.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

    private static final int[] PRESET_COLORS = {
            Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.DKGRAY
    };
    private static final String[] PRESET_NAMES = {
            "Preto", "Vermelho", "Azul", "Verde",
            "Amarelo", "Roxo", "Ciano", "Cinza escuro"
    };

    private PaintCanvasView canvasView;
    private MaterialButtonToggleGroup toolsGroup;
    private MaterialButton currentColorButton;
    private Spinner layerSpinner;
    private final List<String> layerNames = new ArrayList<>();
    private ArrayAdapter<String> layerAdapter;
    private boolean updatingLayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        View statusBarBackground = findViewById(R.id.statusBarBackground);
        ((ViewGroup) root).setClipToPadding(false);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            android.view.ViewGroup.LayoutParams statusParams = statusBarBackground.getLayoutParams();
            if (statusParams.height != systemBars.top) {
                statusParams.height = systemBars.top;
                statusBarBackground.setLayoutParams(statusParams);
            }
            statusBarBackground.setTranslationY(-systemBars.top);
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
            } else if (checkedId == R.id.toolLine) {
                canvasView.setTool(PaintCanvasView.Tool.LINE);
            }
        });

        currentColorButton = findViewById(R.id.currentColorButton);
        updateColorPreview();
        currentColorButton.setOnClickListener(view -> showColorPicker());

        setupLayers();
        findViewById(R.id.clearButton).setOnClickListener(view -> canvasView.clearActiveLayer());
        findViewById(R.id.addLayerButton).setOnClickListener(view -> addLayer());
    }

    private void updateColorPreview() {
        int color = canvasView.getStrokeColor();
        currentColorButton.setBackgroundTintList(ColorStateList.valueOf(color));
        currentColorButton.setStrokeColor(ColorStateList.valueOf(
                Color.luminance(color) < 0.35f ? Color.WHITE : Color.DKGRAY));
        currentColorButton.setContentDescription("Cor atual: " + colorName(color)
                + ". Toque para escolher outra cor.");
    }

    private String colorName(int color) {
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            if (PRESET_COLORS[i] == color) return PRESET_NAMES[i];
        }
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private void showColorPicker() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int horizontalPadding = dp(20);
        content.setPadding(horizontalPadding, dp(8), horizontalPadding, 0);

        TextView presetLabel = new TextView(this);
        presetLabel.setText("Cores rápidas");
        content.addView(presetLabel);

        LinearLayout presets = new LinearLayout(this);
        presets.setGravity(Gravity.CENTER_VERTICAL);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams presetParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        presetParams.bottomMargin = dp(8);
        content.addView(presets, presetParams);

        int current = canvasView.getStrokeColor();
        SeekBar[] channels = new SeekBar[] {
                createColorSlider(content, "Vermelho", Color.red(current)),
                createColorSlider(content, "Verde", Color.green(current)),
                createColorSlider(content, "Azul", Color.blue(current))
        };

        View preview = new View(this);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(40));
        previewParams.topMargin = dp(8);
        content.addView(preview, previewParams);

        Runnable updatePreview = () -> preview.setBackgroundColor(Color.rgb(
                channels[0].getProgress(), channels[1].getProgress(), channels[2].getProgress()));
        for (SeekBar channel : channels) {
            channel.setOnSeekBarChangeListener(colorChangeListener(updatePreview));
        }
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            final int color = PRESET_COLORS[i];
            MaterialButton swatch = new MaterialButton(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(32), dp(32));
            params.setMargins(dp(2), 0, dp(2), 0);
            swatch.setLayoutParams(params);
            swatch.setText("");
            swatch.setContentDescription(PRESET_NAMES[i]);
            swatch.setMinWidth(0);
            swatch.setMinimumWidth(0);
            swatch.setInsetTop(0);
            swatch.setInsetBottom(0);
            swatch.setCornerRadius(dp(16));
            swatch.setBackgroundTintList(ColorStateList.valueOf(color));
            swatch.setOnClickListener(view -> {
                channels[0].setProgress(Color.red(color));
                channels[1].setProgress(Color.green(color));
                channels[2].setProgress(Color.blue(color));
                updatePreview.run();
            });
            presets.addView(swatch);
        }
        updatePreview.run();

        new AlertDialog.Builder(this)
                .setTitle("Cor do traço")
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aplicar", (dialog, which) -> {
                    canvasView.setStrokeColor(Color.rgb(
                            channels[0].getProgress(), channels[1].getProgress(), channels[2].getProgress()));
                    updateColorPreview();
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
                LinearLayout.LayoutParams.MATCH_PARENT, dp(36)));
        return slider;
    }

    private SeekBar.OnSeekBarChangeListener colorChangeListener(Runnable onChanged) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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
        layerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!updatingLayers) canvasView.setActiveLayer(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void addLayer() {
        canvasView.addLayer();
        layerNames.add("Camada " + (layerNames.size() + 1));
        layerAdapter.notifyDataSetChanged();
        updatingLayers = true;
        layerSpinner.setSelection(layerNames.size() - 1);
        updatingLayers = false;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
