package com.safelet.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.safelet.android.R;

/**
 * The last step in the pairing process, shows that everything run succesfully and you are now protected.
 */
public class PairSafeletWizard05Connected extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_safelet_wizard04_connected);

        // Connect the buttons.
        Button okButton = findViewById(R.id.btnOkPairWizardDone);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
