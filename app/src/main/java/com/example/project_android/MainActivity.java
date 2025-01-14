package com.example.project_android;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private DrawingView drawingView;

    private int[] templateResources = {
        R.drawable.template1,
        R.drawable.template2,
        R.drawable.template3,
        R.drawable.template4,
        R.drawable.template5,
        R.drawable.template6,
        R.drawable.template7,
        R.drawable.template8
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        drawingView = findViewById(R.id.drawing_view);

        // Setup color buttons
        setupColorButton(R.id.redButton, Color.RED);
        setupColorButton(R.id.pinkButton, Color.rgb(255, 105, 180));
        setupColorButton(R.id.orangeButton, Color.rgb(255, 165, 0));
        setupColorButton(R.id.yellowButton, Color.YELLOW);
        setupColorButton(R.id.greenButton, Color.GREEN);
        setupColorButton(R.id.lightBlueButton, Color.rgb(135, 206, 235));
        setupColorButton(R.id.blueButton, Color.BLUE);
        setupColorButton(R.id.purpleButton, Color.rgb(128, 0, 128));
        setupColorButton(R.id.brownButton, Color.rgb(139, 69, 19));
        setupColorButton(R.id.greyButton, Color.GRAY);
        setupColorButton(R.id.blackButton, Color.BLACK);
        setupColorButton(R.id.whiteButton, Color.WHITE);

        // Color Picker Button
        findViewById(R.id.colorPickerButton).setOnClickListener(v -> showColorPickerDialog());

        // Tool Buttons
        findViewById(R.id.brushButton).setOnClickListener(v -> {
            drawingView.setTool(DrawingView.TOOL_BRUSH);
            Toast.makeText(this, "Mode Pinceau", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.pencilButton).setOnClickListener(v -> {
            drawingView.setTool(DrawingView.TOOL_PENCIL);
            Toast.makeText(this, "Mode Crayon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.fillButton).setOnClickListener(v -> {
            drawingView.setTool(DrawingView.TOOL_FILL);
            Toast.makeText(this, "Mode Remplissage", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.templateButton).setOnClickListener(v -> showTemplateDialog());

        findViewById(R.id.saveButton).setOnClickListener(v -> checkSavePermissionAndSave());

        findViewById(R.id.clearButton).setOnClickListener(v -> {
            drawingView.clearDrawing();
            Toast.makeText(this, "Dessin effacé", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupColorButton(int buttonId, int color) {
        ImageButton button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            drawingView.setColor(color);
            Toast.makeText(this, "Couleur sélectionnée", Toast.LENGTH_SHORT).show();
        });
    }

    private void showColorPickerDialog() {
        new ColorPickerDialog.Builder(this)
                .setTitle("Choisir une couleur")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton("Confirmer",
                        (ColorEnvelopeListener) (envelope, fromUser) -> {
                            drawingView.setColor(envelope.getColor());
                            Toast.makeText(this, 
                                    "Couleur sélectionnée: #" + envelope.getHexCode(), 
                                    Toast.LENGTH_SHORT).show();
                        })
                .setNegativeButton("Annuler",
                        (DialogInterface.OnClickListener) (dialog, which) -> dialog.dismiss())
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show();
    }

    private void showTemplateDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.template_selection_dialog);
        dialog.setTitle("Choose a Template");

        GridView gridView = dialog.findViewById(R.id.templateGrid);
        gridView.setAdapter(new TemplateAdapter());

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            loadTemplate(templateResources[position]);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadTemplate(int resourceId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(this, resourceId);
            if (drawable == null) {
                Toast.makeText(this, "Failed to load template drawable", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap templateBitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(templateBitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            drawingView.setTemplate(templateBitmap);
            Toast.makeText(this, "Template loaded successfully", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, 
                "Error loading template: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    // Add this helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private class TemplateAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return templateResources.length;
        }

        @Override
        public Object getItem(int position) {
            return templateResources[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(MainActivity.this);
                imageView.setLayoutParams(new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300)); // Height in pixels
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(templateResources[position]);
            return imageView;
        }
    }

    private void checkSavePermissionAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, we don't need external storage permission
            saveDrawing();
        } else {
            // For older versions, we need to check permission
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                saveDrawing();
            } else {
                requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    private void saveDrawing() {
        try {
            if (drawingView.saveDrawing()) {
                Toast.makeText(this, "Dessin sauvegardé dans la galerie!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la sauvegarde: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveDrawing();
            } else {
                Toast.makeText(this, 
                    "Permission nécessaire pour sauvegarder", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
}