package com.ifsc.app;

import android.content.res.ColorStateList;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PaintCanvasView canvasView;
    private MaterialButtonToggleGroup toolsGroup;
    private MaterialButton currentColorButton;
    private final List<String> layerNames = new ArrayList<>();
    private int activeLayer;

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
        SeekBar strokeWidthSlider = findViewById(R.id.strokeWidthSlider);
        canvasView.setStrokeWidth(strokeWidthSlider.getProgress() + 1f);
        strokeWidthSlider.setOnSeekBarChangeListener(colorChangeListener(
                () -> canvasView.setStrokeWidth(strokeWidthSlider.getProgress() + 1f)));
        findViewById(R.id.saveButton).setOnClickListener(view -> saveDrawing());
        findViewById(R.id.layersButton).setOnClickListener(view -> showLayersDialog());
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
        if (color == Color.BLACK) return "Preto";
        if (color == Color.RED) return "Vermelho";
        if (color == Color.BLUE) return "Azul";
        if (color == Color.GREEN) return "Verde";
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private void showColorPicker() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int horizontalPadding = dp(20);
        content.setPadding(horizontalPadding, dp(8), horizontalPadding, 0);

        int current = canvasView.getStrokeColor();
        ColorWheelView colorWheel = new ColorWheelView(this);
        colorWheel.setColor(current);
        LinearLayout.LayoutParams wheelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(240));
        content.addView(colorWheel, wheelParams);

        SeekBar opacity = createColorSlider(content, "Opacidade", Color.alpha(current), 255);
        float[] initialHsv = new float[3];
        Color.colorToHSV(current, initialHsv);
        SeekBar brightness = createColorSlider(content, "Brilho", Math.round(initialHsv[2] * 100), 100);

        View preview = new View(this);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(32));
        previewParams.topMargin = dp(8);
        content.addView(preview, previewParams);

        Runnable updatePreview = () -> preview.setBackgroundColor(colorWheel.getColor());
        colorWheel.setOnColorChangedListener(color -> updatePreview.run());
        opacity.setOnSeekBarChangeListener(colorChangeListener(
                () -> colorWheel.setOpacity(opacity.getProgress())));
        brightness.setOnSeekBarChangeListener(colorChangeListener(
                () -> colorWheel.setBrightness(brightness.getProgress() / 100f)));
        updatePreview.run();

        new AlertDialog.Builder(this)
                .setTitle("ColorPicker Dialog")
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Selecionar", (dialog, which) -> {
                    canvasView.setStrokeColor(colorWheel.getColor());
                    updateColorPreview();
                })
                .show();
    }

    private SeekBar createColorSlider(LinearLayout parent, String label, int initialValue, int maxValue) {
        TextView title = new TextView(this);
        title.setText(label);
        parent.addView(title);
        SeekBar slider = new SeekBar(this);
        slider.setMax(maxValue);
        slider.setProgress(initialValue);
        parent.addView(slider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(32)));
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
        layerNames.add("Camada 1");
    }

    private void addLayer() {
        canvasView.addLayer();
        layerNames.add("Camada " + (layerNames.size() + 1));
        activeLayer = layerNames.size() - 1;
        Toast.makeText(this, layerNames.get(activeLayer) + " adicionada", Toast.LENGTH_SHORT).show();
    }

    private void showLayersDialog() {
        String[] names = layerNames.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Camadas")
                .setSingleChoiceItems(names, activeLayer, (dialog, which) -> {
                    activeLayer = which;
                    canvasView.setActiveLayer(which);
                    dialog.dismiss();
                })
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void saveDrawing() {
        Bitmap bitmap = canvasView.exportBitmap();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,
                "PaintAula-" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/PaintAula");
        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (imageUri == null) {
            Toast.makeText(this, "Não foi possível salvar o desenho", Toast.LENGTH_LONG).show();
            bitmap.recycle();
            return;
        }

        try (OutputStream output = getContentResolver().openOutputStream(imageUri)) {
            if (output == null) throw new IllegalStateException("Stream de saída indisponível");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            Toast.makeText(this, "Desenho salvo em Imagens/PaintAula", Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            getContentResolver().delete(imageUri, null, null);
            Toast.makeText(this, "Erro ao salvar o desenho", Toast.LENGTH_LONG).show();
        } finally {
            bitmap.recycle();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
