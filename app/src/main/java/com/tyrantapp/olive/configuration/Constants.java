package com.tyrantapp.olive.configuration;

/**
 * Created by onetop21 on 15. 2. 3.
 */
public class Constants {
    public static class Intent {
        public static final String EXTRA_ROOM_ID  = "room_id";
        public static final String EXTRA_SPACE_ID = "space_id";

        public static final String EXTRA_AUTHOR   = "author";
        public static final String EXTRA_MIMETYPE = "mimetype";
        public static final String EXTRA_CONTEXT  = "context";

        public static final String EXTRA_SECTION_NUMBER = "section_number";
        public static final String EXTRA_SECTION_INDEX = "section_index";
    }

    public static class Notification {
        public static final String SHARED_NOTIFICATION_ROOM_ID = "noti_room_id";
    }

    public static class Conversation {
        public static final String SHARED_SPACE_INFO = "space_info";
    }

}
