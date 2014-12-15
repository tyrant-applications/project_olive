package com.tyrantapp.olive.providers;

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
    private static final String RECIPIENTS_TABLE_NAME = "recipients";
    private static final String CONVERSATIONS_TABLE_NAME = "conversations";
    private static final int	DATABASE_VERSION = 1; 

    private static final UriMatcher sUriMatcher;
    
    private static final int RECIPIENTS					= 1; 
    private static final int RECIPIENT_ID				= 2;
    
    private static final int CONVERSATIONS				= 3;
    private static final int CONVERSATION_ID			= 4;
    private static final int CONVERSATION_RECIPIENT_ID	= 5;
    
    private static HashMap<String, String> mapRecipientsProjection;
    private static HashMap<String, String> mapConversationsProjection;
    
    public static final class RecipientColumns implements BaseColumns {     
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recipients");  
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.recipients";
        
        public static final String 		USERNAME		= "username";
        public static final String		NICKNAME		= "nickname";
        public static final String		PHONENUMBER		= "phonenumber";
        public static final String		EMAIL			= "email";
        public static final String 		UNREAD			= "unread";
        public static final String		MODIFIED		= "modified";
        public static final String		PICTURE			= "picture";
        public static final String 		STARRED			= "starred";
        
        public static final String[] 	PROJECTIONS = new String[] { _ID, USERNAME, NICKNAME, PHONENUMBER, EMAIL, UNREAD, MODIFIED ,PICTURE, STARRED, };
        public static final String 		ORDERBY = UNREAD + " DESC, " + STARRED + " DESC, " + NICKNAME;
    }  
    
    public static final class ConversationColumns implements BaseColumns {     
        public static final Uri 		CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/conversations");     
        public static final String 		CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tyrantapp.olive.conversations";
        
        public static final String 		RECIPIENT_ID	= "recipient_id";     
        public static final String 		CTX_AUTHOR		= "context_author";
        public static final String 		CTX_CATEGORY	= "context_category";
        public static final String 		CTX_DETAIL		= "context_detail";
        public static final String 		IS_RECV			= "is_receive";
        public static final String 		IS_PENDING		= "is_pending";
        public static final String		IS_READ			= "is_read";
        public static final String 		CREATED			= "created";
        
        public static final String[] 	PROJECTIONS = new String[] { _ID, RECIPIENT_ID, CTX_AUTHOR, CTX_CATEGORY, CTX_DETAIL, IS_RECV, IS_PENDING, IS_READ, CREATED };
        public static final String 		ORDERBY = CREATED;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        
    	// Recipients
        sUriMatcher.addURI(AUTHORITY, RECIPIENTS_TABLE_NAME, RECIPIENTS);
        sUriMatcher.addURI(AUTHORITY, RECIPIENTS_TABLE_NAME + "/#", RECIPIENT_ID);
 
        mapRecipientsProjection = new HashMap<String, String>();
        mapRecipientsProjection.put(RecipientColumns._ID, RecipientColumns._ID);
        mapRecipientsProjection.put(RecipientColumns.USERNAME, RecipientColumns.USERNAME);
        mapRecipientsProjection.put(RecipientColumns.NICKNAME, RecipientColumns.NICKNAME);
        mapRecipientsProjection.put(RecipientColumns.PHONENUMBER, RecipientColumns.PHONENUMBER);
        mapRecipientsProjection.put(RecipientColumns.EMAIL, RecipientColumns.EMAIL);
        mapRecipientsProjection.put(RecipientColumns.UNREAD, RecipientColumns.UNREAD);
        mapRecipientsProjection.put(RecipientColumns.MODIFIED, RecipientColumns.MODIFIED);
        mapRecipientsProjection.put(RecipientColumns.PICTURE, RecipientColumns.PICTURE);
        mapRecipientsProjection.put(RecipientColumns.STARRED, RecipientColumns.STARRED);
        
        // Conversations
        sUriMatcher.addURI(AUTHORITY, CONVERSATIONS_TABLE_NAME, CONVERSATIONS);
        sUriMatcher.addURI(AUTHORITY, CONVERSATIONS_TABLE_NAME + "/#", CONVERSATION_ID);
        sUriMatcher.addURI(AUTHORITY, CONVERSATIONS_TABLE_NAME + "/" + ConversationColumns.RECIPIENT_ID + "/#", CONVERSATION_RECIPIENT_ID);
 
        mapConversationsProjection = new HashMap<String, String>();
        mapConversationsProjection.put(ConversationColumns._ID, ConversationColumns._ID);
        mapConversationsProjection.put(ConversationColumns.RECIPIENT_ID, ConversationColumns.RECIPIENT_ID);
        mapConversationsProjection.put(ConversationColumns.CTX_AUTHOR, ConversationColumns.CTX_AUTHOR);
        mapConversationsProjection.put(ConversationColumns.CTX_CATEGORY, ConversationColumns.CTX_CATEGORY);
        mapConversationsProjection.put(ConversationColumns.CTX_DETAIL, ConversationColumns.CTX_DETAIL);
        mapConversationsProjection.put(ConversationColumns.IS_RECV, ConversationColumns.IS_RECV);
        mapConversationsProjection.put(ConversationColumns.IS_PENDING, ConversationColumns.IS_PENDING);
        mapConversationsProjection.put(ConversationColumns.IS_READ, ConversationColumns.IS_READ);
        mapConversationsProjection.put(ConversationColumns.CREATED, ConversationColumns.CREATED);        
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + RECIPIENTS_TABLE_NAME + " (" + 
            		RecipientColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
            		RecipientColumns.USERNAME + " VARCHAR(255) NOT NULL," +            		
            		RecipientColumns.NICKNAME + " VARCHAR(255)," +
            		RecipientColumns.PHONENUMBER + " VARCHAR(255)," +
            		RecipientColumns.EMAIL + " VARCHAR(255)," +
            		RecipientColumns.MODIFIED + " DATE," +
            		RecipientColumns.PICTURE + " BLOB," +
            		RecipientColumns.UNREAD + " INTEGER NOT NULL DEFAULT 0," +
            		RecipientColumns.STARRED + " BOOLEAN" +
            		");");
            
            db.execSQL("CREATE TABLE IF NOT EXISTS " + CONVERSATIONS_TABLE_NAME + " (" + 
            		ConversationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
            		ConversationColumns.RECIPIENT_ID + " INTEGER," + 
            		ConversationColumns.CTX_AUTHOR + " INTEGER," + 
            		ConversationColumns.CTX_CATEGORY + " INTEGER," + 
            		ConversationColumns.CTX_DETAIL + " VARCHAR(255)," + 
            		ConversationColumns.IS_RECV + " BOOLEAN," + 
            		ConversationColumns.IS_PENDING + " BOOLEAN," +
            		ConversationColumns.IS_READ + " BOOLEAN," +
            		ConversationColumns.CREATED + " DATE" + 
            		");");
        }
 
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + RECIPIENTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CONVERSATIONS_TABLE_NAME);
            onCreate(db);
        }
    }
 
    private DatabaseHelper dbHelper;
 
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
    	boolean bIsRecipientTable = true;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case RECIPIENTS:
                break;
            case RECIPIENT_ID:
                where = where + RecipientColumns._ID + " = " + uri.getLastPathSegment();
                break;
            case CONVERSATIONS:
            	bIsRecipientTable = false;
                break;
            case CONVERSATION_ID:
            	bIsRecipientTable = false;
                where = where + RecipientColumns._ID + " = " + uri.getLastPathSegment();
                break;
            case CONVERSATION_RECIPIENT_ID:
            	bIsRecipientTable = false;
                where = where + ConversationColumns.RECIPIENT_ID + " = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
  
        int count = 0;
        if (bIsRecipientTable) {
	        count = db.delete(RECIPIENTS_TABLE_NAME, where, whereArgs);
	        getContext().getContentResolver().notifyChange(uri, null);
        } else {
        	count = db.delete(CONVERSATIONS_TABLE_NAME, where, whereArgs);
	        getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
 
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case RECIPIENTS:
                return RecipientColumns.CONTENT_TYPE;
            case CONVERSATIONS:
                return ConversationColumns.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
 
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
    	Log.d(TAG, "insert data to " + uri.toString());
    	
        if (sUriMatcher.match(uri) != RECIPIENTS &&
        	sUriMatcher.match(uri) != CONVERSATIONS &&
        	sUriMatcher.match(uri) != CONVERSATION_RECIPIENT_ID) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        boolean bIsRecipientTable = true;
        if (sUriMatcher.match(uri) == CONVERSATIONS ||
        	sUriMatcher.match(uri) == CONVERSATION_RECIPIENT_ID) {
        	bIsRecipientTable = false;
        }
 
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = -1;
        if (bIsRecipientTable) {
	        rowId = db.insert(RECIPIENTS_TABLE_NAME, RecipientColumns.USERNAME, values);
	        if (rowId > 0) {
	            Uri insertUri = ContentUris.withAppendedId(RecipientColumns.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(insertUri, null);
	            return insertUri;
	        }
        } else {
        	rowId = db.insert(CONVERSATIONS_TABLE_NAME, null /*ConversationColumns.CTX_CATEGORY*/, values);
	        if (rowId > 0) {
	            Uri insertUri = ContentUris.withAppendedId(ConversationColumns.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(insertUri, null);
	            return insertUri;
	        }
        }
 
        throw new SQLException("Failed to insert row into " + uri);
    }
 
    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }
 
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
        case RECIPIENTS:
        case RECIPIENT_ID:
        	qb.setTables(RECIPIENTS_TABLE_NAME);
            qb.setProjectionMap(mapRecipientsProjection);
        	break;
        case CONVERSATIONS:
        case CONVERSATION_ID:
        case CONVERSATION_RECIPIENT_ID:
        	qb.setTables(CONVERSATIONS_TABLE_NAME);
            qb.setProjectionMap(mapConversationsProjection);
        	break;
        default:
        	throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        switch (sUriMatcher.match(uri)) {    
            case RECIPIENTS:
                break;
            case RECIPIENT_ID:
                selection = selection + RecipientColumns._ID + " = " + uri.getLastPathSegment();
                break;
            case CONVERSATIONS:
                break;
            case CONVERSATION_ID:
                selection = selection + ConversationColumns._ID + " = " + uri.getLastPathSegment();
                break;
            case CONVERSATION_RECIPIENT_ID:
                selection = selection + ConversationColumns.RECIPIENT_ID + " = " + uri.getLastPathSegment();
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
            case RECIPIENTS:
                count = db.update(RECIPIENTS_TABLE_NAME, values, where, whereArgs);
                break;
            case RECIPIENT_ID:
                count = db.update(RECIPIENTS_TABLE_NAME, values, RecipientColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            case CONVERSATIONS:
                count = db.update(CONVERSATIONS_TABLE_NAME, values, where, whereArgs);
                break;
            case CONVERSATION_ID:
                count = db.update(CONVERSATIONS_TABLE_NAME, values, ConversationColumns._ID + "=" + uri.getLastPathSegment(), null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
 
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }   
}
