package com.ifsc.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorWheelView extends View {

    public interface OnColorChangedListener {
        void onColorChanged(int color);
    }

    private final float[] hsv = {0f, 1f, 1f};
    private final Paint wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap wheelBitmap;
    private int opacity = 255;
    private float centerX;
    private float centerY;
    private float radius;
    private float density;
    private OnColorChangedListener listener;

    public ColorWheelView(Context context) {
        super(context);
        initialize(context);
    }

    public ColorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ColorWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(2f * density);
        setContentDescription("Roda para escolher matiz e saturação da cor");
        setFocusable(true);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    public void setColor(int color) {
        opacity = Color.alpha(color);
        Color.colorToHSV(color, hsv);
        rebuildWheel();
        invalidate();
    }

    public void setBrightness(float brightness) {
        hsv[2] = Math.max(0f, Math.min(1f, brightness));
        notifyColorChanged();
        invalidate();
    }

    public void setOpacity(int opacity) {
        this.opacity = Math.max(0, Math.min(255, opacity));
        notifyColorChanged();
        invalidate();
    }

    public int getColor() {
        return Color.HSVToColor(opacity, hsv);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        centerX = width / 2f;
        centerY = height / 2f;
        radius = Math.max(0f, Math.min(width, height) / 2f - 10f * density);
        rebuildWheel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (wheelBitmap == null || radius <= 0f) return;

        float left = centerX - radius;
        float top = centerY - radius;
        float value = hsv[2];
        wheelPaint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[] {
                value, 0, 0, 0, 0,
                0, value, 0, 0, 0,
                0, 0, value, 0, 0,
                0, 0, 0, 1, 0
        })));
        canvas.drawBitmap(wheelBitmap, null,
                new RectF(left, top, left + radius * 2f, top + radius * 2f), wheelPaint);
        wheelPaint.setColorFilter(null);

        float angle = (float) Math.toRadians(hsv[0]);
        float markerX = centerX + (float) Math.cos(angle) * hsv[1] * radius;
        float markerY = centerY + (float) Math.sin(angle) * hsv[1] * radius;
        markerPaint.setColor(Color.WHITE);
        canvas.drawCircle(markerX, markerY, 10f * density, markerPaint);
        markerPaint.setColor(Color.DKGRAY);
        canvas.drawCircle(markerX, markerY, 8f * density, markerPaint);
    }

    private void rebuildWheel() {
        if (radius <= 0f) return;
        int bitmapSize = Math.max(1, Math.round(radius * 2f));
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[bitmapSize * bitmapSize];
        float bitmapCenter = bitmapSize / 2f;
        float bitmapRadius = bitmapSize / 2f;
        for (int y = 0; y < bitmapSize; y++) {
            float dy = (y + 0.5f - bitmapCenter) / bitmapRadius;
            for (int x = 0; x < bitmapSize; x++) {
                float dx = (x + 0.5f - bitmapCenter) / bitmapRadius;
                float saturation = (float) Math.sqrt(dx * dx + dy * dy);
                int index = y * bitmapSize + x;
                if (saturation > 1f) {
                    pixels[index] = Color.TRANSPARENT;
                } else {
                    float hue = (float) Math.toDegrees(Math.atan2(dy, dx));
                    if (hue < 0f) hue += 360f;
                    pixels[index] = Color.HSVToColor(new float[]{hue, saturation, 1f});
                }
            }
        }
        bitmap.setPixels(pixels, 0, bitmapSize, 0, 0, bitmapSize, bitmapSize);
        if (wheelBitmap != null && !wheelBitmap.isRecycled()) wheelBitmap.recycle();
        wheelBitmap = bitmap;
    }

    private void updateSelection(float x, float y) {
        if (radius <= 0f) return;
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.hypot(dx, dy);
        hsv[1] = Math.min(1f, distance / radius);
        float hue = (float) Math.toDegrees(Math.atan2(dy, dx));
        hsv[0] = hue < 0f ? hue + 360f : hue;
        notifyColorChanged();
        invalidate();
    }

    private void notifyColorChanged() {
        if (listener != null) listener.onColorChanged(getColor());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                updateSelection(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_UP:
                updateSelection(event.getX(), event.getY());
                performClick();
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
