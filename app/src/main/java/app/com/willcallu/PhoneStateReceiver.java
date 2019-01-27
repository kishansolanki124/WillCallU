package app.com.willcallu;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import app.com.willcallu.room_db.Profile;
import app.com.willcallu.room_db.ProfileDao;
import app.com.willcallu.room_db.ProfileRoomDatabase;
import kotlin.text.Regex;

public class PhoneStateReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    //private static String savedNumber;
    private static boolean ringing = false;
    private static boolean call_received = false;
    ArrayList<String> selectedContacts = new ArrayList<>();
    ArrayList secondsArrayList = new ArrayList<Double>();
    ArrayList<Profile> foundedTimeArrayList = new ArrayList<>();
    private ProfileDao mProfileDao;
    private int sentSmsCounter = 0;

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            //savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            //28 is pie OS
            if ((android.os.Build.VERSION.SDK_INT >= 28) && (number == null || number.length() == 0))
                return;

            int state = 0;
            if (stateStr != null) {
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCallDetails(context);
                            //selectedContacts.add("+918866714328");
                            //sendSMS(context);
                        }
                    }, 3000);

                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
            }
            onCallStateChanged(context, state, number);
        }
    }

    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                ringing = true;
                //savedNumber = number;
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                call_received = true;
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {

                }
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                if (ringing && !call_received) {

                    ringing = false;
                }
//                else {
//
//                }
                call_received = false;

//                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
//                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
//                    //Ring but no pickup-  a miss
//                    long diffInMs = new Date().getTime() - callStartTime.getTime();
//                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
//                    if (diffInSec > 28) {
//                        //phone cut automatic
//
//                    } else {

//                    }
//
//
//                } else if (isIncoming) {
//
//                } else {
//
//                }
                break;
        }
        lastState = state;
    }

    private void getCallDetails(Context context) {

        StringBuilder sb = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null,
                CallLog.Calls.DATE + " DESC limit 1;");
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :");
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number); // mobile number
            String callType = managedCursor.getString(type); // call type
            String callDate = managedCursor.getString(date); // call date
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String dir = "";
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("Call duration in sec :--- " + callDuration + " " +
                    "\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir
                    + " \nCall Date:--- " + callDayTime + " \nCall duration in sec :--- " + callDuration);
            sb.append("\n----------------------------------");

            if (dir.matches("MISSED")) {
                if (WillCallUApplication.Companion.getOnlyContactsBoolEnable()) {
                    if (checkContactIsSaved(context, phNumber) != null &&
                            checkContactIsSaved(context, phNumber).length() > 0) {
                        printDifference(context, callDayTime, phNumber, new Date());
                    }
                } else {
                    printDifference(context, callDayTime, phNumber, new Date());
                }
            }
        }
        managedCursor.close();
        Log.e("Agil value --- ", sb.toString());
        sb.toString();
    }

    void sendSMS(String phoneNumber, String message, Context context) {
        selectedContacts = new ArrayList<>();
        selectedContacts.add(phoneNumber);

        sentSmsCounter = 0;

        SmsManager sms = SmsManager.getDefault();

        ArrayList<String> messageParts = sms.divideMessage(message + "\n" + context.getString(R.string.sms_sent_via));

        int partsCount = messageParts.size();

        ArrayList<PendingIntent> sentPendings = new ArrayList<>(partsCount);

        ArrayList<PendingIntent> deliveredPendings = new ArrayList<>(partsCount);

        for (int i = 0; i < partsCount; i++) {
            /* Adding Sent PendingIntent For Message Part */
            PendingIntent sentPending = PendingIntent.getBroadcast(context, 0, new Intent("SENT"), 0);

            context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            sentSmsCounter += 1;
                            if (selectedContacts.size() == sentSmsCounter) {
                                //all smss are sent
                            }

                            break;

                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Log.e("smserror", "Not Sent: Generic failure.");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Log.e("smserror", "Not Sent: No service (possibly, no SIM-card).");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Log.e("smserror", "Not Sent: Null PDU.");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Log.e("smserror", "Not Sent: Radio off (possibly, Airplane mode enabled in Settings).");
                            break;
                    }
                }
            }, new IntentFilter("SENT"));

            sentPendings.add(sentPending);

            /* Adding Delivered PendingIntent For Message Part */

            PendingIntent deliveredPending = PendingIntent.getBroadcast(
                    context, 0, new Intent("DELIVERED"), 0);

            context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Log.e("sms", "SMS Delivered.");
                            break;
                        case Activity.RESULT_CANCELED:
                            Log.e("sms", "Not Delivered: Canceled.");
                            break;
                    }
                }
            }, new IntentFilter("DELIVERED"));

            deliveredPendings.add(deliveredPending);
        }

        for (int i = 0; i < selectedContacts.size(); i++) {
            //smsManager.sendTextMessage(selectedContacts.get(i), null, message, null, null);
            sms.sendMultipartTextMessage(selectedContacts.get(i), null, messageParts, sentPendings, deliveredPendings);
        }
    }

    /**
     * 8.9.18.10.49PM
     * used to compare two times difference, used to check missed call duration , which will used
     * to check if the duration's seconds are less or greater than user's decided missed call duration
     *
     * @param startDate call arrived time
     * @param endDate   current time
     */
    public void printDifference(Context context, Date startDate, String phoneNumber, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        System.out.printf("%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);

        if (elapsedSeconds > WillCallUApplication.Companion.getRingTime()) {

//            JobScheduler jobScheduler;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                jobScheduler = (JobScheduler) context.getApplicationContext()
//                        .getSystemService(JOB_SCHEDULER_SERVICE);
//
//                ComponentName componentName = new ComponentName(context,
//                        DbUpdateJobService.class);
//
//                JobInfo jobInfo = new JobInfo.Builder(1, componentName)
//                        .setPeriodic(86400000).setRequiredNetworkType(
//                                JobInfo.NETWORK_TYPE_NONE)
//                        .setPersisted(true).build();
//                if (jobScheduler != null) {
//                    jobScheduler.schedule(jobInfo);
//                }
//            }

            checkForProfile(phoneNumber, context);
        }
    }

    private void checkForProfile(String phoneNumber, Context context) {

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        List<Profile> allPRofiles = extractProfileByDay(context, dayOfWeek - 1);

        secondsArrayList = new ArrayList<Double>();
        foundedTimeArrayList = new ArrayList<Profile>();

        if (allPRofiles != null) {
            for (int i = 0; i < allPRofiles.size(); i++) {
                Profile prof = allPRofiles.get(i);
                if (isTimeBetweenTwoTime(prof.mStartTime, prof.mEndTime,
                        new SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH).format(new Date()))) {
                    findTimeDifference(prof, prof.mStartTime, prof.mEndTime);
                }
            }

            if (secondsArrayList.size() > 0) {
                Log.d("lowest is ", Collections.min(secondsArrayList).toString());
                int minIndex = secondsArrayList.indexOf(Collections.min(secondsArrayList));
                Log.d("lowest minIndex ", String.valueOf(minIndex));
                Log.d("profile name ", foundedTimeArrayList.get(minIndex).mName);


                sendSMS(phoneNumber, foundedTimeArrayList.get(minIndex).mMessage, context);
            }
        }
    }

    public List<Profile> extractProfileByDay(Context context, int currentDay) {
        try {
            ProfileRoomDatabase db = ProfileRoomDatabase.getDatabase(context);
            mProfileDao = db.profileDao();
            return new extractProfileByDayAsyncTask(mProfileDao, currentDay).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    Boolean isTimeBetweenTwoTime(String initialTime, String finalTime, String currentTimestr) {

        Regex reg = new Regex("^([0-1][0-9]|2[0-3]):([0-5][0-9])$");

        if (initialTime.matches(String.valueOf(reg)) && finalTime.matches(String.valueOf(reg))
                && currentTimestr.matches(String.valueOf(reg))) {
            boolean valid = false;
            // Start Time
            Date startTime = null;
            try {
                startTime = new SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH)
                        .parse(initialTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startTime);

            // Current Time
            Date currentTime = null;
            try {
                currentTime = new SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH)
                        .parse(currentTimestr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentTime);

            // End Time
            Date endTime = null;
            try {
                endTime = new SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH)
                        .parse(finalTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endTime);

            //
            if (currentTime.compareTo(endTime) < 0) {

                currentCalendar.add(Calendar.DATE, 1);
                currentTime = currentCalendar.getTime();

            }

            if (startTime.compareTo(endTime) < 0) {

                startCalendar.add(Calendar.DATE, 1);
                startTime = startCalendar.getTime();

            }
            //
            if (currentTime.before(startTime)) {

                System.out.println(" Time is Lesser ");

                valid = false;
            } else {

                if (currentTime.after(endTime)) {
                    endCalendar.add(Calendar.DATE, 1);
                    endTime = endCalendar.getTime();

                }

                System.out.println("Comparing , Start Time /n " + startTime);
                System.out.println("Comparing , End Time /n " + endTime);
                System.out
                        .println("Comparing , Current Time /n " + currentTime);

                if (currentTime.before(endTime)) {
                    System.out.println("RESULT, Time lies b/w");
                    valid = true;
                } else {
                    valid = false;
                    System.out.println("RESULT, Time does not lies b/w");
                }
            }
            return valid;
        } else {
            return false;
        }

//        if (initialTime.matches(reg) && finalTime.matches(reg) &&
//                currentTime.matches(reg)) {
//            val valid: Boolean
//            //Start Time
//            //all times are from java.util.Date
//            val inTime = SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH).parse(initialTime)
//            val calendar1 = Calendar.getInstance();
//            calendar1.setTime(inTime);
//
//            //Current Time
//            val checkTime = SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH).parse(currentTime);
//            val calendar3 = Calendar.getInstance();
//            calendar3.setTime(checkTime);
//
//            //End Time
//            val finTime = SimpleDateFormat(AppConstants.FORMAT_HH_mm, Locale.ENGLISH).parse(finalTime);
//            val calendar2 = Calendar.getInstance();
//            calendar2.setTime(finTime);
//
//            if (finalTime.compareTo(initialTime) < 0) {
//                calendar2.add(Calendar.DATE, 1);
//                calendar3.add(Calendar.DATE, 1);
//            }
//
//            val actualTime = calendar3.getTime();
//            if ((actualTime.after(calendar1.getTime()) ||
//                            actualTime.compareTo(calendar1.getTime()) == 0) &&
//                    actualTime.before(calendar2.getTime())) {
//                valid = true
//                return valid
//            } else {
//                return false
//            }
//        } else {
//            return false
//        }
    }

    private void findTimeDifference(Profile mProfileViewModel, String startTime, String endTime) {

        int start = Integer.parseInt(startTime.split(":")[0]);
        int end = Integer.parseInt(endTime.split(":")[0]);
        int startMinute = Integer.parseInt(startTime.split(":")[1]);
        int endMinute = Integer.parseInt(endTime.split(":")[1]);

        int totalhours = 0;
        int totalminutes = 0;

        if (start == end) {
            if (endMinute - startMinute > 0) {
                totalhours = end - start;
                if (endMinute - startMinute >= 0) {
                    totalminutes = endMinute - startMinute;
                } else {
                    totalhours = totalhours - 1;
                    totalminutes = (endMinute + 60) - startMinute;
                }
            } else {
                totalhours = (24 - start) + end;

                if (endMinute - startMinute >= 0) {
                    totalminutes = endMinute - startMinute;
                } else {
                    totalhours = totalhours - 1;
                    totalminutes = (endMinute + 60) - startMinute;
                }
            }
        } else if (start < end) {
            totalhours = end - start;
            if (endMinute - startMinute >= 0) {
                totalminutes = endMinute - startMinute;
            } else {
                totalhours = totalhours - 1;
                totalminutes = (endMinute + 60) - startMinute;
            }
        } else {
            totalhours = (24 - start) + end;
            if (endMinute - startMinute >= 0) {
                totalminutes = endMinute - startMinute;
            } else {
                totalhours = totalhours - 1;
                totalminutes = (endMinute + 60) - startMinute;
            }
        }
//        if (start < end) {
//            totalhours = end - start
//
//            if (endMinute - startMinute >= 0) {
//                totalminutes = endMinute - startMinute
//            } else {
//                totalhours = totalhours - 1
//                totalminutes = endMinute + 60 - startMinute
//            }
//
//        } else {
//
//            totalhours = 24 - start + end
//
//            if (endMinute - startMinute >= 0) {
//                totalminutes = endMinute - startMinute
//            } else {
//                totalhours = totalhours - 1
//                totalminutes = endMinute + 60 - startMinute
//            }
//        }

        String printString = String.format("%02d:%02d", totalhours, totalminutes);
//        if (totalhours == 24) {
//            printString = String.format("%02d:%02d", totalhours, totalminutes)
//        } else {
//            printString = String.format("%02d:%02d", if (totalhours === 12 || totalhours === 0) 12 else totalhours % 12, totalminutes)
//        }

        //Log.d("difference", "difference between " + startTime + "and " + endTime + "is " + totalhours.toString() + ":" + totalminutes.toString())
        Log.d("difference", "difference between " + startTime + " and " + endTime + " is " + printString);

        int temp = 60 * totalminutes + 3600 * totalhours;

        Log.d("seconds", String.valueOf(temp));
        secondsArrayList.add(((double) temp));
        foundedTimeArrayList.add(mProfileViewModel);
//        println(totalhours)
//        println(totalminutes)
    }

    /**
     * function to check if incoming call is saved in contacts
     *
     * @param context     context
     * @param phoneNumber incoming phone number
     * @return saved contact name
     */
    private String checkContactIsSaved(Context context, String phoneNumber) {
        String res = "";
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor c = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (c != null) { // cursor not null means number is found contactsTable
                if (c.moveToFirst()) {   // so now find the contact Name
                    res = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                }
                c.close();
            }
        } catch (Exception ex) {
            /* Ignore */
            res = "";
        }
        return res;
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