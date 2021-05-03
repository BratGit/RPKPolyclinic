package com.rpkeffect.android.rpkpolyclinik.database;

public class UserDBSchema {
    public static final class UserTable {
        public static final String NAME = "users";

        public static final class Cols {
            public static final String ID = "id";
            public static final String EMAIL = "email";
            public static final String SURNAME = "surname";
            public static final String NAME = "name";
            public static final String PATRONYMIC = "patronymic";
            public static final String BIRTHDATE = "birth_date";
        }
    }

    public static final class OrderedServicesTable {
        public static final String NAME = "ordered_services";

        public static final class Cols {
            public static final String ID = "id";
            public static final String SERVICE_ID = "service_id";
            public static final String USER_ID = "user_id";
        }
    }
}
