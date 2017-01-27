package com.magic09.magicfileselectorsample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.magic09.magicfileselector.MagicFileSelector;


/**
 * MainActivity demonstrates the usage of MagicFileSelector.
 */
public class MainActivity extends AppCompatActivity {

    static final int FILE_FOLDER_REQUEST = 0;
    static final int STORAGE_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup browse button
        Button browseButton = (Button) findViewById(R.id.bt_browse);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browseNow();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_FOLDER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(MagicFileSelector.DATA_KEY_RETURN)) {

                // Get the chosen file.
                String filePath = data.getExtras().getString(MagicFileSelector.DATA_KEY_RETURN);
                updatePathDisplay(filePath);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST: {
                // If cancelled the result arrays are empty
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted continue to browse for file
                    startBrowse();
                }
            }
        }
    }



    /* Methods */

    /**
     * Method checks for permissions and starts the browse for folder activity.
     */
    private void browseNow() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission not yet granted
            // Check if we should show an explanation - if the user has previously been
            // asked and denied the request
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Note you should implement a dialog to explain why the permissions are required
                // and then call this
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST);
            } else {

                // No explanation needed, just request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST);
            }

        } else {
            // Permissions already granted - browse for file
            startBrowse();
        }
    }

    /**
     * Method starts the browse activity with the selected settings.
     */
    private void startBrowse() {

        // Create browse intent
        Intent browseIntent = new Intent(this, MagicFileSelector.class);

        // Setup mode
        RadioGroup modeGroup = (RadioGroup) findViewById(R.id.rg_mode);
        if (modeGroup.getCheckedRadioButtonId() == R.id.rb_file) {
            browseIntent.putExtra(MagicFileSelector.DATA_KEY_MODE, MagicFileSelector.MODE_FILE);
        } else {
            browseIntent.putExtra(MagicFileSelector.DATA_KEY_MODE, MagicFileSelector.MODE_FOLDER);
        }

        // Setup simple path if required
        CheckBox simplePath = (CheckBox) findViewById(R.id.cb_simplepath);
        if (simplePath.isChecked()) {
            browseIntent.putExtra(MagicFileSelector.DATA_KEY_SIMPLE_PATH, true);
        }

        startActivityForResult(browseIntent, FILE_FOLDER_REQUEST);
    }

    /**
     * Method updates the result TextView with the argument path.
     * @param path The path to display.
     */
    private void updatePathDisplay(String path) {
        TextView pathDisplay = (TextView) findViewById(R.id.tv_result);
        pathDisplay.setText(path);
    }

}
