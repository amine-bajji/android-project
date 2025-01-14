package com.example.project_android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.LinkedList;
import java.util.Queue;
import android.graphics.Point;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.graphics.RectF;
import android.media.MediaScannerConnection;

public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = Color.BLACK;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Bitmap template;
    private boolean isTemplateLoaded = false;
    private static final int TOLERANCE = 20;
    public static final int TOOL_BRUSH = 1;
    public static final int TOOL_PENCIL = 2;
    public static final int TOOL_FILL = 3;
    
    private int currentTool = TOOL_BRUSH;
    private float brushSize = 20f;
    private float pencilSize = 5f;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.FILL);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        
        // If there's a template waiting to be drawn, draw it now
        if (template != null) {
            drawTemplateOnCanvas();
        }
    }

    public void setTemplate(Bitmap newTemplate) {
        if (newTemplate == null) return;
        
        try {
            // Create a mutable copy of the template
            template = newTemplate.copy(Bitmap.Config.ARGB_8888, true);
            isTemplateLoaded = true;

            // If the canvas is ready, draw the template
            if (drawCanvas != null) {
                drawTemplateOnCanvas();
            }
            
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
            isTemplateLoaded = false;
        }
    }

    private void drawTemplateOnCanvas() {
        if (template == null || drawCanvas == null) return;

        // Clear the canvas
        drawCanvas.drawColor(Color.WHITE);

        // Scale the template to fit the canvas while maintaining aspect ratio
        float scale = Math.min(
            (float) getWidth() / template.getWidth(),
            (float) getHeight() / template.getHeight()
        );

        float newWidth = template.getWidth() * scale;
        float newHeight = template.getHeight() * scale;
        float left = (getWidth() - newWidth) / 2;
        float top = (getHeight() - newHeight) / 2;

        // Create a destination rectangle for the scaled bitmap
        RectF destRect = new RectF(left, top, left + newWidth, top + newHeight);

        // Draw the scaled template
        drawCanvas.drawBitmap(template, null, destRect, null);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                if (currentTool == TOOL_FILL) {
                    floodFill((int)touchX, (int)touchY, paintColor);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentTool != TOOL_FILL) {
                    drawPath.lineTo(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentTool != TOOL_FILL) {
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                }
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void floodFill(int x, int y, int targetColor) {
        if (x < 0 || x >= canvasBitmap.getWidth() || y < 0 || y >= canvasBitmap.getHeight()) return;

        int sourceColor = canvasBitmap.getPixel(x, y);
        if (sourceColor == targetColor) return;

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty()) {
            Point p = queue.remove();
            if (p.x < 0 || p.x >= canvasBitmap.getWidth() || 
                p.y < 0 || p.y >= canvasBitmap.getHeight()) continue;

            if (canvasBitmap.getPixel(p.x, p.y) != sourceColor) continue;

            canvasBitmap.setPixel(p.x, p.y, targetColor);

            queue.add(new Point(p.x + 1, p.y));
            queue.add(new Point(p.x - 1, p.y));
            queue.add(new Point(p.x, p.y + 1));
            queue.add(new Point(p.x, p.y - 1));
        }
    }

    public void clearCanvas() {
        drawCanvas.drawColor(Color.WHITE);
        invalidate();
    }

    public void setColor(int color) {
        paintColor = color;
        drawPaint.setColor(paintColor);
    }

    public boolean saveDrawing() {
        try {
            // Get the current timestamp for unique filename
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Drawing_" + timeStamp + ".png";

            // Get the Pictures directory
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appDir = new File(picturesDir, "MyDrawings");
            
            // Create the directory if it doesn't exist
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            // Create the file
            File file = new File(appDir, fileName);

            // Save the bitmap to file
            FileOutputStream fos = new FileOutputStream(file);
            canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            // Notify the media scanner to make the image appear in the gallery
            MediaScannerConnection.scanFile(
                getContext(),
                new String[]{file.getAbsolutePath()},
                new String[]{"image/png"},
                null
            );
            
            return true; // Successfully saved
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Failed to save
        }
    }

    // Méthode pour effacer le dessin et revenir au template
    public void clearDrawing() {
        drawCanvas.drawColor(Color.WHITE);
        isTemplateLoaded = false;
        template = null;
        invalidate();
    }

    public void setTool(int tool) {
        currentTool = tool;
        switch (tool) {
            case TOOL_BRUSH:
                drawPaint.setStyle(Paint.Style.STROKE);
                drawPaint.setStrokeWidth(brushSize);
                break;
            case TOOL_PENCIL:
                drawPaint.setStyle(Paint.Style.STROKE);
                drawPaint.setStrokeWidth(pencilSize);
                break;
            case TOOL_FILL:
                drawPaint.setStyle(Paint.Style.FILL);
                break;
        }
    }

    // Add this method to handle template loading errors
    public boolean isTemplateLoaded() {
        return isTemplateLoaded;
    }
} 