package app.com.willcallu.room_db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProfileRepository {

    private ProfileDao mProfileDao;
    private LiveData<List<Profile>> mAllProfiles;

    ProfileRepository(Application application) {
        ProfileRoomDatabase db = ProfileRoomDatabase.getDatabase(application);
        mProfileDao = db.profileDao();
        mAllProfiles = mProfileDao.getAllProfiles();
    }

    LiveData<List<Profile>> getAllProfiles() {
        return mAllProfiles;
    }


    public void insert(Profile profile) {
        new insertAsyncTask(mProfileDao).execute(profile);
    }

    public void update(Profile profile) {
        new updateAsyncTask(mProfileDao).execute(profile);
    }

    public void delete(Profile profile) {
        new deleteAsyncTask(mProfileDao).execute(profile);
    }

    public List<Profile> extractProfileByDay(int currentDay) {
        try {
            return new extractProfileByDayAsyncTask(mProfileDao, currentDay).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class insertAsyncTask extends AsyncTask<Profile, Void, Void> {

        private ProfileDao mAsyncTaskDao;

        insertAsyncTask(ProfileDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Profile... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<Profile, Void, Void> {

        private ProfileDao mAsyncTaskDao;

        updateAsyncTask(ProfileDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Profile... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Profile, Void, Void> {

        private ProfileDao mAsyncTaskDao;

        deleteAsyncTask(ProfileDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Profile... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class extractProfileByDayAsyncTask extends AsyncTask<Profile, String, List<Profile>> {

        private ProfileDao mAsyncTaskDao;
        private int currentDay;

        extractProfileByDayAsyncTask(ProfileDao dao, int day) {
            mAsyncTaskDao = dao;
            currentDay = day;
        }

        @Override
        protected List<Profile> doInBackground(Profile... profiles) {
            return mAsyncTaskDao.extractProfileByDay(String.valueOf(currentDay));
        }
    }
}