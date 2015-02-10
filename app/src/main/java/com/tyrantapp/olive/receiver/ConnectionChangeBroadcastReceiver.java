package com.tyrantapp.olive.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.tyrantapp.olive.ConversationActivity;
import com.tyrantapp.olive.OliveApplication;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.service.SyncNetworkService;
import com.tyrantapp.olive.type.ChatSpaceInfo;
import com.tyrantapp.olive.util.SharedVariables;

/**
 * Created by onetop21 on 15. 2. 8.
 */
public class ConnectionChangeBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive( Context context, Intent intent )
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(     ConnectivityManager.TYPE_MOBILE );
        if ( (activeNetInfo != null) || (mobNetInfo != null) ) {
            //Toast.makeText(context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            //Toast.makeText( context, "Mobile Network Type : " + mobNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();

            String foregroundActivity = OliveHelper.getForegroundActivityName(context);
            boolean isTopPackage = foregroundActivity.startsWith(OliveApplication.class.getPackage().getName());

            Intent syncIntent = null;
            if (isTopPackage) {
                // need to call sync
                syncIntent = new Intent(context, SyncNetworkService.class).setAction(SyncNetworkService.INTENT_ACTION_SYNC_ALL);
                context.startService(syncIntent);
            }
        }
    }
}
