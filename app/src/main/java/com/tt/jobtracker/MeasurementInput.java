package com.tt.jobtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.sstracker.R;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.AsycResponse.AsyncResponse;

public class MeasurementInput extends Activity implements AsyncResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_measurement);

        Spinner spnrUnit = (Spinner) findViewById(R.id.spnrUnit);
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapterUnits = ArrayAdapter
                .createFromResource(this, R.array.unit_array,
                        android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterUnits
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spnrUnit.setAdapter(adapterUnits);

        Spinner spnrType = (Spinner) findViewById(R.id.spnrType);
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter
                .createFromResource(this, R.array.type_array,
                        android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterType
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spnrType.setAdapter(adapterType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.takemeasurement, menu);
        return true;
    }

    @Override
    public void processFinish(ServerResult result) {
        // TODO Auto-generated method stub

    }

    public void btnSave_click(View view) {
        String width = ((EditText) findViewById(R.id.txtWidth)).getText()
                .toString();
        String height = ((EditText) findViewById(R.id.txtHeight)).getText()
                .toString();
        if (width.isEmpty() || height.isEmpty()) {
            Toast.makeText(this, "" + "Fill width and height",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("Width", width);
        resultIntent.putExtra("Height", height);
        resultIntent.putExtra("Unit", ((Spinner) findViewById(R.id.spnrUnit))
                .getSelectedItem().toString());
        resultIntent.putExtra("Type", ((Spinner) findViewById(R.id.spnrType))
                .getSelectedItem().toString());

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
