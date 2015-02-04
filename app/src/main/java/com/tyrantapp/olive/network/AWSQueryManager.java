package com.tyrantapp.olive.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.kth.baasio.Baas;
import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.push.BaasioMessage;
import com.kth.baasio.entity.push.BaasioPayload;
import com.kth.baasio.entity.push.BaasioPush;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.kth.baasio.query.BaasioQuery.ORDER_BY;
import com.kth.baasio.response.BaasioResponse;
import com.tyrantapp.olive.configuration.BaasioConfig;
import com.tyrantapp.olive.configuration.Constants;
import com.tyrantapp.olive.helper.DatabaseHelper;
import com.tyrantapp.olive.helper.OliveHelper;
import com.tyrantapp.olive.provider.OliveContentProvider;
import com.tyrantapp.olive.type.ChatSpaceInfo;
import com.tyrantapp.olive.type.SpaceInfo;
import com.tyrantapp.olive.type.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

public class AWSQueryManager extends RESTApiManager {
    private static final String TAG = "NetworkHelper";

    private static final String SERVER_URL = "http://ec2-54-64-210-165.ap-northeast-1.compute.amazonaws.com";
    private static final String SERVER_PORT = "8080";
    private static final String SERVER_PATH_USER_SIGNUP = "/user/signup";
    private static final String SERVER_PATH_USER_SIGNIN = "/oauth2/access_token";
    private static final String SERVER_PATH_USER_LEAVE = "/user/leave";
    private static final String SERVER_PATH_USER_UPDATE = "/user/update";
    //private static final String SERVER_PATH_USER_INFO = "/user/info";       // input : friends_id : id list // 내 프로필 (id / photo / timestamp)
    private static final String SERVER_PATH_FRIENDS_ADD = "/friends/add";    // input : friends_id : id list
    private static final String SERVER_PATH_FRIENDS_DEL = "/friends/delete"; // input : friends_id : id list
    private static final String SERVER_PATH_FRIENDS_FIND = "/friends/find"; // input : emails / contacts
    private static final String SERVER_PATH_FRIENDS_PROFILE = "/friends/profile"; // input : friends_id : id lis
    private static final String SERVER_PATH_FRIENDS_LIST = "/friends/list";

    private static final String SERVER_PATH_ROOMS_CREATE = "/rooms/create";
    private static final String SERVER_PATH_ROOMS_LEAVE = "/rooms/leave"; // input : room_id:
    private static final String SERVER_PATH_ROOMS_INFO = "/rooms/info"; // input : room_id:
    private static final String SERVER_PATH_ROOMS_LIST = "/rooms/list"; // input : room_id:
    private static final String SERVER_PATH_MESSAGE_POST = "/message/post";
    private static final String SERVER_PATH_MESSAGE_NEW = "/message/new";
    private static final String SERVER_PATH_MESSAGE_READ = "/message/read";


    private static final String SERVER_URL_USER_SIGNUP = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_USER_SIGNUP;
    private static final String SERVER_URL_USER_SIGNIN = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_USER_SIGNIN;
    private static final String SERVER_URL_USER_LEAVE = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_USER_LEAVE;
    private static final String SERVER_URL_USER_UPDATE = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_USER_UPDATE;
    //private static final String SERVER_URL_USER_INFO    = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_USER_INFO;
    private static final String SERVER_URL_FRIENDS_ADD = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_FRIENDS_ADD;
    private static final String SERVER_URL_FRIENDS_DEL = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_FRIENDS_DEL;
    private static final String SERVER_URL_FRIENDS_FIND = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_FRIENDS_FIND;
    private static final String SERVER_URL_FRIENDS_PROFILE = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_FRIENDS_PROFILE;
    private static final String SERVER_URL_FRIENDS_LIST = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_FRIENDS_LIST;
    private static final String SERVER_URL_ROOMS_CREATE = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_ROOMS_CREATE;
    private static final String SERVER_URL_ROOMS_LEAVE = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_ROOMS_LEAVE;
    private static final String SERVER_URL_ROOMS_INFO = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_ROOMS_INFO;
    private static final String SERVER_URL_ROOMS_LIST = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_ROOMS_LIST;
    private static final String SERVER_URL_MESSAGE_POST = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_MESSAGE_POST;
    private static final String SERVER_URL_MESSAGE_NEW = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_MESSAGE_NEW;
    private static final String SERVER_URL_MESSAGE_READ = SERVER_URL + ":" + SERVER_PORT + SERVER_PATH_MESSAGE_READ;


    public static void initialize(Context context) {
        RESTApiManager.setContext(context);
        setInstance(new AWSQueryManager());
    }

    public int signUp(String username, String password) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (OliveHelper.isEmailAddress(getContext(), username)) {
            RestClient restClient = new RestClient(SERVER_URL_USER_SIGNUP);
            restClient.AddParam("username", username);
            restClient.AddParam("password", password);

            try {
                restClient.Execute(RestClient.POST);
                if (isValidNetwork(restClient)) {
                    HashMap<String, String> mapRecv = JSONParser(restClient.getResponse());
                    if (isSucceed(mapRecv)) {
                        eRet = OLIVE_SUCCESS;

                        // for push
                        BaasioUser user = null;
                        try {
                            user = BaasioUser.signUp(username/*username*/, username/*name*/, username/*email*/, BaasioConfig.BAASIO_PASSWORD);
                            android.util.Log.d(TAG, "Success to sign up [" + user.getUsername() + "]");
                        } catch (BaasioException e) {
                            e.printStackTrace();
                        }
                    } else {
                        switch (getErrorCode(mapRecv)) {
                            case 3:
                                eRet = OLIVE_FAIL_ALREADY_EXIST;
                                break;
                            default:
                                eRet = OLIVE_FAIL_UNKNOWN;
                                break;
                        }
                    }
                } else {
                    eRet = OLIVE_FAIL_BAD_NETWORK;
                }

            } catch (Exception e) {
                e.printStackTrace();
                android.util.Log.d(TAG, "Error!" + restClient.getErrorMessage() + " / " + restClient.getResponseCode());
            }
        } else {
            eRet = this.OLIVE_FAIL_INVALID_ID_PW;
        }

        return eRet;
    }

    public int signIn(String username, String password) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (OliveHelper.isEmailAddress(getContext(), username)) {
            RestClient restClient = new RestClient(SERVER_URL_USER_SIGNIN);
            restClient.AddParam("client_id", BaasioConfig.CLIENT_ID);
            restClient.AddParam("client_secret", BaasioConfig.CLIENT_SECRET);
            restClient.AddParam("grant_type", "password");
            restClient.AddParam("username", username);
            restClient.AddParam("password", password);
            restClient.AddParam("device_type", "android");
            restClient.AddParam("device_id", OliveHelper.getIMEINumber(getContext()));

            try {
                restClient.Execute(RestClient.POST);
                if (isValidNetwork(restClient)) {
                    HashMap<String, String> mapRecv = JSONParser(restClient.getResponse());
                    String error = mapRecv.get("error");
                    if (error == null) {
                        String accessToken = mapRecv.get("access_token");
                        String tokenType = mapRecv.get("token_type");
                        String expiresIn = mapRecv.get("expires_in");
                        String scope = mapRecv.get("scope");

                        UserProfile profile = new UserProfile();
                        profile.mUsername = username;
                        profile.mModified = System.currentTimeMillis();
                        DatabaseHelper.UserHelper.updateUserProfile(getContext(), profile);
                        DatabaseHelper.UserHelper.updateUserPassword(getContext(), password);
                        DatabaseHelper.UserHelper.updateAccessToken(getContext(), /*tokenType + " " + */accessToken);

                        android.util.Log.d(TAG, "access_token = " + accessToken + " / tokenType = " + tokenType + " / expiresIn = " + expiresIn + " / scope = " + scope);
                        eRet = OLIVE_SUCCESS;
                    } else {
                        android.util.Log.d(TAG, "Failed to sign in.");
                        eRet = OLIVE_FAIL_INVALID_ID_PW;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                android.util.Log.d(TAG, "Error!" + restClient.getErrorMessage() + " / " + restClient.getResponseCode());
            }

            // Baas for push
            if (eRet == OLIVE_SUCCESS) {
                BaasioUser user = null;
                try {
                    // connect to baasio server
                    user = BaasioUser.signIn(getContext(), username, BaasioConfig.BAASIO_PASSWORD);
                    android.util.Log.d(TAG, "Connect to Baasio for pushed [" + user.getUsername() + "]");

                    // push message for sign out other device
                    HashMap<String, String> mapParams = new HashMap<String, String>();
                    mapParams.put(OLIVE_PUSH_PROPERTY_PUSH_TYPE, OLIVE_PUSH_PROPERTY_PUSH_TYPE_SIGNOUT);
                    pushMessage(username, mapParams, null);

                    eRet = OLIVE_SUCCESS;
                } catch (BaasioException e) {
                    e.printStackTrace();
                }
            }
        } else {
            eRet = this.OLIVE_FAIL_INVALID_ID_PW;
        }

        return eRet;
    }

    public int signOut() {
        int eRet = OLIVE_SUCCESS;
        if (DatabaseHelper.UserHelper.removeUserProfile(getContext())) {
            BaasioUser.signOut(getContext());
        } else {
            eRet = OLIVE_FAIL_UNKNOWN;
        }
        return eRet;
    }

    public int leave() {
        int eRet = OLIVE_FAIL_UNKNOWN;

        HashMap<String, String> mapRecv = null; // = new HashMap<>();
        RestClient restClient = new RestClient(SERVER_URL_USER_LEAVE);
        eRet = sendByHttpWithAuthenticate(RestClient.POST, restClient, mapRecv);

        if (eRet == OLIVE_SUCCESS) {// for push
            BaasioUser user = null;
            try {
                user = Baas.io().getSignedInUser();
                user = user.unsubscribe(getContext());

                android.util.Log.d(TAG, "Success to unsubscribe.");

                eRet = OLIVE_SUCCESS;
            } catch (BaasioException e) {
                e.printStackTrace();
            }
        }

        return eRet;
    }

    public boolean verifyDevice() {
        boolean bRet = false;
        // Accesstoken이 유효한지, 혹시 타기기에 의해 로그아웃이 되진 않았는지 확인 필요.
        bRet = true;
        return bRet;
    }

    public boolean isAutoSignIn() {
        boolean bRet = false;

        String token = DatabaseHelper.UserHelper.UserPrivateHelper.getAccessToken(getContext());
        if (DatabaseHelper.UserHelper.UserPrivateHelper.getAccessToken(getContext()) != null) {
            bRet = true;
        }

        return bRet;
    }

    public HashMap<String, String> getUserProfile() {
        HashMap<String, String> mapRet = null;

        // 새로 로그인 했을 때, 서버로부터 사진 정도는 가져올 수 있어야 함.
        // 일단 friends/profile을 이용하여 가져옴
        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_FRIENDS_ADD);
            restClient.AddParam("friends_id", DatabaseHelper.UserHelper.getUserProfile(getContext()).mUsername);

            HashMap<String, String> mapRecv = new HashMap<String, String>();
            if (sendByHttp(RestClient.POST, restClient, mapRecv) == OLIVE_SUCCESS) {
                ArrayList<HashMap<String, String>> arrayData = JSONArrayParser(mapRecv.get(OLIVE_PROPERTY_USER_PROFILE.OLIVE_PROPERTY_PROFILE_LIST));
                if (arrayData.size() > 0) {
                    mapRet = new HashMap<String, String>();
                    mapRet.putAll(arrayData.get(0));
                }
            }
        }
        return mapRet;
    }

    // run when boot complete
    public int updateUserPhonenumber(String phonenumber) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_USER_UPDATE);
            restClient.AddParam("new_phone", phonenumber);

            eRet = sendByHttpWithAuthenticate(RestClient.POST, restClient, null);
        }
        return eRet;
    }

    public int updateUserPicture(Bitmap picture) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_USER_UPDATE);
            //restClient.AddParam("new_picture", DatabaseHelper.UserHelper.getUserProfile(getContext()).mPicture);

            eRet = sendByHttpWithAuthenticate(RestClient.POST, restClient, null);
        }
        return eRet;
    }

    public int changePassword(String oldPassword, String newPassword) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_USER_UPDATE);
            restClient.AddParam("new_password", newPassword);

            HashMap<String, String> mapRecv = null;
            eRet = sendByHttpWithAuthenticate(RestClient.POST, restClient, mapRecv);
        }
        return eRet;
    }

    public int addFriends(String[] usernames) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_FRIENDS_ADD);
            String friends = new String();
            for (String username : usernames) friends += username + ",";
            if (friends.endsWith(",")) friends = friends.substring(0, friends.length() - 1);
            restClient.AddParam("friends_id", friends);

            eRet = sendByHttp(RestClient.POST, restClient, null);
        }
        return eRet;
    }

    public int removeFriends(String[] usernames) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_FRIENDS_DEL);
            String friends = new String();
            for (String username : usernames) friends += username + ",";
            if (friends.endsWith(",")) friends = friends.substring(0, friends.length() - 1);
            restClient.AddParam("friends_id", friends);

            eRet = sendByHttp(RestClient.POST, restClient, null);
        }
        return eRet;
    }

    public ArrayList<HashMap<String, String>> findFriends(String[] emails, String[] contacts) {
        ArrayList<HashMap<String, String>> arrRet = null;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_FRIENDS_FIND);
            String emailValues = new String();
            String contValues = new String();
            if (emails != null) {
                for (String email : emails) emailValues += email + ",";
                if (emailValues.endsWith(","))
                    emailValues = emailValues.substring(0, emailValues.length() - 1);
            }
            if (contacts != null) {
                for (String contact : contacts) contValues += contact + ",";
                if (contValues.endsWith(","))
                    contValues = contValues.substring(0, contValues.length() - 1);
            }

            if (!emailValues.isEmpty()) {
                restClient.AddParam("emails", emailValues);
            }

            if (!contValues.isEmpty()) {
                restClient.AddParam("contacts", contValues);
            }

            ArrayList<HashMap<String, String>> arrFriends = null;
            HashMap<String, String> mapRecv = new HashMap<String, String>();
            if (sendByHttp(RestClient.POST, restClient, mapRecv) == OLIVE_SUCCESS) {
                arrRet = new ArrayList<HashMap<String, String>>();
                arrFriends = new ArrayList<HashMap<String, String>>();
                arrFriends.addAll(JSONArrayParser(mapRecv.get(OLIVE_PROPERTY_FRIENDS_FIND.OLIVE_PROPERTY_EMAILS_LIST)));
                arrFriends.addAll(JSONArrayParser(mapRecv.get(OLIVE_PROPERTY_FRIENDS_FIND.OLIVE_PROPERTY_CONTACTS_LIST)));
            }

            HashSet<String> setReduceDuplicate = new HashSet<String>();
            for (HashMap<String, String> friend : arrFriends) {
                String username = friend.get(OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME);
                if (!setReduceDuplicate.contains(username)) {
                    setReduceDuplicate.add(username);
                    arrRet.add(friend);
                }
            }
        }
        return arrRet;
    }

    public ArrayList<HashMap<String, String>> getFriendsList() { // <- 동작 안함 (서버의 친구 리스트 (아이디/휴대전화/최종수정시간 리스트)
        ArrayList<HashMap<String, String>> arrRet = null;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_FRIENDS_LIST);
            HashMap<String, String> mapRecv = new HashMap<String, String>();
            if (sendByHttp(RestClient.POST, restClient, mapRecv) == OLIVE_SUCCESS) {
                arrRet = JSONArrayParser(mapRecv.get(OLIVE_PROPERTY_FRIENDS_PROFILE.OLIVE_PROPERTY_PROFILE_LIST));
            }
        }

        return arrRet;
    }

    public ArrayList<HashMap<String, String>>  getFriendsProfile(String[] usernames) { // <- 동작 안함 (친구의 아이디/ 휴대전화/ 사진/ 최종수정시간)
        ArrayList<HashMap<String, String>> arrRet = null;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_FRIENDS_PROFILE);
            String values = new String();
            for (String username : usernames) values += username + ",";
            if (values.endsWith(",")) values = values.substring(0, values.length() - 1);

            restClient.AddParam("friends_id", values);

            HashMap<String, String> mapRecv = new HashMap<String, String>();
            if (sendByHttp(RestClient.POST, restClient, mapRecv) == OLIVE_SUCCESS) {
                arrRet = JSONArrayParser(mapRecv.get(OLIVE_PROPERTY_FRIENDS_PROFILE.OLIVE_PROPERTY_PROFILE_LIST));
            }
        }

        return arrRet;
    }

    public HashMap<String, String> createRoom(String[] usernames) {
        HashMap<String, String> mapRet = null;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_ROOMS_CREATE);
            String values = new String();
            for (String username : usernames) values += username + ",";
            if (values.endsWith(",")) values = values.substring(0, values.length() - 1);
            restClient.AddParam("friends_id", values);

            mapRet = new HashMap<String, String>();
            int eRet = sendByHttp(RestClient.POST, restClient, mapRet);
            if (eRet == OLIVE_SUCCESS) {
                // Push!
                for (String friend : usernames) {
                    HashMap<String, String> mapParams = new HashMap<String, String>();
                    mapParams.put(OLIVE_PUSH_PROPERTY_PUSH_TYPE, OLIVE_PUSH_PROPERTY_PUSH_TYPE_CREATE);
                    mapParams.put(OLIVE_PUSH_PROPERTY_ROOM_ID, String.valueOf(mapRet.get(OLIVE_PROPERTY_ROOM_BUNDLE_ITEM.OLIVE_PROPERTY_ROOM_ID)));
                    pushMessage(friend, mapParams, null);
                }
            } else
            if (eRet == OLIVE_FAIL_BAD_NETWORK) {
                mapRet.clear();
                mapRet = null;
                //lRet = Long.valueOf(mapRecv.get("data::room::id"));
            }

        }
        return mapRet;
    }

    public int leaveRoom(long idRoom) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_ROOMS_LEAVE);
            restClient.AddParam("room_id", String.valueOf(idRoom));

            eRet = sendByHttp(RestClient.POST, restClient, null);
            if (eRet == OLIVE_SUCCESS) {
                // Push!
                UserProfile profile = DatabaseHelper.UserHelper.getUserProfile(getContext());
                SpaceInfo info = DatabaseHelper.SpaceHelper.getSpaceInfo(getContext(), DatabaseHelper.SpaceHelper.getSpaceId(getContext(), idRoom));
                if (info != null) {
                    String[] participants = info.mParticipants.split(",");
                    for (String friend : participants) {
                        if (profile.mUsername.equals(friend)) continue;

                        HashMap<String, String> mapParams = new HashMap<String, String>();
                        mapParams.put(OLIVE_PUSH_PROPERTY_PUSH_TYPE, OLIVE_PUSH_PROPERTY_PUSH_TYPE_LEAVE);
                        mapParams.put(OLIVE_PUSH_PROPERTY_ROOM_ID, String.valueOf(idRoom));
                        pushMessage(friend, mapParams, null);
                    }
                }
            }
        }
        return eRet;
    }

    public ArrayList<HashMap<String, String>> getRoomsList() {
        ArrayList<HashMap<String, String>> arrRet = null;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_ROOMS_LIST);

            HashMap<String, String> mapRecv = new HashMap<String, String>();
            int eRet = sendByHttp(RestClient.POST, restClient, mapRecv);
            if (eRet == OLIVE_SUCCESS) {
                arrRet = JSONArrayParser(mapRecv.get(OLIVE_PROPERTY_ROOMS_LIST.OLIVE_PROPERTY_ROOM_LIST));
            } else
            if (eRet == OLIVE_FAIL_BAD_NETWORK) {
                arrRet.clear();
                arrRet = null;
            }
        }

        return arrRet;
    }

    public HashMap<String, String> getRoomInfo(long idRoom, ArrayList<HashMap<String, String>> participants) {
        HashMap<String, String> mapRet = null;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_ROOMS_INFO);
            restClient.AddParam("room_id", String.valueOf(idRoom));

            mapRet = new HashMap<String, String>();
            int eRet = sendByHttp(RestClient.POST, restClient, mapRet);
            if (eRet == OLIVE_SUCCESS) {
                if (participants != null) {
                    participants.addAll(JSONArrayParser(mapRet.get(OLIVE_PROPERTY_ROOM_INFO.OLIVE_PROPERTY_ROOM_ATTENDANTS_LIST)));
                }
            } else
            if (eRet == OLIVE_FAIL_BAD_NETWORK) {
                mapRet.clear();
                mapRet = null;
            }
        }
        return mapRet;
    }

    public HashMap<String, String> sendMessage(long idRoom, String author, String mimetype, String context) {
        HashMap<String, String> mapRet = null;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_MESSAGE_POST);
            restClient.AddParam("room_id", String.valueOf(idRoom));
            restClient.AddParam("msg_type", String.valueOf(OliveHelper.convertMimetype(mimetype)));
            restClient.AddParam("contents", context);

            mapRet = new HashMap<String, String>();
            int eRet = sendByHttp(RestClient.POST, restClient, mapRet);
            if (eRet == OLIVE_SUCCESS) {
                // Push!
                UserProfile profile = DatabaseHelper.UserHelper.getUserProfile(getContext());
                SpaceInfo info = DatabaseHelper.SpaceHelper.getSpaceInfo(getContext(), DatabaseHelper.SpaceHelper.getSpaceId(getContext(), idRoom));
                StringTokenizer tokenizer = new StringTokenizer(info.mParticipants, ";,");
                while (tokenizer.hasMoreTokens()) {
                    String participant = tokenizer.nextToken();

                    if (!profile.mUsername.equals(participant)) {
                        HashMap<String, String> mapParams = new HashMap<String, String>();
                        mapParams.put(OLIVE_PUSH_PROPERTY_PUSH_TYPE, OLIVE_PUSH_PROPERTY_PUSH_TYPE_POST);
                        mapParams.put(OLIVE_PUSH_PROPERTY_SENDER, profile.mUsername);
                        mapParams.put(OLIVE_PUSH_PROPERTY_ROOM_ID, String.valueOf(idRoom));
                        pushMessage(participant, mapParams, context);
                    }
                }
            } else
            if (eRet == OLIVE_FAIL_BAD_NETWORK) {
                mapRet.clear();
                mapRet = null;
            }
        }
        return mapRet;
    }

    public ArrayList<HashMap<String, String>> receiveMessages(long idRoom) {
        ArrayList<HashMap<String, String>> arrRet = null;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_MESSAGE_NEW);
            if (idRoom >= 0) {
                restClient.AddParam("room_id", String.valueOf(idRoom));
            }

            HashMap<String, String> mapRecv = new HashMap<String, String>();
            if (sendByHttp(RestClient.POST, restClient, mapRecv) == OLIVE_SUCCESS) {
                arrRet = JSONArrayParser(mapRecv.get(OLIVE_PROPERTY_MESSAGES_LIST.OLIVE_PROPERTY_MESSAGE_LIST));
            }
        }
        return arrRet;
    }

    public int readMessages(long idRoom) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (isAutoSignIn()) {
            RestClient restClient = new RestClient(SERVER_URL_MESSAGE_READ);
            restClient.AddParam("room_id", String.valueOf(idRoom));

            ChatSpaceInfo info = DatabaseHelper.ChatSpaceHelper.getChatSpaceInfo(getContext(), DatabaseHelper.SpaceHelper.getSpaceId(getContext(), idRoom));
            if (info != null) {
                long idMessage = DatabaseHelper.ConversationHelper.getMessageId(getContext(), info.mConversationId);
                restClient.AddParam("last_msg_id", Long.valueOf(idMessage).toString());
            }

            HashMap<String, String> mapRecv = null;
            eRet = sendByHttp(RestClient.POST, restClient, mapRecv);
        }
        return eRet;
    }

    private int sendByHttpWithAuthenticate(int execution, RestClient client, HashMap<String, String> mapRecv) {
        return sendByHttp(execution, client, true, mapRecv);
    }

    private int sendByHttp(int execution, RestClient client, HashMap<String, String> mapRecv) {
        return sendByHttp(execution, client, false, mapRecv);
    }

    private int sendByHttp(int execution, RestClient client, boolean withPassword, HashMap<String, String> mapRecv) {
        int eRet = OLIVE_FAIL_UNKNOWN;

        if (isAutoSignIn()) {
            UserProfile profile = DatabaseHelper.UserHelper.getUserProfile(getContext());
            client.AddHeader("Authorization", DatabaseHelper.UserHelper.UserPrivateHelper.getAccessToken(getContext()));
            //client.AddParam("client_id", BaasioConfig.CLIENT_ID);
            //client.AddParam("client_secret", BaasioConfig.CLIENT_SECRET);
            //client.AddParam("username", profile.mUsername);
            if (withPassword) client.AddParam("password", DatabaseHelper.UserHelper.UserPrivateHelper.getUserPassword(getContext()));

            try {
                client.Execute(execution);
                if (isValidNetwork(client)) {
                    if (mapRecv == null) mapRecv = new HashMap<String, String>();
                    mapRecv.putAll(JSONParser(client.getResponse()));
                    if (isSucceed(mapRecv)) {
                        eRet = OLIVE_SUCCESS;
                    }
                } else {
                    eRet = OLIVE_FAIL_BAD_NETWORK;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            eRet = OLIVE_FAIL_NO_SIGNIN;
        }

        return eRet;
    }

    private boolean pushMessage(String recipientId, HashMap<String, String> params, String msg) {
        boolean bRet = false;
        // Push!
        try {
            BaasioQuery mQuery = new BaasioQuery();
            mQuery.setType(BaasioUser.ENTITY_TYPE + "/" + recipientId);
            mQuery.setOrderBy(BaasioBaseEntity.PROPERTY_MODIFIED, ORDER_BY.DESCENDING);

            BaasioResponse reponse = mQuery.query();
            BaasioUser recipient = BaasioBaseEntity.toType(reponse.getFirstEntity(), BaasioUser.class);

            BaasioPayload payload = new BaasioPayload();
            payload.setAlert(msg);      // 전송할 메시지
            if (params != null) {
                for (String key : params.keySet()) {
                    payload.setProperty(key, params.get(key));
                }
            }
            payload.setSound("homerun.caf");    // iOS APNS의 sound
            payload.setBadge(1);                // iOS APNS badge 갯수

            BaasioMessage message = new BaasioMessage();
            message.setPayload(payload);
            message.setTarget(BaasioMessage.TARGET_TYPE_USER);  // 회원 개별 발송
            message.setPlatform(BaasioMessage.PLATFORM_FLAG_TYPE_GCM);
            message.setTo(recipient.getUuid().toString());

            BaasioPush.sendPush(message);

            android.util.Log.d(TAG, "Pushed to " + recipientId);
            bRet = true;
        } catch (BaasioException e) {
            e.printStackTrace();
        }
        return bRet;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private static function
    private static boolean isValidNetwork(RestClient client) {
        android.util.Log.d(TAG, "Error = " + client.getErrorMessage() + " / Response = " + client.getResponseCode());
        return client.getResponseCode() == 200;
    }

    private static boolean isSucceed(HashMap<String, String> mapRecv) {
        return "true".equals(mapRecv.get("success"));
    }

    private static int getErrorCode(HashMap<String, String> mapRecv) {
        if (!isSucceed(mapRecv)) {
            android.util.Log.d(TAG, "Error Code = " + mapRecv.get("error_code") + " / message = " + mapRecv.get("message") + " / data = " + mapRecv.get("data"));
            return Integer.parseInt(mapRecv.get(OLIVE_PROPERTY_ERROR.OLIVE_PROPERTY_ERROR_CODE));
        }
        android.util.Log.d(TAG, "Received data is not occured error.");
        return -1;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // static function associated with database or web
    public static boolean verifySpace(Context context, long idSpace) {
        boolean bRet = false;
        SpaceInfo info = DatabaseHelper.SpaceHelper.getSpaceInfo(context, idSpace);
        if (info != null) {
            RESTApiManager helper = RESTApiManager.getInstance();
            long idRoom = info.mChatroomId;
            HashMap<String, String> mapInfo = helper.getRoomInfo(idRoom, null);
            if (mapInfo != null) {
                bRet = isSucceed(mapInfo);
            }
        }
        return bRet;
    }

    public static long obtainSpaceIdByRoomId(Context context, long idRoom) {
        long idSpace = DatabaseHelper.SpaceHelper.getSpaceId(context, idRoom);
        if (idSpace < 0) {
            // 서버에는 있고 로컬에만 없는 경우
            RESTApiManager helper = RESTApiManager.getInstance();
            UserProfile profile = DatabaseHelper.UserHelper.getUserProfile(context);
            ArrayList<HashMap<String, String>> listParticipants = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> mapRoomInfo = helper.getRoomInfo(idRoom, listParticipants);

            SpaceInfo info = new SpaceInfo();
            info.mChatroomId = idRoom;
            info.mTitle = new String();
            info.mParticipants = new String();
            for (HashMap<String, String> participant : listParticipants) {
                String participantName = participant.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME);
                if (!profile.mUsername.equals(participantName)) {
                    info.mTitle += participant.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME) + ",";
                }
                info.mParticipants += participant.get(RESTApiManager.OLIVE_PROPERTY_PROFILE_LIST_ITEM.OLIVE_PROPERTY_USERNAME) + ",";
            }
            if (info.mTitle.endsWith(",")) info.mTitle = info.mTitle.substring(0, info.mTitle.length() - 1);
            if (info.mParticipants.endsWith(",")) info.mParticipants = info.mParticipants.substring(0, info.mParticipants.length() - 1);
            info.mType = OliveContentProvider.SpaceColumns.TYPE_CHAT;
            info.mStarred = false;
            idSpace = DatabaseHelper.SpaceHelper.addSpace(context, info);
        } else
        if (!verifySpace(context, idSpace)) {
            // 서버에는 없고 로컬에만 있는 경우 (안해도 될것 같지만... 혹시 모르니... 하지만 데이터 소모량이 많다면 삭제해도 무방)
            DatabaseHelper.SpaceHelper.removeSpace(context, idSpace);
            idSpace = -1;
        }
        return idSpace;
    }
}
