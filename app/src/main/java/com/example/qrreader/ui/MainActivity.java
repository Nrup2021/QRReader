package com.example.qrreader.ui;
import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrreader.R;
import com.example.qrreader.qrreaderlib.QRDataListener;
import com.example.qrreader.qrreaderlib.QREader;


public class MainActivity extends AppCompatActivity {

  private static final String cameraPerm = Manifest.permission.CAMERA;

  // UI
  private TextView text;

  // QREader
  private SurfaceView mySurfaceView;
  private QREader qrEader;

  boolean hasCameraPermission = false;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_main);
    hasCameraPermission = RuntimePermissionUtil.checkPermissonGranted(this, cameraPerm);

    text = findViewById(R.id.code_info);

    final Button stateBtn = findViewById(R.id.btn_start_stop);
    // change of reader state in dynamic
    stateBtn.setOnClickListener(v -> {
      if (qrEader.isCameraRunning()) {
        stateBtn.setText("Start QREader");
        qrEader.stop();
      } else {
        stateBtn.setText("Stop QREader");
        qrEader.start();
      }
    });

    stateBtn.setVisibility(View.VISIBLE);

    Button restartbtn = findViewById(R.id.btn_restart_activity);
    restartbtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        restartActivity();
      }
    });

    // Setup SurfaceView
    // -----------------
    mySurfaceView = findViewById(R.id.camera_view);

    if (hasCameraPermission) {
      // Setup QREader
      setupQREader();
    } else {
      RuntimePermissionUtil.requestPermission(MainActivity.this, cameraPerm, 100);
    }
  }

  void restartActivity() {
    startActivity(new Intent(MainActivity.this, MainActivity.class));
    finish();
  }

  void setupQREader() {
    // Init QREader
    // ------------
    qrEader = new QREader.Builder(this, mySurfaceView, new QRDataListener() {
      @Override
      public void onDetected(final String data) {
        Log.d("QREader", "Value : " + data);
        text.post(new Runnable() {
          @Override
          public void run() {
            text.setText(data);
          }
        });
      }
    }).facing(QREader.BACK_CAM)
        .enableAutofocus(true)
        .height(mySurfaceView.getHeight())
        .width(mySurfaceView.getWidth())
        .build();
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (hasCameraPermission) {

      // Cleanup in onPause()
      // --------------------
      qrEader.releaseAndCleanup();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (hasCameraPermission) {

      // Init and Start with SurfaceView
      // -------------------------------
      qrEader.initAndStart(mySurfaceView);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions,
      @NonNull final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 100) {
      RuntimePermissionUtil.onRequestPermissionsResult(grantResults, new RPResultListener() {
        @Override
        public void onPermissionGranted() {
          if (RuntimePermissionUtil.checkPermissonGranted(MainActivity.this, cameraPerm)) {
            restartActivity();
          }
        }

        @Override
        public void onPermissionDenied() {
          // do nothing
        }
      });
    }
  }
}
