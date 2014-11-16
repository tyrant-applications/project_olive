package com.tyrantapp.olive.services;

import com.tyrantapp.olive.types.UserInfo;

interface IOliveService {	
	int     	signUp(String username, String password);	
	int			signIn(String username, String password);
	int 		signOut();
	boolean 	isSignedIn();
	UserInfo 	getUserProfile();
	UserInfo 	getRecipientProfile(String username);
}

