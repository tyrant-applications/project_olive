package com.tyrantapp.olive.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.tyrantapp.olive.provider.OliveContentProvider;
import com.tyrantapp.olive.type.ChatSpaceInfo;
import com.tyrantapp.olive.type.ConversationMessage;
import com.tyrantapp.olive.type.RecipientInfo;
import com.tyrantapp.olive.type.SpaceInfo;
import com.tyrantapp.olive.type.UserProfile;

import java.util.ArrayList;

/**
 * Created by onetop21 on 15. 1. 6.
 */
public class DatabaseHelper {
    private final static String TAG = DatabaseHelper.class.getSimpleName();

    public static class UserHelper {

        public static UserProfile getUserProfile(Context context) {
            UserProfile profile = null;
            Cursor cursor = context.getContentResolver().query(OliveContentProvider.UserColumns.CONTENT_URI, OliveContentProvider.UserColumns.PROJECTIONS, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();

                    profile = new UserProfile();
                    profile.mUsername = cursor.getString(cursor.getColumnIndex(OliveContentProvider.UserColumns.USERNAME));
                    //profile.mPicture = cursor.getBlob(cursor.getColumnIndex(OliveContentProvider.UserColumns.PICTURE));
                    profile.mModified = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.UserColumns.MODIFIED));
                }
                cursor.close();
            }
            return profile;
        }

        public static boolean removeUserProfile(Context context) {
            boolean bRet = false;
            Cursor cursor = context.getContentResolver().query(OliveContentProvider.UserColumns.CONTENT_URI, OliveContentProvider.UserColumns.PROJECTIONS, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    if (context.getContentResolver().delete(OliveContentProvider.UserColumns.CONTENT_URI, null, null) > 0) {
                        bRet = true;
                    }
                }
                cursor.close();
            }
            return bRet;
        }

        public static boolean updateUserProfile(Context context, UserProfile profile) {
            boolean bRet = false;
            if (profile != null) {
                Cursor cursor = context.getContentResolver().query(OliveContentProvider.UserColumns.CONTENT_URI, OliveContentProvider.UserColumns.PROJECTIONS, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        long id = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.UserColumns._ID));
                        ContentValues values = new ContentValues();
                        values.put(OliveContentProvider.UserColumns.USERNAME, profile.mUsername);
                        //values.put(OliveContentProvider.UserColumns.PICTURE, profile.mPicture);
                        values.put(OliveContentProvider.UserColumns.MODIFIED, profile.mModified);
                        if (context.getContentResolver().update(OliveContentProvider.UserColumns.CONTENT_URI, values, OliveContentProvider.UserColumns._ID + "=?", new String[]{String.valueOf(id),}) > 0) {
                            bRet = true;
                        }
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(OliveContentProvider.UserColumns.USERNAME, profile.mUsername);
                        //values.put(OliveContentProvider.UserColumns.PICTURE, profile.mPicture);
                        values.put(OliveContentProvider.UserColumns.MODIFIED, profile.mModified);
                        Uri uri = context.getContentResolver().insert(OliveContentProvider.UserColumns.CONTENT_URI, values);
                        if (Long.valueOf(uri.getLastPathSegment()) >= 0) {
                            bRet = true;
                        }
                    }
                    cursor.close();
                }
            }
            return bRet;
        }

        public static boolean updateUserPassword(Context context, String password) {
            boolean bRet = false;
            if (password != null) {
                Cursor cursor = context.getContentResolver().query(OliveContentProvider.UserColumns.CONTENT_URI, new String[] { OliveContentProvider.UserColumns._ID, OliveContentProvider.UserColumns.PASSWORD, }, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        long id = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.UserColumns._ID));
                        ContentValues values = new ContentValues();
                        values.put(OliveContentProvider.UserColumns.PASSWORD, password);
                        if (context.getContentResolver().update(OliveContentProvider.UserColumns.CONTENT_URI, values, OliveContentProvider.UserColumns._ID + "=?", new String[]{String.valueOf(id),}) > 0) {
                            bRet = true;
                        }
                    }
                    cursor.close();
                }
            }
            return bRet;
        }

        public static boolean updateAccessToken(Context context, String accesstoken) {
            boolean bRet = false;
            if (accesstoken != null) {
                Cursor cursor = context.getContentResolver().query(OliveContentProvider.UserColumns.CONTENT_URI, new String[] { OliveContentProvider.UserColumns._ID, OliveContentProvider.UserColumns.ACCESSTOKEN, }, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        long id = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.UserColumns._ID));
                        ContentValues values = new ContentValues();
                        values.put(OliveContentProvider.UserColumns.ACCESSTOKEN, accesstoken);
                        if (context.getContentResolver().update(OliveContentProvider.UserColumns.CONTENT_URI, values, OliveContentProvider.UserColumns._ID + "=?", new String[]{String.valueOf(id),}) > 0) {
                            bRet = true;
                        }
                    }
                    cursor.close();
                }
            }
            return bRet;
        }

        // WARNING! THIS CLASS IS TREATING TOO SENSITIVE DATA (PRIVATE INFORMATION)
        public static class UserPrivateHelper {

            public static String getUserPassword(Context context) {
                String pszRet = null;
                Cursor cursor = context.getContentResolver().query(OliveContentProvider.UserColumns.CONTENT_URI, new String[] { OliveContentProvider.UserColumns._ID, OliveContentProvider.UserColumns.PASSWORD, }, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        pszRet = cursor.getString(cursor.getColumnIndex(OliveContentProvider.UserColumns.PASSWORD));
                    }
                    cursor.close();
                }
                return pszRet;
            }

            public static String getAccessToken(Context context) {
                String pszRet = null;
                Cursor cursor = context.getContentResolver().query(OliveContentProvider.UserColumns.CONTENT_URI, new String[] { OliveContentProvider.UserColumns._ID, OliveContentProvider.UserColumns.ACCESSTOKEN, }, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        pszRet = cursor.getString(cursor.getColumnIndex(OliveContentProvider.UserColumns.ACCESSTOKEN));
                    }
                    cursor.close();
                }
                return pszRet;
            }
        }
    }

    public static class RecipientHelper {

        public static long addRecipient(Context context, RecipientInfo info) {
            if (info != null) {
                ContentValues values = new ContentValues();
                values.put(OliveContentProvider.RecipientColumns.USERNAME, info.mUsername);
                values.put(OliveContentProvider.RecipientColumns.DISPLAYNAME, info.mDisplayname);
                values.put(OliveContentProvider.RecipientColumns.PHONENUMBER, info.mPhoneNumber);
                values.put(OliveContentProvider.RecipientColumns.MODIFIED, info.mModified);
                Uri uri = context.getContentResolver().insert(OliveContentProvider.RecipientColumns.CONTENT_URI, values);
                return Long.valueOf(uri.getLastPathSegment());
            } else {
                return -1;
            }
        }

        public static boolean removeRecipient(Context context, long recipientId) {
            boolean bRet = false;
            Uri uri = Uri.withAppendedPath(OliveContentProvider.RecipientColumns.CONTENT_URI, String.valueOf(recipientId));
            if (context.getContentResolver().delete(uri, null, null) > 0) {
                bRet = true;
            }
            return bRet;
        }

        public static boolean updateRecipient(Context context, RecipientInfo info) {
            boolean bRet = false;
            if (info != null) {
                long recipientId = getRecipientId(context, info.mUsername);
                Uri uri = Uri.withAppendedPath(OliveContentProvider.RecipientColumns.CONTENT_URI, String.valueOf(recipientId));

                ContentValues values = new ContentValues();
                values.put(OliveContentProvider.RecipientColumns.USERNAME, info.mUsername);
                values.put(OliveContentProvider.RecipientColumns.DISPLAYNAME, info.mDisplayname);
                values.put(OliveContentProvider.RecipientColumns.PHONENUMBER, info.mPhoneNumber);
                values.put(OliveContentProvider.RecipientColumns.MODIFIED, info.mModified);
                if (context.getContentResolver().update(uri, values, null, null) > 0) {
                    bRet = true;
                }
            }
            return bRet;
        }

        public static RecipientInfo getRecipientInfo(Context context, long recipientId) {
            RecipientInfo info = null;
            if (recipientId >= 0) {
                Uri uri = Uri.withAppendedPath(OliveContentProvider.RecipientColumns.CONTENT_URI, String.valueOf(recipientId));
                Cursor cursor = context.getContentResolver().query(uri, OliveContentProvider.RecipientColumns.PROJECTIONS, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        info = new RecipientInfo();
                        info.mId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.RecipientColumns._ID));
                        info.mUsername = cursor.getString(cursor.getColumnIndex(OliveContentProvider.RecipientColumns.USERNAME));
                        info.mDisplayname = cursor.getString(cursor.getColumnIndex(OliveContentProvider.RecipientColumns.DISPLAYNAME));
                        info.mPhoneNumber = cursor.getString(cursor.getColumnIndex(OliveContentProvider.RecipientColumns.PHONENUMBER));
                        info.mModified = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.RecipientColumns.MODIFIED));
                    }
                    cursor.close();
                }
            }

            return info;
        }

        public static long getRecipientId(Context context, String recipientName) {
            long lRecipientId = -1;

            if (recipientName != null) {
                Cursor cursor = context.getContentResolver().query(
                        OliveContentProvider.RecipientColumns.CONTENT_URI,
                        new String[] { OliveContentProvider.RecipientColumns._ID, },
                        OliveContentProvider.RecipientColumns.USERNAME + "=?",
                        new String[] { recipientName, },
                        null);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    lRecipientId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.RecipientColumns._ID));
                }
            }

            return lRecipientId;
        }

        public static Cursor getCursor(Context context) {
            return context.getContentResolver().query(OliveContentProvider.RecipientColumns.CONTENT_URI, OliveContentProvider.RecipientColumns.PROJECTIONS, null, null, OliveContentProvider.RecipientColumns.ORDERBY);
        }
    }

    public static class SpaceHelper {

        public static long addSpace(Context context, SpaceInfo info) {
            if (info != null) {
                ContentValues values = new ContentValues();
                values.put(OliveContentProvider.SpaceColumns.CHATROOM_ID, info.mChatroomId);
                values.put(OliveContentProvider.SpaceColumns.PARTICIPANTS, info.mParticipants);
                values.put(OliveContentProvider.SpaceColumns.TITLE, info.mTitle);
                values.put(OliveContentProvider.SpaceColumns.TYPE, info.mType);
                values.put(OliveContentProvider.SpaceColumns.STARRED, info.mStarred);
                Uri uri = context.getContentResolver().insert(OliveContentProvider.SpaceColumns.CONTENT_URI, values);
                return Long.valueOf(uri.getLastPathSegment());
            } else {
                return -1;
            }
        }

        public static boolean removeSpace(Context context, long spaceId) {
            boolean bRet = false;
            Uri uri = Uri.withAppendedPath(OliveContentProvider.SpaceColumns.CONTENT_URI, String.valueOf(spaceId));
            if (context.getContentResolver().delete(uri, null, null) > 0) {
                bRet = true;
            }
            return bRet;
        }

        public static boolean updateSpaceInfo(Context context, SpaceInfo info) {
            boolean bRet = false;
            if (info != null) {
                long spaceId = getSpaceId(context, info.mChatroomId);
                Uri uri = Uri.withAppendedPath(OliveContentProvider.SpaceColumns.CONTENT_URI, String.valueOf(spaceId));

                ContentValues values = new ContentValues();
                values.put(OliveContentProvider.SpaceColumns.CHATROOM_ID, info.mChatroomId);
                values.put(OliveContentProvider.SpaceColumns.PARTICIPANTS, info.mParticipants);
                values.put(OliveContentProvider.SpaceColumns.TITLE, info.mTitle);
                values.put(OliveContentProvider.SpaceColumns.TYPE, info.mType);
                values.put(OliveContentProvider.SpaceColumns.STARRED, info.mStarred);
//                values.put(OliveContentProvider.SpaceColumns.UNREAD, info.mUnread);
//                values.put(OliveContentProvider.SpaceColumns.SNIPPET, info.mSnippet);
//                values.put(OliveContentProvider.SpaceColumns.LAST_UPDATED, info.mLastUpdated);
                if (context.getContentResolver().update(uri, values, null, null) > 0) {
                    bRet = true;
                }
            }
            return bRet;
        }

        public static SpaceInfo getSpaceInfo(Context context, long spaceId) {
            SpaceInfo info = null;
            if (spaceId >= 0) {
                Uri uri = Uri.withAppendedPath(OliveContentProvider.SpaceColumns.CONTENT_URI, String.valueOf(spaceId));
                Cursor cursor = context.getContentResolver().query(uri, OliveContentProvider.SpaceColumns.PROJECTIONS, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        info = new SpaceInfo();
                        info.mId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.SpaceColumns._ID));
                        info.mChatroomId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.CHATROOM_ID));
                        info.mParticipants = cursor.getString(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.PARTICIPANTS));
                        info.mTitle = cursor.getString(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.TITLE));
                        info.mType = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.TYPE));
                        info.mStarred = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.STARRED)) > 0;
                        info.mUnread = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.UNREAD));
                        info.mConversationId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.CONV_ID));
                        info.mSender = cursor.getString(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.SENDER));
                        info.mSnippet = cursor.getString(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.SNIPPET));
                        info.mLastUpdated = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.LAST_UPDATED));
                    }
                    cursor.close();
                }
            }

            return info;
        }

        public static long getSpaceId(Context context, long chatroom_id) {
            long lSpaceId = -1;

            if (chatroom_id >= 0) {
                Cursor cursor = context.getContentResolver().query(
                        OliveContentProvider.SpaceColumns.CONTENT_URI,
                        new String[] { OliveContentProvider.SpaceColumns._ID, },
                        OliveContentProvider.SpaceColumns.CHATROOM_ID + "=?",
                        new String[] { String.valueOf(chatroom_id), },
                        null);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    lSpaceId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.SpaceColumns._ID));
                }
            }

            return lSpaceId;
        }

        public static long getRoomId(Context context, long spaceId) {
            int idRoom = -1;

            if (spaceId >= 0) {
                Cursor cursor = context.getContentResolver().query(
                        OliveContentProvider.SpaceColumns.CONTENT_URI,
                        new String[] { OliveContentProvider.SpaceColumns.CHATROOM_ID, },
                        OliveContentProvider.SpaceColumns._ID + "=?",
                        new String[] { String.valueOf(spaceId), },
                        null);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    idRoom = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.SpaceColumns.CHATROOM_ID));
                }
            }

            return idRoom;
        }

        public static boolean setStarred(Context context, long spaceId, boolean flag) {
            ContentValues values = new ContentValues();
            values.put(OliveContentProvider.SpaceColumns.STARRED, flag);
            return context.getContentResolver().update(
                    Uri.withAppendedPath(OliveContentProvider.SpaceColumns.CONTENT_URI, String.valueOf(spaceId)),
                    values,
                    null, null) > 0;
        }

        public static Cursor getCursor(Context context) {
            return context.getContentResolver().query(OliveContentProvider.SpaceColumns.CONTENT_URI, OliveContentProvider.SpaceColumns.PROJECTIONS, null, null, OliveContentProvider.SpaceColumns.ORDERBY);
        }
    }

    public static class ConversationHelper {

        public static long addMessage(Context context, ConversationMessage message) {
            if (message != null) {
                ContentValues values = new ContentValues();
                values.put(OliveContentProvider.ConversationColumns.MESSAGE_ID, message.mMessageId);
                values.put(OliveContentProvider.ConversationColumns.SPACE_ID, message.mSpaceId);
                values.put(OliveContentProvider.ConversationColumns.SENDER, message.mSender);
                values.put(OliveContentProvider.ConversationColumns.AUTHOR, message.mAuthor);
                values.put(OliveContentProvider.ConversationColumns.MIMETYPE, message.mMimetype);
                values.put(OliveContentProvider.ConversationColumns.CONTEXT, message.mContext);
                values.put(OliveContentProvider.ConversationColumns.STATUS, OliveContentProvider.ConversationColumns.STATUS_PENDING);
                values.put(OliveContentProvider.ConversationColumns.CREATED, message.mCreated);
                Uri uri = context.getContentResolver().insert(OliveContentProvider.ConversationColumns.CONTENT_URI, values);
                return Long.valueOf(uri.getLastPathSegment());
            } else {
                return -1;
            }
        }

        public static boolean removeMessage(Context context, long conversationId) {
            boolean bRet = false;
            Uri uri = Uri.withAppendedPath(OliveContentProvider.ConversationColumns.CONTENT_URI, String.valueOf(conversationId));
            if (context.getContentResolver().delete(uri, null, null) > 0) {
                bRet = true;
            }
            return bRet;
        }

        public static ConversationMessage getMessage(Context context, long conversationId) {
            ConversationMessage message = null;
            if (conversationId >= 0) {
                Uri uri = Uri.withAppendedPath(OliveContentProvider.ConversationColumns.CONTENT_URI, String.valueOf(conversationId));
                Cursor cursor = context.getContentResolver().query(uri, OliveContentProvider.ConversationColumns.PROJECTIONS, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        message = new ConversationMessage();
                        message.mId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns._ID));
                        message.mMessageId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.MESSAGE_ID));
                        message.mSpaceId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.SPACE_ID));
                        message.mSender = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.SENDER));
                        message.mAuthor = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.AUTHOR));
                        message.mMimetype = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.MIMETYPE));
                        message.mContext = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.CONTEXT));
                        message.mStatus = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.STATUS));
                        message.mCreated = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.CREATED));
                    }
                    cursor.close();
                }
            }

            return message;
        }

        public static ArrayList<ConversationMessage> getPendingMessages(Context context, long idSpace) {
            ArrayList<ConversationMessage> arrRet = new ArrayList<ConversationMessage>();
            Uri uri = OliveContentProvider.ConversationColumns.CONTENT_URI;
            Cursor cursor = null;
            if (idSpace < 0) {
                cursor = context.getContentResolver().query(uri, OliveContentProvider.ConversationColumns.PROJECTIONS,
                        OliveContentProvider.ConversationColumns.STATUS + "=?",
                        new String[]{String.valueOf(OliveContentProvider.ConversationColumns.STATUS_PENDING),},
                        OliveContentProvider.ConversationColumns.ORDERBY);
            } else {
                cursor = context.getContentResolver().query(uri, OliveContentProvider.ConversationColumns.PROJECTIONS,
                        OliveContentProvider.ConversationColumns.STATUS + "=? AND " + OliveContentProvider.ConversationColumns.SPACE_ID + "=?",
                        new String[]{ String.valueOf(OliveContentProvider.ConversationColumns.STATUS_PENDING), String.valueOf(idSpace) },
                        OliveContentProvider.ConversationColumns.ORDERBY);
            }
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        ConversationMessage message = new ConversationMessage();
                        message.mId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns._ID));
                        message.mMessageId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.MESSAGE_ID));
                        message.mSpaceId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.SPACE_ID));
                        message.mSender = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.SENDER));
                        message.mAuthor = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.AUTHOR));
                        message.mMimetype = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.MIMETYPE));
                        message.mContext = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.CONTEXT));
                        message.mStatus = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.STATUS));
                        message.mCreated = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.CREATED));

                        arrRet.add(message);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            return arrRet;
        }

        public static long getConversationId(Context context, long message_id) {
            long idConv = -1;

            if (message_id >= 0) {
                Cursor cursor = context.getContentResolver().query(
                        OliveContentProvider.ConversationColumns.CONTENT_URI,
                        new String[] { OliveContentProvider.ConversationColumns._ID, },
                        OliveContentProvider.ConversationColumns.MESSAGE_ID + "=?",
                        new String[] { String.valueOf(message_id), },
                        null);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    idConv = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ConversationColumns._ID));
                }
            }

            return idConv;
        }

        public static long getMessageId(Context context, long convId) {
            long idMessage = -1;

            if (convId >= 0) {
                Cursor cursor = context.getContentResolver().query(
                        OliveContentProvider.ConversationColumns.CONTENT_URI,
                        new String[] { OliveContentProvider.ConversationColumns.MESSAGE_ID, },
                        OliveContentProvider.ConversationColumns._ID + "=?",
                        new String[] { String.valueOf(convId), },
                        null);

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    idMessage = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.ConversationColumns.MESSAGE_ID));
                }
            }

            return idMessage;
        }

        public static boolean setDispendToMyMessage(Context context, long idConversation, long idMessage, long timestamp) {
            ContentValues values = new ContentValues();
            values.put(OliveContentProvider.ConversationColumns.MESSAGE_ID, idMessage);
            //values.put(OliveContentProvider.ConversationColumns.CREATED, timestamp);
            values.put(OliveContentProvider.ConversationColumns.STATUS, OliveContentProvider.ConversationColumns.STATUS_UNREAD);
            return context.getContentResolver().update(
                    OliveContentProvider.ConversationColumns.CONTENT_URI,
                    values,
                    OliveContentProvider.ConversationColumns._ID + "=? AND " + OliveContentProvider.ConversationColumns.SENDER + "=?",
                    new String[] {String.valueOf(idConversation), UserHelper.getUserProfile(context).mUsername, } ) > 0;
        }

        public static boolean setReadToOtherMessages(Context context, long idSpace) {
            ContentValues values = new ContentValues();
            values.put(OliveContentProvider.ConversationColumns.STATUS, OliveContentProvider.ConversationColumns.STATUS_READ);
            return context.getContentResolver().update(
                    OliveContentProvider.ConversationColumns.CONTENT_URI,
                    values,
                    OliveContentProvider.ConversationColumns.SPACE_ID + "=? AND " + OliveContentProvider.ConversationColumns.SENDER + "!=?",
                    new String[] {String.valueOf(idSpace), UserHelper.getUserProfile(context).mUsername, } ) > 0;
        }

        public static Cursor getCursor(Context context, long spaceId) {
            return context.getContentResolver().query(
                    OliveContentProvider.ConversationColumns.CONTENT_URI,
                    OliveContentProvider.ConversationColumns.PROJECTIONS,
                    OliveContentProvider.ConversationColumns.SPACE_ID + "=?",
                    new String[] { String.valueOf(spaceId), },
                    OliveContentProvider.ConversationColumns.ORDERBY);
        }
    }

    public static class ChatSpaceHelper {

        public static Cursor getCursor(Context context) {
            return context.getContentResolver().query(
                    OliveContentProvider.ChatSpaceColumns.CONTENT_URI,
                    OliveContentProvider.ChatSpaceColumns.PROJECTIONS,
                    null, null,
                    OliveContentProvider.ChatSpaceColumns.ORDERBY
            );
        }

        public static ChatSpaceInfo getChatSpaceInfo(Context context, long spaceId) {
            ChatSpaceInfo info = null;
            if (spaceId >= 0) {
                Uri uri = Uri.withAppendedPath(OliveContentProvider.ChatSpaceColumns.CONTENT_URI, String.valueOf(spaceId));
                Cursor cursor = context.getContentResolver().query(uri, OliveContentProvider.ChatSpaceColumns.PROJECTIONS, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        info = new ChatSpaceInfo();
                        info.mId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns._ID));
                        info.mChatroomId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.CHATROOM_ID));
                        info.mParticipants = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.PARTICIPANTS));
                        info.mTitle = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.TITLE));
                        info.mType = OliveContentProvider.SpaceColumns.TYPE_CHAT;
                        info.mStarred = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.STARRED)) > 0;
                        info.mUnread = cursor.getInt(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.UNREAD));
                        info.mConversationId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.CONV_ID));
                        info.mSender = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.SENDER));
                        info.mSnippet = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.SNIPPET));
                        info.mLastUpdated = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.LAST_UPDATED));
                        info.mRecipientId = cursor.getLong(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.RECIPIENT_ID));
                        info.mPhonenumber = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.PHONENUMBER));
                        info.mDisplayname = cursor.getString(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.DISPLAYNAME));
                        //info.mPicture = cursor.getBlob(cursor.getColumnIndex(OliveContentProvider.ChatSpaceColumns.PICTURE));
                    }
                    cursor.close();
                }
            }

            return info;
        }

    };

    public static class ContactProviderHelper {
        public final static String DISPLAYNAME = ContactsContract.Data.DISPLAY_NAME;
        public final static String DATA = ContactsContract.Data.DATA1;
        public final static String SUBDATA = ContactsContract.Data.DATA4;

        public static Cursor getCursor(Context context) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    "(" + ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=?) AND " +
                            ContactsContract.Data.CONTACT_ID + " IN (SELECT " + ContactsContract.Contacts._ID + " FROM contacts WHERE " + ContactsContract.Contacts.HAS_PHONE_NUMBER + "!=0)",
                    new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE}, ContactsContract.Data.CONTACT_ID);
            c.moveToFirst();
            return c;
        }

        public static String getDisplaynameByEmail(Context context, String email) {
            String pszRet = null;

            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, },
                    ContactsContract.CommonDataKinds.Email.DATA + "=?",
                    new String[] { email, },
                    null);

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                pszRet = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                c.close();
            }

            return pszRet;
        }

        public static String getDisplaynameByPhone(Context context, String phone) {
            String pszRet = null;

            ContentResolver cr = context.getContentResolver();
              Cursor c = cr.query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phone),
                    new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, },
                    null, //ContactsContract.CommonDataKinds.Phone.DATA + "=?",
                    null, //new String[] { phone, },
                    null);

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                pszRet = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                c.close();
            }

            return pszRet;
        }

        public static String getDisplayname(Context context, String email, String phone) {
            String pszRet = (email != null && email.length() > 0) ? email : "UNKNOWN USER";

            String candidate01 = (phone != null && phone.length() > 0) ? getDisplaynameByPhone(context, phone) : null;
            String candidate02 = (email != null && email.length() > 0) ? getDisplaynameByEmail(context, email) : null;

            if (candidate01 != null) pszRet = candidate01;
            else
            if (candidate02 != null) pszRet = candidate02;

            return pszRet;
        }

    };

    public static class ButtonBoardHelper {

    };
}
