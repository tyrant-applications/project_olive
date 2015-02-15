package com.tyrantapp.olive;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.type.ButtonInfo;


public class KeyCustomizeActivity extends BaseActivity {
    private final static String TAG = KeyCustomizeActivity.class.getSimpleName();

    private EditText mButtonText;
    private ImageView mButtonImage;

    private long mButtonId;
    private ButtonInfo mButtonInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_customize);

        mButtonText = (EditText)findViewById(R.id.button_text);
        mButtonImage = (ImageView)findViewById(R.id.button_image);

        mButtonId = getIntent().getLongExtra(Constants.Intent.EXTRA_BUTTON_ID, -1);

        if (mButtonId < 0) {
            finish();
            throw new IllegalArgumentException("Not found button id in intent.");
        } else {
            mButtonInfo = DatabaseHelper.PresetButtonHelper.getButtonInfo(this, mButtonId);
            onEditText(null);
        }

        setEnablePasscode(true);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_key_customize, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public void onBack(View view) {
        KeyboardView keyboardView = new KeyboardView(getApplicationContext(), null);
//        int height = (keyboardView.getKeyboard()).getHeight();
//        android.util.Log.d(TAG, "Keyboard Height = " + height);

        onBackPressed();
    }

    public void onAccept(View view) {
        mButtonInfo.mContext = mButtonText.getText().toString();
        DatabaseHelper.PresetButtonHelper.updateButton(this, mButtonId, mButtonInfo);

        onBackPressed();
    }

    public void onEditText(View view) {
        findViewById(R.id.tab_text).setSelected(true);
        findViewById(R.id.tab_opinion).setSelected(false);
        findViewById(R.id.tab_foods).setSelected(false);
        findViewById(R.id.tab_shopping).setSelected(false);
        findViewById(R.id.tab_trans).setSelected(false);

        mButtonInfo.mMimetype = OliveHelper.MIMETYPE_TEXT;
        updateButtonContext(mButtonInfo);
    }

    private void updateButtonContext(ButtonInfo info) {
        boolean bShowImage = true;
        if (OliveHelper.MIMETYPE_TEXT.equals(info.mMimetype)) {
            mButtonText.setText(info.mContext);
            bShowImage = false;
        } else
        if (OliveHelper.MIMETYPE_IMAGE.equals(info.mMimetype)) {
        } else
        if (OliveHelper.MIMETYPE_VIDEO.equals(info.mMimetype)) {
        } else
        if (OliveHelper.MIMETYPE_AUDIO.equals(info.mMimetype)) {
        } else
        if (OliveHelper.MIMETYPE_GEOLOCATE.equals(info.mMimetype)) {
        } else
        if (OliveHelper.MIMETYPE_EMOJI.equals(info.mMimetype)) {
            //holder.mButtonImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), ));
        }

        if (bShowImage) {
            mButtonText.setVisibility(View.GONE);
            mButtonImage.setVisibility(View.VISIBLE);
        } else {
            mButtonText.setVisibility(View.VISIBLE);
            mButtonImage.setVisibility(View.GONE);
            OliveHelper.showSoftInputMethod(this, mButtonText);
        }
    }

}
