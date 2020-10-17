package com.sloy.sevibus.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.sloy.sevibus.R;

public class HomeActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    findViewById(R.id.goodbye_name).setOnClickListener(view -> {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/sloydev"));
      startActivity(intent);
    });

    findViewById(R.id.uninstall).setOnClickListener(view -> {
      Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      intent.setData(Uri.parse("package:" + getPackageName()));
      startActivity(intent);
    });

    findViewById(R.id.thanks).setOnClickListener(view -> {
      Toast.makeText(HomeActivity.this, "No, no, ¡gracias a ti!", Toast.LENGTH_LONG).show();
    });
  }
}
