package com.example.ardemoapp;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

/**
 * AR Demo Activity
 */
public class ArDemoActivity extends AppCompatActivity {
    private ArFragment arFragment;
    private Uri selectedObject;
    private AnchorNode anchorNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arSceneView);
        assert arFragment != null;
        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(view -> removeAnchorNode(anchorNode));

        findViewById(R.id.tvLamp).setOnClickListener(view -> {
            selectedObject = Uri.parse("Standing_lamp_01.sfb");
        });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                        return;
                    }
                    Anchor anchor = hitResult.createAnchor();
                    placeObject(arFragment, anchor, selectedObject);
                }
        );
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        ModelRenderable.builder()
                .setSource(this, model)
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally((throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
                }));

    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }


    private void removeAnchorNode(AnchorNode nodeToremove) {
        //Remove an anchor node
        if (nodeToremove != null) {
            arFragment.getArSceneView().getScene().removeChild(nodeToremove);
            nodeToremove.getAnchor().detach();
            nodeToremove.setParent(null);
            Toast.makeText(ArDemoActivity.this, "AnchorNode removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ArDemoActivity.this, "AnchorNode not found", Toast.LENGTH_SHORT).show();
        }
    }
}
