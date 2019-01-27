package app.com.willcallu.room_db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

@Database(entities = {Profile.class}, version = 1)
public abstract class ProfileRoomDatabase extends RoomDatabase {
    private static ProfileRoomDatabase INSTANCE;

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    public static ProfileRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ProfileRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ProfileRoomDatabase.class, "profile_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ProfileDao profileDao();

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final ProfileDao mDao;

        PopulateDbAsync(ProfileRoomDatabase db) {
            mDao = db.profileDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
//            mDao.deleteAll();
//            Profile profile = new Profile("Hello", "Hello", "Hello",
//                    "Hello", "Hello", "Hello");
//            mDao.insert(profile);
            return null;
        }
    }
}