package com.kemar.olam.utility;

import io.realm.Realm;
import io.realm.RealmConfiguration;



public class RealmHelper {

    public static Realm getRealmInstance() {
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .allowWritesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();
        return Realm.getInstance(config);
    }

}
