package com.tyrantapp.olive.configuration;

/**
 * Created by onetop21 on 15. 2. 3.
 */
public class Constants {
    public static class Intent {
        public static final String EXTRA_ROOM_ID    = "room_id";
        public static final String EXTRA_SPACE_ID   = "space_id";

        public static final String EXTRA_AUTHOR     = "author";
        public static final String EXTRA_MIMETYPE   = "mimetype";
        public static final String EXTRA_CONTEXT    = "context";

        public static final String EXTRA_BUTTON_ID  = "button_id";
    }

    public static class Notification {
        public static final String SHARED_NOTIFICATION_ROOM_ID = "noti_room_id";
    }

    public static class Conversation {
        public static final String SHARED_SPACE_INFO = "space_info";
    }

    public static class System {
        public static final String  DEVICE_ID_KEY = "device_id";
    }

    public static class Configuration {
        public static final String  GOOGLE_SENDER_ID = "1028073397160";  // Place here your Google project id
        public static final String  CLIENT_ID = "2472266904a9452d7e30";
        public static final String  CLIENT_SECRET = "26e82a650178634867d59a54404913bcbc175265";
        public static final int     NOTIFICATION_ID = 10000;
        public static final int     MAX_IMAGE_RESOLUTION = 1920;
    }

}
