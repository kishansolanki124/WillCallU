package app.com.willcallu.room_db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ProfileDao {

    @Insert
    void insert(Profile profile);

    @Update
    void update(Profile profile);

    @Delete
    void delete(Profile profile);

    @Query("SELECT * from profile_table WHERE week_days LIKE  '%' || :currentDay || '%' ORDER BY id DESC")
    List<Profile> extractProfileByDay(String currentDay);

    @Query("SELECT * from profile_table ORDER BY name ASC")
    LiveData<List<Profile>> getAllProfiles();
}