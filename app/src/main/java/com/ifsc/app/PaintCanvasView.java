package com.ifsc.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class PaintCanvasView extends View {

    public enum Tool { FREEHAND, RECTANGLE, CIRCLE }

    private static final float STROKE_WIDTH = 8f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<List<Stroke>> layers = new ArrayList<>();
    private int activeLayer;
    private int strokeColor = Color.BLACK;
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
        paint.setStrokeWidth(STROKE_WIDTH);
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
        canvas.drawColor(Color.WHITE);
        for (List<Stroke> layer : layers) {
            for (Stroke stroke : layer) drawStroke(canvas, stroke);
        }
        if (inProgress != null) drawStroke(canvas, inProgress);
    }

    private void drawStroke(Canvas canvas, Stroke stroke) {
        paint.setColor(stroke.color);
        paint.setStrokeWidth(STROKE_WIDTH);
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
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                inProgress = new Stroke(tool, strokeColor, x, y);
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
        final float startX;
        final float startY;
        float endX;
        float endY;
        final Path path = new Path();

        Stroke(Tool tool, int color, float x, float y) {
            this.tool = tool;
            this.color = color;
            startX = x;
            startY = y;
            endX = x;
            endY = y;
        }
    }
}
