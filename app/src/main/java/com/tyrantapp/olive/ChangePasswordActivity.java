package com.tyrantapp.olive;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tyrantapp.olive.network.RESTApiManager;


public class ChangePasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        setEnablePasscode(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onChangePassword(View view) {
        EditText cv = (EditText)findViewById(R.id.current_password);
        EditText nv = (EditText)findViewById(R.id.new_password);
        EditText fv = (EditText)findViewById(R.id.confirm_password);

        String currentPassword = cv.getText().toString();
        String newPassword = nv.getText().toString();
        String confirmPassword = fv.getText().toString();

        if (newPassword != null && newPassword.equals(confirmPassword)) {
            RESTApiManager helper = RESTApiManager.getInstance();
            if (helper.changePassword(currentPassword, newPassword) == RESTApiManager.OLIVE_SUCCESS) {
                Toast.makeText(this, "Succeed to change.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to change.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "New password is not matched.", Toast.LENGTH_SHORT).show();
        }
    }
}
