package com.tyrantapp.olive.services;

interface ISyncNetworkService {
	void syncRecipientInfo();
	void syncUnreadCount();
	void syncConversation(String username);
}

