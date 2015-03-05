package com.tyrantapp.olive.provider;

import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class OliveContentProvider extends ContentProvider {
	public static final String	AUTHORITY = "com.tyrantapp.olive";
	public static final Uri		CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	
    private static final String TAG = "OliveContentProvider";
    private static final String DATABASE_NAME = "olive.db";
    private static final String USER_TABLE_NAME = "user";
    private static final String RECIPIENTS_TABLE_NAME = "recipients";
    private static final String SPACES_TABLE_NAME = "spaces";
    private static final String CONVERSATIONS_TABLE_NAME = "conversations";
    private static final String SPACES_VIEW_NAME = "spaces_view";
    private static final String CHATSPACES_VIEW_NAME = "chatspaces";
    private static final String PRESETBUTTONS_TABLE_NAME = "presetbuttons";
    private static final String DOWNLOADSETS_TABLE_NAME = "downloadsets";
    private static final String DOWNLOADBUTTONS_TABLE_NAME = "downloadbuttons";
    private static final int	DATABASE_VERSION = 1;

    private static final UriMatcher sUriMatcher;

    private static final int USER   					= 1;

    private static final int RECIPIENTS					= 2;
    private static final int RECIPIENT_ID				= 3;

    private static final int SPACES	        			= 4;
    private static final int SPACE_ID       			= 5;

    private static final int CONVERSATIONS				= 6;
    private static final int CONVERSATION_ID			= 7;

    private static final int CHATSPACES                 = 8;
    private static final int CHATSPACE_ID               = 9;

    private static final int PRESETBUTTONS              = 10;
    private static final int PRESETBUTTON_ID            = 11;

    private static final int DOWNLOADSETS               = 12;
    private static final int DOWNLOADSET_ID             = 13;

    private static final int DOWNLOADBUTTONS            = 14;
    private static final int DOWNLOADBUTTON_ID          = 15;

    private static HashMap<String, String> mapUserProjection;
    private static HashMap<String, String> mapRecipientsProjection;
    private static HashMap<String, String> mapSpacesProjection;
    private static HashMap<String, String> mapConversationsProjection;
    private static HashMap<String, String> mapChatSpacesProjection;
    private static HashMap<String, String> mapPresetButtonsProjection;
    private static HashMap<String, String> mapDownloadSetsProjection;
    private static HashMap<String, String> mapDownloadButtonsProjection;

    public static final class UserColumns implements BaseColumns {
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.user";
        
        public static final String 		USERNAME		= "username";
        public static final String      PASSWORD        = "password";
        public static final String      ACCESSTOKEN     = "accesstoken";
        public static final String		MODIFIED		= "modified";

        public static final String[] 	PROJECTIONS = new String[] { _ID, USERNAME, /*PASSWORD, ACCESSTOKEN,*/ MODIFIED, };
        public static final String 		ORDERBY = "LIMIT 1";
    }

    public static final class RecipientColumns implements BaseColumns {
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recipients");
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.recipients";

        public static final String 		USERNAME		= "username";
        public static final String      DISPLAYNAME     = "displayname";
        public static final String		PHONENUMBER		= "phonenumber";
        public static final String		PICTURE			= "picture";
        public static final String 		MEDIAURL 		= "mediaurl";
        public static final String		MODIFIED		= "modified";

        public static final String[] 	PROJECTIONS = new String[] { _ID, USERNAME, DISPLAYNAME, PHONENUMBER, PICTURE, MEDIAURL, MODIFIED, };
        public static final String 		ORDERBY = _ID;
    }

    public static final class SpaceColumns implements BaseColumns {
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/spaces");
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.spaces";
        public static final int         TYPE_CHAT = 0;
        public static final int         TYPE_VOTE = 1;

        public static final String 		CHATROOM_ID	    = "chatroom_id";
        public static final String      PARTICIPANTS    = "participants";
        public static final String      TITLE           = "title";
        public static final String 		TYPE		    = "type";
        public static final String		STARRED    		= "starred";

        // by View
        public static final String		UNREAD  		= "unread";
        public static final String      CONV_ID         = "conv_id";
        public static final String		SENDER			= "sender";
        public static final String		SNIPPET			= "snippet";
        public static final String      LAST_UPDATED    = "last_updated";

        public static final String[] 	PROJECTIONS = new String[] { _ID, CHATROOM_ID, PARTICIPANTS, TITLE, TYPE, STARRED, UNREAD, CONV_ID, SENDER, SNIPPET, LAST_UPDATED, };
        public static final String 		ORDERBY = UNREAD + " DESC, " + STARRED + " DESC, " + TITLE;
    }

    public static final class ConversationColumns implements BaseColumns {     
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/conversations");     
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.conversations";
        public static final int         STATUS_NONE = 0;
        public static final int         STATUS_READ = 1;    // if message read by receiver...
        public static final int         STATUS_UNREAD = 2;  // if message arrived to receiver...
        public static final int         STATUS_PENDING = 3; // if message arrived to server or pended on sender's

        public static final String 		MESSAGE_ID	    = "message_id";
        public static final String 		SPACE_ID	    = "space_id";
        public static final String 		SENDER		    = "sender";
        public static final String 		AUTHOR    	    = "author";
        public static final String 		MIMETYPE    	= "mimetype";
        public static final String 		CONTEXT 		= "context";
        public static final String 		MEDIAURL 		= "mediaurl";
        public static final String 		STATUS			= "status";
        public static final String 		CREATED			= "created";
        
        public static final String[] 	PROJECTIONS = new String[] { _ID, MESSAGE_ID, SPACE_ID, SENDER, AUTHOR, MIMETYPE, CONTEXT, MEDIAURL, STATUS, CREATED };
        public static final String 		ORDERBY = CREATED;
    }

    public static final class ChatSpaceColumns implements BaseColumns {
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/chatspaces");
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.chatspaces";

        public static final String 		CHATROOM_ID	    = "chatroom_id";
        public static final String      PARTICIPANTS    = "participants";
        public static final String      TITLE           = "title";
        public static final String		STARRED    		= "starred";
        public static final String		UNREAD  		= "unread";
        public static final String      CONV_ID         = "conv_id";
        public static final String		SENDER  		= "sender";
        public static final String		SNIPPET			= "snippet";
        public static final String      LAST_UPDATED    = "last_updated";

        public static final String 		RECIPIENT_ID    = "recipient_id";
        public static final String      DISPLAYNAME     = "displayname";
        public static final String		PHONENUMBER		= "phonenumber";
        public static final String		PICTURE			= "picture";

        public static final String[] 	PROJECTIONS = new String[] { _ID, CHATROOM_ID, PARTICIPANTS, TITLE, STARRED, UNREAD, CONV_ID, SENDER, SNIPPET, LAST_UPDATED, RECIPIENT_ID, DISPLAYNAME, PHONENUMBER, PICTURE, };
        public static final String 		ORDERBY = UNREAD + " DESC, " + STARRED + " DESC, " + TITLE;
    }

    public static final class PresetButtonColumns implements BaseColumns {
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/presetbuttons");
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.presetbuttons";

        public static final String 		INDEX           = "_index";
        public static final String      MIMETYPE        = "mimetype";
        public static final String		CONTEXT			= "context";
        public static final String      BUTTON_ID       = "button_id";
        public static final String 		AUTHOR		    = "author";
        public static final String 		VERSION		    = "version";

        public static final String[] 	PROJECTIONS = new String[] { _ID, INDEX, MIMETYPE, CONTEXT, BUTTON_ID, AUTHOR, VERSION };
        public static final String 		ORDERBY = INDEX;
    }

    public static final class DownloadSetColumns implements BaseColumns {
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/downloadsets");
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.downloadsets";

        public static final String 		INDEX		    = "_index";
        public static final String      DISPLAYNAME     = "displayname";
        public static final String 		AUTHOR		    = "author";
        public static final String      VERSION         = "version";

        public static final String[] 	PROJECTIONS = new String[] { _ID, INDEX, DISPLAYNAME, AUTHOR, VERSION, };
        public static final String 		ORDERBY = INDEX;
    }

    public static final class DownloadButtonColumns implements BaseColumns {
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/downloadbuttons");
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.downloadbuttons";

        public static final String      MIMETYPE        = "mimetype";
        public static final String		CONTEXT			= "context";
        public static final String      BUTTON_ID       = "button_id";
        public static final String 		AUTHOR		    = "author";
        public static final String 		VERSION		    = "version";

        public static final String[] 	PROJECTIONS = new String[] { _ID, MIMETYPE, CONTEXT, BUTTON_ID, AUTHOR, VERSION, };
        public static final String 		ORDERBY = BUTTON_ID;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // User
        sUriMatcher.addURI(AUTHORITY, USER_TABLE_NAME, USER);

        mapUserProjection = new HashMap<String, String>();
        mapUserProjection.put(UserColumns._ID, UserColumns._ID);
        mapUserProjection.put(UserColumns.USERNAME, UserColumns.USERNAME);
        mapUserProjection.put(UserColumns.PASSWORD, UserColumns.PASSWORD);
        mapUserProjection.put(UserColumns.ACCESSTOKEN, UserColumns.ACCESSTOKEN);
        mapUserProjection.put(UserColumns.MODIFIED, UserColumns.MODIFIED);

    	// Recipients
        sUriMatcher.addURI(AUTHORITY, RECIPIENTS_TABLE_NAME, RECIPIENTS);
        sUriMatcher.addURI(AUTHORITY, RECIPIENTS_TABLE_NAME + "/#", RECIPIENT_ID);

        mapRecipientsProjection = new HashMap<String, String>();
        mapRecipientsProjection.put(RecipientColumns._ID, RecipientColumns._ID);
        mapRecipientsProjection.put(RecipientColumns.USERNAME, RecipientColumns.USERNAME);
        mapRecipientsProjection.put(RecipientColumns.DISPLAYNAME, RecipientColumns.DISPLAYNAME);
        mapRecipientsProjection.put(RecipientColumns.PHONENUMBER, RecipientColumns.PHONENUMBER);
        mapRecipientsProjection.put(RecipientColumns.PICTURE, RecipientColumns.PICTURE);
        mapRecipientsProjection.put(RecipientColumns.MEDIAURL, RecipientColumns.MEDIAURL);
        mapRecipientsProjection.put(RecipientColumns.MODIFIED, RecipientColumns.MODIFIED);

        // Spaces
        sUriMatcher.addURI(AUTHORITY, SPACES_TABLE_NAME, SPACES);
        sUriMatcher.addURI(AUTHORITY, SPACES_TABLE_NAME + "/#", SPACE_ID);

        mapSpacesProjection = new HashMap<String, String>();
        mapSpacesProjection.put(SpaceColumns._ID, SpaceColumns._ID);
        mapSpacesProjection.put(SpaceColumns.CHATROOM_ID, SpaceColumns.CHATROOM_ID);
        mapSpacesProjection.put(SpaceColumns.PARTICIPANTS, SpaceColumns.PARTICIPANTS);
        mapSpacesProjection.put(SpaceColumns.TITLE, SpaceColumns.TITLE);
        mapSpacesProjection.put(SpaceColumns.TYPE, SpaceColumns.TYPE);
        mapSpacesProjection.put(SpaceColumns.STARRED, SpaceColumns.STARRED);
        mapSpacesProjection.put(SpaceColumns.UNREAD, SpaceColumns.UNREAD);
        mapSpacesProjection.put(SpaceColumns.CONV_ID, SpaceColumns.CONV_ID);
        mapSpacesProjection.put(SpaceColumns.SENDER, SpaceColumns.SENDER);
        mapSpacesProjection.put(SpaceColumns.SNIPPET, SpaceColumns.SNIPPET);
        mapSpacesProjection.put(SpaceColumns.LAST_UPDATED, SpaceColumns.LAST_UPDATED);

        // Conversations
        sUriMatcher.addURI(AUTHORITY, CONVERSATIONS_TABLE_NAME, CONVERSATIONS);
        sUriMatcher.addURI(AUTHORITY, CONVERSATIONS_TABLE_NAME + "/#", CONVERSATION_ID);

        mapConversationsProjection = new HashMap<String, String>();
        mapConversationsProjection.put(ConversationColumns._ID, ConversationColumns._ID);
        mapConversationsProjection.put(ConversationColumns.MESSAGE_ID, ConversationColumns.MESSAGE_ID);
        mapConversationsProjection.put(ConversationColumns.SPACE_ID, ConversationColumns.SPACE_ID);
        mapConversationsProjection.put(ConversationColumns.SENDER, ConversationColumns.SENDER);
        mapConversationsProjection.put(ConversationColumns.AUTHOR, ConversationColumns.AUTHOR);
        mapConversationsProjection.put(ConversationColumns.MIMETYPE, ConversationColumns.MIMETYPE);
        mapConversationsProjection.put(ConversationColumns.CONTEXT, ConversationColumns.CONTEXT);
        mapConversationsProjection.put(ConversationColumns.MEDIAURL, ConversationColumns.MEDIAURL);
        mapConversationsProjection.put(ConversationColumns.STATUS, ConversationColumns.STATUS);
        mapConversationsProjection.put(ConversationColumns.CREATED, ConversationColumns.CREATED);

        // ChatSpace
        sUriMatcher.addURI(AUTHORITY, CHATSPACES_VIEW_NAME, CHATSPACES);
        sUriMatcher.addURI(AUTHORITY, CHATSPACES_VIEW_NAME + "/#", CHATSPACE_ID);

        mapChatSpacesProjection = new HashMap<String, String>();
        mapChatSpacesProjection.put(ChatSpaceColumns._ID, ChatSpaceColumns._ID);
        mapChatSpacesProjection.put(ChatSpaceColumns.CHATROOM_ID, ChatSpaceColumns.CHATROOM_ID);
        mapChatSpacesProjection.put(ChatSpaceColumns.PARTICIPANTS, ChatSpaceColumns.PARTICIPANTS);
        mapChatSpacesProjection.put(ChatSpaceColumns.TITLE, ChatSpaceColumns.TITLE);
        mapChatSpacesProjection.put(ChatSpaceColumns.STARRED, ChatSpaceColumns.STARRED);
        mapChatSpacesProjection.put(ChatSpaceColumns.UNREAD, ChatSpaceColumns.UNREAD);
        mapChatSpacesProjection.put(ChatSpaceColumns.CONV_ID, ChatSpaceColumns.CONV_ID);
        mapChatSpacesProjection.put(ChatSpaceColumns.SENDER, ChatSpaceColumns.SENDER);
        mapChatSpacesProjection.put(ChatSpaceColumns.SNIPPET, ChatSpaceColumns.SNIPPET);
        mapChatSpacesProjection.put(ChatSpaceColumns.LAST_UPDATED, ChatSpaceColumns.LAST_UPDATED);
        mapChatSpacesProjection.put(ChatSpaceColumns.RECIPIENT_ID, ChatSpaceColumns.RECIPIENT_ID);
        mapChatSpacesProjection.put(ChatSpaceColumns.DISPLAYNAME, ChatSpaceColumns.DISPLAYNAME);
        mapChatSpacesProjection.put(ChatSpaceColumns.PHONENUMBER, ChatSpaceColumns.PHONENUMBER);
        mapChatSpacesProjection.put(ChatSpaceColumns.PICTURE, ChatSpaceColumns.PICTURE);

        // PresetButtons
        sUriMatcher.addURI(AUTHORITY, PRESETBUTTONS_TABLE_NAME, PRESETBUTTONS);
        sUriMatcher.addURI(AUTHORITY, PRESETBUTTONS_TABLE_NAME + "/#", PRESETBUTTON_ID);

        mapPresetButtonsProjection = new HashMap<String, String>();
        mapPresetButtonsProjection.put(PresetButtonColumns._ID, PresetButtonColumns._ID);
        mapPresetButtonsProjection.put(PresetButtonColumns.INDEX, PresetButtonColumns.INDEX);
        mapPresetButtonsProjection.put(PresetButtonColumns.MIMETYPE, PresetButtonColumns.MIMETYPE);
        mapPresetButtonsProjection.put(PresetButtonColumns.CONTEXT, PresetButtonColumns.CONTEXT);
        mapPresetButtonsProjection.put(PresetButtonColumns.BUTTON_ID, PresetButtonColumns.BUTTON_ID);
        mapPresetButtonsProjection.put(PresetButtonColumns.AUTHOR, PresetButtonColumns.AUTHOR);
        mapPresetButtonsProjection.put(PresetButtonColumns.VERSION, PresetButtonColumns.VERSION);

        // DownloadSets
        sUriMatcher.addURI(AUTHORITY, DOWNLOADSETS_TABLE_NAME, DOWNLOADSETS);
        sUriMatcher.addURI(AUTHORITY, DOWNLOADSETS_TABLE_NAME + "/#", DOWNLOADSET_ID);

        mapDownloadSetsProjection = new HashMap<String, String>();
        mapDownloadSetsProjection.put(DownloadSetColumns._ID, DownloadSetColumns._ID);
        mapDownloadSetsProjection.put(DownloadSetColumns.INDEX, DownloadSetColumns.INDEX);
        mapDownloadSetsProjection.put(DownloadSetColumns.DISPLAYNAME, DownloadSetColumns.DISPLAYNAME);
        mapDownloadSetsProjection.put(DownloadSetColumns.AUTHOR, DownloadSetColumns.AUTHOR);
        mapDownloadSetsProjection.put(DownloadSetColumns.VERSION, DownloadSetColumns.VERSION);

        // DownloadButtons
        sUriMatcher.addURI(AUTHORITY, DOWNLOADBUTTONS_TABLE_NAME, DOWNLOADBUTTONS);
        sUriMatcher.addURI(AUTHORITY, DOWNLOADBUTTONS_TABLE_NAME + "/#", DOWNLOADBUTTON_ID);

        mapDownloadButtonsProjection = new HashMap<String, String>();
        mapDownloadButtonsProjection.put(DownloadButtonColumns._ID, DownloadButtonColumns._ID);
        mapDownloadButtonsProjection.put(DownloadButtonColumns.MIMETYPE, DownloadButtonColumns.MIMETYPE);
        mapDownloadButtonsProjection.put(DownloadButtonColumns.CONTEXT, DownloadButtonColumns.CONTEXT);
        mapDownloadButtonsProjection.put(DownloadButtonColumns.BUTTON_ID, DownloadButtonColumns.BUTTON_ID);
        mapDownloadButtonsProjection.put(DownloadButtonColumns.AUTHOR, DownloadButtonColumns.AUTHOR);
        mapDownloadButtonsProjection.put(DownloadButtonColumns.VERSION, DownloadButtonColumns.VERSION);
    }
    
    private static class DatabaseOpenHelper extends SQLiteOpenHelper {
        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME + " (" +
            		UserColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            		UserColumns.USERNAME + " VARCHAR(255) NOT NULL," +
            		UserColumns.PASSWORD + " VARCHAR(255)," +
            		UserColumns.ACCESSTOKEN + " VARCHAR(255)," +
            		UserColumns.MODIFIED + " DATETIME" +
            		");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + RECIPIENTS_TABLE_NAME + " (" +
                    RecipientColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RecipientColumns.USERNAME + " VARCHAR(255) NOT NULL," +
                    RecipientColumns.DISPLAYNAME + " VARCHAR(255) NOT NULL," +
                    RecipientColumns.PHONENUMBER + " VARCHAR(255) NOT NULL," +
                    RecipientColumns.PICTURE + " VARCHAR(255)," +
                    RecipientColumns.MEDIAURL + " VARCHAR(255)," +
                    RecipientColumns.MODIFIED + " DATETIME" +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + SPACES_TABLE_NAME + " (" +
                    SpaceColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SpaceColumns.CHATROOM_ID + " INTEGER," +
                    SpaceColumns.PARTICIPANTS + " VARCHAR(255) NOT NULL," +
                    SpaceColumns.TITLE + " VARCHAR(255)," +
                    SpaceColumns.TYPE + " INTEGER NOT NULL DEFAULT 0," +
                    SpaceColumns.STARRED + " BOOLEAN NOT NULL DEFAULT FALSE" +
                    ");");
            
            db.execSQL("CREATE TABLE IF NOT EXISTS " + CONVERSATIONS_TABLE_NAME + " (" + 
            		ConversationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ConversationColumns.MESSAGE_ID + " INTEGER," +
            		ConversationColumns.SPACE_ID + " INTEGER," +
                    ConversationColumns.SENDER + " VARCHAR(255) NOT NULL," +
            		ConversationColumns.AUTHOR + " VARCHAR(255) NOT NULL," +
            		ConversationColumns.MIMETYPE + " VARCHAR(255) NOT NULL," +
                    ConversationColumns.CONTEXT + " VARCHAR(255) NOT NULL," +
                    ConversationColumns.MEDIAURL + " VARCHAR(255)," +
            		ConversationColumns.STATUS + " INTEGER NOT NULL DEFAULT 0," +
            		ConversationColumns.CREATED + " DATETIME" +
            		");");

            db.execSQL("CREATE VIEW IF NOT EXISTS " + SPACES_VIEW_NAME + " AS " +
                    "SELECT " +
                    SPACES_TABLE_NAME + ".*, " +
                    " (SELECT COUNT(" + ConversationColumns._ID + ") FROM " + CONVERSATIONS_TABLE_NAME + " WHERE " + ConversationColumns.SPACE_ID + " == " + SPACES_TABLE_NAME + "." + SpaceColumns._ID + " AND " + CONVERSATIONS_TABLE_NAME + "." + ConversationColumns.SENDER + " NOT LIKE \'%\' || (SELECT " + UserColumns.USERNAME + " FROM " + USER_TABLE_NAME + ") || \'%\' AND " + CONVERSATIONS_TABLE_NAME + "." + ConversationColumns.STATUS + " != " + ConversationColumns.STATUS_READ + ") AS " + SpaceColumns.UNREAD + "," +
                    " (SELECT " + ConversationColumns._ID      + " FROM " + CONVERSATIONS_TABLE_NAME + " WHERE " + ConversationColumns.SPACE_ID + " == " + SPACES_TABLE_NAME + "." + SpaceColumns._ID + " ORDER BY " + ConversationColumns.CREATED + " DESC LIMIT 1) AS " + SpaceColumns.CONV_ID + "," +
                    " (SELECT " + ConversationColumns.SENDER   + " FROM " + CONVERSATIONS_TABLE_NAME + " WHERE " + ConversationColumns.SPACE_ID + " == " + SPACES_TABLE_NAME + "." + SpaceColumns._ID + " ORDER BY " + ConversationColumns.CREATED + " DESC LIMIT 1) AS " + SpaceColumns.SENDER + "," +
                    " (SELECT " + ConversationColumns.CONTEXT  + " FROM " + CONVERSATIONS_TABLE_NAME + " WHERE " + ConversationColumns.SPACE_ID + " == " + SPACES_TABLE_NAME + "." + SpaceColumns._ID + " ORDER BY " + ConversationColumns.CREATED + " DESC LIMIT 1) AS " + SpaceColumns.SNIPPET + "," +
                    " (SELECT " + ConversationColumns.CREATED  + " FROM " + CONVERSATIONS_TABLE_NAME + " WHERE " + ConversationColumns.SPACE_ID + " == " + SPACES_TABLE_NAME + "." + SpaceColumns._ID + " ORDER BY " + ConversationColumns.CREATED + " DESC LIMIT 1) AS " + SpaceColumns.LAST_UPDATED +
                    " FROM " + SPACES_TABLE_NAME
            );

            db.execSQL("CREATE VIEW IF NOT EXISTS " + CHATSPACES_VIEW_NAME + " AS " +
                    "SELECT " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns._ID + " AS " + ChatSpaceColumns._ID + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.CHATROOM_ID + " AS " + ChatSpaceColumns.CHATROOM_ID + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.PARTICIPANTS + " AS " + ChatSpaceColumns.PARTICIPANTS + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.TITLE + " AS " + ChatSpaceColumns.TITLE + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.STARRED + " AS " + ChatSpaceColumns.STARRED + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.UNREAD + " AS " + ChatSpaceColumns.UNREAD + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.CONV_ID + " AS " + ChatSpaceColumns.CONV_ID + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.SENDER + " AS " + ChatSpaceColumns.SENDER + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.SNIPPET + " AS " + ChatSpaceColumns.SNIPPET + ", " +
                    SPACES_VIEW_NAME     + "." + SpaceColumns.LAST_UPDATED + " AS " + ChatSpaceColumns.LAST_UPDATED + ", " +
                    RECIPIENTS_TABLE_NAME + "." + RecipientColumns._ID + " AS " + ChatSpaceColumns.RECIPIENT_ID + ", " +
                    RECIPIENTS_TABLE_NAME + "." + RecipientColumns.DISPLAYNAME + " AS " + ChatSpaceColumns.DISPLAYNAME + ", " +
                    RECIPIENTS_TABLE_NAME + "." + RecipientColumns.PHONENUMBER + " AS " + ChatSpaceColumns.PHONENUMBER + ", " +
                    RECIPIENTS_TABLE_NAME + "." + RecipientColumns.PICTURE + " AS " + ChatSpaceColumns.PICTURE +

                    " FROM " +
                    SPACES_VIEW_NAME + " LEFT OUTER JOIN " + RECIPIENTS_TABLE_NAME +
                    " ON " +
                    SPACES_VIEW_NAME + "." + SpaceColumns.PARTICIPANTS + " LIKE \'%\' || " + RecipientColumns.USERNAME + " || \'%\'" +
                    " WHERE " +
                    SPACES_VIEW_NAME + "." + SpaceColumns.TYPE + " == " + SpaceColumns.TYPE_CHAT
            );

            db.execSQL("CREATE TABLE IF NOT EXISTS " + PRESETBUTTONS_TABLE_NAME + " (" +
                    PresetButtonColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PresetButtonColumns.INDEX + " INTEGER AUTOINCREMENT," +
                    PresetButtonColumns.MIMETYPE + " VARCHAR(255) NOT NULL," +
                    PresetButtonColumns.CONTEXT + " VARCHAR(255) NOT NULL," +
                    PresetButtonColumns.BUTTON_ID + " LONG," +
                    PresetButtonColumns.AUTHOR + " VARCHAR(255) NOT NULL," +
                    PresetButtonColumns.VERSION + " INTEGER" +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + DOWNLOADSETS_TABLE_NAME + " (" +
                    DownloadSetColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DownloadSetColumns.INDEX + " INTEGER AUTOINCREMENT," +
                    DownloadSetColumns.DISPLAYNAME + " VARCHAR(255) NOT NULL," +
                    DownloadSetColumns.AUTHOR + " VARCHAR(255) NOT NULL," +
                    DownloadSetColumns.VERSION + " VARCHAR(255)" +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + DOWNLOADBUTTONS_TABLE_NAME + " (" +
                    DownloadButtonColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DownloadButtonColumns.MIMETYPE + " VARCHAR(255) NOT NULL," +
                    DownloadButtonColumns.CONTEXT + " VARCHAR(255) NOT NULL," +
                    DownloadButtonColumns.BUTTON_ID + " LONG," +
                    DownloadButtonColumns.AUTHOR + " VARCHAR(255) NOT NULL," +
                    DownloadButtonColumns.VERSION + " INTEGER" +
                    ");");
        }
 
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + RECIPIENTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + SPACES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CONVERSATIONS_TABLE_NAME);
            db.execSQL("DROP VIEW  IF EXISTS " + SPACES_VIEW_NAME);
            db.execSQL("DROP VIEW  IF EXISTS " + CHATSPACES_VIEW_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + PRESETBUTTONS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DOWNLOADSETS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DOWNLOADBUTTONS_TABLE_NAME);
            onCreate(db);
        }
    }
 
    private DatabaseOpenHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseOpenHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;

        switch (sUriMatcher.match(uri)) {
            case USER:
                count = db.delete(USER_TABLE_NAME, where, whereArgs);
                break;
            case RECIPIENT_ID:
                where = ((where != null) ? where + " AND " : "") + RecipientColumns._ID + " = " + uri.getLastPathSegment();
            case RECIPIENTS:
                count = db.delete(RECIPIENTS_TABLE_NAME, where, whereArgs);
                break;
            case SPACE_ID:
                where = ((where != null) ? where + " AND " : "") + SpaceColumns._ID + " = " + uri.getLastPathSegment();
            case SPACES:
                count = db.delete(SPACES_TABLE_NAME, where, whereArgs);
                break;
            case CONVERSATION_ID:
                where = ((where != null) ? where + " AND " : "") +  ConversationColumns._ID + " = " + uri.getLastPathSegment();
            case CONVERSATIONS:
                count = db.delete(CONVERSATIONS_TABLE_NAME, where, whereArgs);
                break;
            case PRESETBUTTON_ID:
                where = ((where != null) ? where + " AND " : "") +  PresetButtonColumns._ID + " = " + uri.getLastPathSegment();
            case PRESETBUTTONS:
                count = db.delete(PRESETBUTTONS_TABLE_NAME, where, whereArgs);
                break;
            case DOWNLOADSET_ID:
                where = ((where != null) ? where + " AND " : "") +  DownloadSetColumns._ID + " = " + uri.getLastPathSegment();
            case DOWNLOADSETS:
                count = db.delete(DOWNLOADSETS_TABLE_NAME, where, whereArgs);
            case DOWNLOADBUTTON_ID:
                where = ((where != null) ? where + " AND " : "") +  DownloadButtonColumns._ID + " = " + uri.getLastPathSegment();
            case DOWNLOADBUTTONS:
                count = db.delete(DOWNLOADBUTTONS_TABLE_NAME, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            switch (sUriMatcher.match(uri)) {
                case CONVERSATIONS:
                case CONVERSATION_ID:
                    getContext().getContentResolver().notifyChange(SpaceColumns.CONTENT_URI, null);
                case SPACES:
                case SPACE_ID:
                case RECIPIENTS:
                case RECIPIENT_ID:
                    getContext().getContentResolver().notifyChange(ChatSpaceColumns.CONTENT_URI, null);
                    break;
            }
        }

        return count;
    }
 
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case USER:
                return UserColumns.CONTENT_TYPE;
            case RECIPIENT_ID:
            case RECIPIENTS:
                return RecipientColumns.CONTENT_TYPE;
            case SPACE_ID:
            case SPACES:
                return SpaceColumns.CONTENT_TYPE;
            case CONVERSATION_ID:
            case CONVERSATIONS:
                return ConversationColumns.CONTENT_TYPE;
            case PRESETBUTTON_ID:
            case PRESETBUTTONS:
                return PresetButtonColumns.CONTENT_TYPE;
            case DOWNLOADSET_ID:
            case DOWNLOADSETS:
                return DownloadSetColumns.CONTENT_TYPE;
            case DOWNLOADBUTTON_ID:
            case DOWNLOADBUTTONS:
                return DownloadButtonColumns.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
    	Log.d(TAG, "insert data to " + uri.toString());

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = -1;
        Uri insertUri = null;

        if (sUriMatcher.match(uri) == USER) {
            rowId = db.insert(USER_TABLE_NAME, UserColumns.USERNAME, values);
            insertUri = ContentUris.withAppendedId(UserColumns.CONTENT_URI, rowId);
        } else
        if (sUriMatcher.match(uri) == RECIPIENTS ||
            sUriMatcher.match(uri) == RECIPIENT_ID) {
            rowId = db.insert(RECIPIENTS_TABLE_NAME, RecipientColumns.USERNAME, values);
            insertUri = ContentUris.withAppendedId(RecipientColumns.CONTENT_URI, rowId);
        } else
        if (sUriMatcher.match(uri) == SPACES ||
            sUriMatcher.match(uri) == SPACE_ID) {
            rowId = db.insert(SPACES_TABLE_NAME, SpaceColumns.CHATROOM_ID, values);
            insertUri = ContentUris.withAppendedId(SpaceColumns.CONTENT_URI, rowId);
        } else
        if (sUriMatcher.match(uri) == CONVERSATIONS ||
            sUriMatcher.match(uri) == CONVERSATION_ID) {
            rowId = db.insert(CONVERSATIONS_TABLE_NAME, ConversationColumns.CONTEXT, values);
            insertUri = ContentUris.withAppendedId(ConversationColumns.CONTENT_URI, rowId);
        } else
        if (sUriMatcher.match(uri) == PRESETBUTTONS ||
                sUriMatcher.match(uri) == PRESETBUTTON_ID) {
            rowId = db.insert(PRESETBUTTONS_TABLE_NAME, PresetButtonColumns.CONTEXT, values);
            insertUri = ContentUris.withAppendedId(PresetButtonColumns.CONTENT_URI, rowId);
        } else
        if (sUriMatcher.match(uri) == DOWNLOADSETS ||
                sUriMatcher.match(uri) == DOWNLOADSET_ID) {
            rowId = db.insert(DOWNLOADSETS_TABLE_NAME, DownloadSetColumns.AUTHOR, values);
            insertUri = ContentUris.withAppendedId(DownloadSetColumns.CONTENT_URI, rowId);
        } else
        if (sUriMatcher.match(uri) == DOWNLOADBUTTONS ||
                sUriMatcher.match(uri) == DOWNLOADBUTTON_ID) {
            rowId = db.insert(DOWNLOADBUTTONS_TABLE_NAME, DownloadButtonColumns.CONTEXT, values);
            insertUri = ContentUris.withAppendedId(DownloadButtonColumns.CONTENT_URI, rowId);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (rowId >= 0) {
            getContext().getContentResolver().notifyChange(insertUri, null);
            switch (sUriMatcher.match(uri)) {
                case CONVERSATIONS:
                case CONVERSATION_ID:
                    getContext().getContentResolver().notifyChange(SpaceColumns.CONTENT_URI, null);
                case SPACES:
                case SPACE_ID:
                case RECIPIENTS:
                case RECIPIENT_ID:
                    getContext().getContentResolver().notifyChange(ChatSpaceColumns.CONTENT_URI, null);
                    break;
            }

            return insertUri;
        }
 
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case USER:
                qb.setTables(USER_TABLE_NAME);
                qb.setProjectionMap(mapUserProjection);
                break;
            case RECIPIENT_ID:
                selection = ((selection != null) ? selection + " AND " : "") + RecipientColumns._ID + " = " + uri.getLastPathSegment();
            case RECIPIENTS:
                qb.setTables(RECIPIENTS_TABLE_NAME);
                qb.setProjectionMap(mapRecipientsProjection);
                break;
            case SPACE_ID:
                selection = ((selection != null) ? selection + " AND " : "") + SpaceColumns._ID + " = " + uri.getLastPathSegment();
            case SPACES:
                qb.setTables(SPACES_VIEW_NAME);
                qb.setProjectionMap(mapSpacesProjection);
                break;
            case CONVERSATION_ID:
                selection = ((selection != null) ? selection + " AND " : "") + ConversationColumns._ID + " = " + uri.getLastPathSegment();
            case CONVERSATIONS:
                qb.setTables(CONVERSATIONS_TABLE_NAME);
                qb.setProjectionMap(mapConversationsProjection);
                break;
            case CHATSPACE_ID:
                selection = ((selection != null) ? selection + " AND " : "") + ChatSpaceColumns._ID + " = " + uri.getLastPathSegment();
            case CHATSPACES:
                qb.setTables(CHATSPACES_VIEW_NAME);
                qb.setProjectionMap(mapChatSpacesProjection);
                break;
            case PRESETBUTTON_ID:
                selection = ((selection != null) ? selection + " AND " : "") + PresetButtonColumns._ID + " = " + uri.getLastPathSegment();
            case PRESETBUTTONS:
                qb.setTables(PRESETBUTTONS_TABLE_NAME);
                qb.setProjectionMap(mapPresetButtonsProjection);
                break;
            case DOWNLOADSET_ID:
                selection = ((selection != null) ? selection + " AND " : "") + DownloadSetColumns._ID + " = " + uri.getLastPathSegment();
            case DOWNLOADSETS:
                qb.setTables(DOWNLOADSETS_TABLE_NAME);
                qb.setProjectionMap(mapDownloadSetsProjection);
                break;
            case DOWNLOADBUTTON_ID:
                selection = ((selection != null) ? selection + " AND " : "") + DownloadButtonColumns._ID + " = " + uri.getLastPathSegment();
            case DOWNLOADBUTTONS:
                qb.setTables(DOWNLOADBUTTONS_TABLE_NAME);
                qb.setProjectionMap(mapDownloadButtonsProjection);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }
 
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case USER:
                count = db.update(USER_TABLE_NAME, values, where, whereArgs);
                break;
            case RECIPIENTS:
                count = db.update(RECIPIENTS_TABLE_NAME, values, where, whereArgs);
                break;
            case RECIPIENT_ID:
                count = db.update(RECIPIENTS_TABLE_NAME, values, ((where != null) ? where + " AND " : "") + RecipientColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            case SPACES:
                count = db.update(SPACES_TABLE_NAME, values, where, whereArgs);
                break;
            case SPACE_ID:
                count = db.update(SPACES_TABLE_NAME, values, ((where != null) ? where + " AND " : "") + SpaceColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            case CONVERSATIONS:
                count = db.update(CONVERSATIONS_TABLE_NAME, values, where, whereArgs);
                break;
            case CONVERSATION_ID:
                count = db.update(CONVERSATIONS_TABLE_NAME, values, ((where != null) ? where + " AND " : "") + ConversationColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            case PRESETBUTTONS:
                count = db.update(PRESETBUTTONS_TABLE_NAME, values, where, whereArgs);
                break;
            case PRESETBUTTON_ID:
                count = db.update(PRESETBUTTONS_TABLE_NAME, values, ((where != null) ? where + " AND " : "") + PresetButtonColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            case DOWNLOADSETS:
                count = db.update(DOWNLOADSETS_TABLE_NAME, values, where, whereArgs);
                break;
            case DOWNLOADSET_ID:
                count = db.update(DOWNLOADSETS_TABLE_NAME, values, ((where != null) ? where + " AND " : "") + DownloadSetColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            case DOWNLOADBUTTONS:
                count = db.update(DOWNLOADBUTTONS_TABLE_NAME, values, where, whereArgs);
                break;
            case DOWNLOADBUTTON_ID:
                count = db.update(DOWNLOADBUTTONS_TABLE_NAME, values, ((where != null) ? where + " AND " : "") + DownloadButtonColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            switch (sUriMatcher.match(uri)) {
                case CONVERSATIONS:
                case CONVERSATION_ID:
                    getContext().getContentResolver().notifyChange(SpaceColumns.CONTENT_URI, null);
                case SPACES:
                case SPACE_ID:
                case RECIPIENTS:
                case RECIPIENT_ID:
                    getContext().getContentResolver().notifyChange(ChatSpaceColumns.CONTENT_URI, null);
                    break;
            }
        }

        return count;
    }   
}
