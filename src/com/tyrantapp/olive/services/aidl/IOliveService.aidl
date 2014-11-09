package com.tyrantapp.olive.services.aidl;

import com.tyrantapp.olive.services.aidl.IOliveServiceCallback;
import com.tyrantapp.olive.types.UserInfo;

interface IOliveService {	
	int     	signUp(String username, String password);	
	int			signIn(String username, String password);
	UserInfo	getSignedUserInfo();
}

