package app.com.willcallu.room_db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private ProfileRepository mRepository;

    private LiveData<List<Profile>> mAllProfiles;

    public ProfileViewModel(Application application) {
        super(application);
        mRepository = new ProfileRepository(application);
        mAllProfiles = mRepository.getAllProfiles();
    }

    public LiveData<List<Profile>> getmAllProfiles() {
        return mAllProfiles;
    }

    public void insert(Profile profile) {
        mRepository.insert(profile);
    }

    public void update(Profile profile) {
        mRepository.update(profile);
    }

    public void delete(Profile profile) {
        mRepository.delete(profile);
    }

    public List<Profile> extractProfileByDay(int currentDay) {
        return mRepository.extractProfileByDay(currentDay);
    }
}