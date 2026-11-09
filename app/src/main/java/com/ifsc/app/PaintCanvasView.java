package com.ifsc.app;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PaintCanvasView extends View {

    public enum Tool { FREEHAND, RECTANGLE, CIRCLE, LINE }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<List<Stroke>> layers = new ArrayList<>();
    private int activeLayer;
    private int strokeColor = Color.RED;
    private float strokeWidth = 8f;
    private Tool tool = Tool.FREEHAND;
    private Stroke inProgress;

    public PaintCanvasView(Context context) {
        super(context);
        initialize();
    }

    public PaintCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PaintCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        layers.add(new ArrayList<>());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public void setStrokeColor(int color) {
        strokeColor = color;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeWidth(float width) {
        strokeWidth = Math.max(1f, Math.min(40f, width));
    }

    public Bitmap exportBitmap() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            throw new IllegalStateException("A área de desenho ainda não está pronta");
        }
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        drawContents(new Canvas(bitmap));
        return bitmap;
    }

    public void addLayer() {
        layers.add(new ArrayList<>());
        activeLayer = layers.size() - 1;
        invalidate();
    }

    public void setActiveLayer(int index) {
        if (index >= 0 && index < layers.size()) {
            activeLayer = index;
            invalidate();
        }
    }

    public void clearActiveLayer() {
        layers.get(activeLayer).clear();
        inProgress = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContents(canvas);
    }

    private void drawContents(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (List<Stroke> layer : layers) {
            for (Stroke stroke : layer) drawStroke(canvas, stroke);
        }
        if (inProgress != null) drawStroke(canvas, inProgress);
    }

    private void drawStroke(Canvas canvas, Stroke stroke) {
        paint.setColor(stroke.color);
        paint.setStrokeWidth(stroke.strokeWidth);
        switch (stroke.tool) {
            case FREEHAND:
                canvas.drawPath(stroke.path, paint);
                break;
            case RECTANGLE:
                canvas.drawRect(new RectF(
                        Math.min(stroke.startX, stroke.endX),
                        Math.min(stroke.startY, stroke.endY),
                        Math.max(stroke.startX, stroke.endX),
                        Math.max(stroke.startY, stroke.endY)), paint);
                break;
            case CIRCLE:
                float centerX = (stroke.startX + stroke.endX) / 2f;
                float centerY = (stroke.startY + stroke.endY) / 2f;
                float radius = (float) Math.hypot(stroke.endX - stroke.startX,
                        stroke.endY - stroke.startY) / 2f;
                canvas.drawCircle(centerX, centerY, radius, paint);
                break;
            case LINE:
                canvas.drawLine(stroke.startX, stroke.startY,
                        stroke.endX, stroke.endY, paint);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                inProgress = new Stroke(tool, strokeColor, strokeWidth, x, y);
                if (tool == Tool.FREEHAND) inProgress.path.moveTo(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (inProgress == null) return false;
                for (int i = 0; i < event.getHistorySize(); i++) {
                    updateStroke(event.getHistoricalX(i), event.getHistoricalY(i));
                }
                updateStroke(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (inProgress == null) return false;
                updateStroke(x, y);
                layers.get(activeLayer).add(inProgress);
                inProgress = null;
                invalidate();
                performClick();
                return true;
            case MotionEvent.ACTION_CANCEL:
                inProgress = null;
                invalidate();
                return true;
            default:
                return false;
        }
    }

    private void updateStroke(float x, float y) {
        inProgress.endX = x;
        inProgress.endY = y;
        if (inProgress.tool == Tool.FREEHAND) inProgress.path.lineTo(x, y);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private static final class Stroke {
        final Tool tool;
        final int color;
        final float strokeWidth;
        final float startX;
        final float startY;
        float endX;
        float endY;
        final Path path = new Path();

        Stroke(Tool tool, int color, float strokeWidth, float x, float y) {
            this.tool = tool;
            this.color = color;
            this.strokeWidth = strokeWidth;
            startX = x;
            startY = y;
            endX = x;
            endY = y;
        }
    }
}
