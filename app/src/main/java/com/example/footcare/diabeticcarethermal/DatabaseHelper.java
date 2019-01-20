package com.example.footcare.diabeticcarethermal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.util.Calendar;

//Taken from https://www.youtube.com/watch?v=cp2rL3sAFmI
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "footcarethermal.db";

//    //Table Journal Entry
//    public static final String TABLE_JOURNAL_ENTRY = "JournalEntry";
//    public static final String COL_JOURNAL_ID = "JournalID";
//    public static final String COL_ENTRY_DATE = "EntryDate";
//    public static final String COL_DAILY_TASK = "DailyTask";
//    public static final String COL_RATING = "Rating";


    // Table User data
    public static final String TABLE_USER_DATA = "UserData";
    public static final String COL_USER_ID = "UserID";
    public static final String COL_ACTION_YEAR = "ActionYear";
    public static final String COL_ACTION_MONTH = "ActionMonth";
    public static final String COL_ACTION_DAY = "ActionDay";
    public static final String COL_ACTION_TIME = "ActionTime";
    public static final String COL_SECTION_ACCESSED = "SectionAccessed";

    //Table Images
    public static final String TABLE_IMAGES = "Images";
    public static final String COL_IMAGE_ID = "ImageID";
    public static final String COL_IMAGE_TYPE = "ImageType";
    public static final String COL_IMAGE_PATH = "ImagePath";
    public static final String COL_IMAGE_DATE = "Date";
    public static final String COL_IMAGE_FOOT = "FootType";
    public static final String COL_NOTES = "Notes";
    //Add Foreign Key Here

    //Table Notifications
    public static final String TABLE_NOTIFICATIONS = "Notifications";
    public static final String COL_NOTIFICATION_ID = "ID";
    public static final String COL_NOTIFICATION_ACTIVE = "Active";
    public static final String COL_NOTIFICATION_TITLE = "Title";
    public static final String COL_NOTIFICATION_TEXT = "Content";
    public static final String COL_NOTIFICATION_INTERVAL = "Interval";
    public static final String COL_NOTIFICATION_HOUR = "Hour";
    public static final String COL_NOTIFICATION_MIN = "Min";

    //Table Analysis
    public static final String TABLE_ANALYSIS = "Analysis";
    public static final String COL_ITEM_ID = "ItemID";
    public static final String COL_WOUND_SIZE = "WoundSize";
    public static final String COL_WOUND_FOOT_PERCENT = "WoundFootPercent";
    public static final String COL_FOOT = "FootType";
    public static final String COL_TEMP = "Temp";
    public static final String COL_SLOUGH = "Slough";
    public static final String COL_NECROTIC = "Necrotic";
    public static final String COL_GRANULATION = "Granulation";
    public static final String COL_DATE = "Date";

    //Table Bounding Box
    public static final String TABLE_BOUNDING_BOX = "BoundingBox";
    public static final String COL_BOUNDING_BOX_ID = "BoundingBoxID";
    public static final String COL_X = "X";
    public static final String COL_Y = "Y";
    public static final String COL_WIDTH = "Width";
    public static final String COL_HEIGHT = "Height";

    // Table Thermal Data
    public static final String TABLE_TEMP = "Temps";
    public static final String COL_TEMP_ID = "TempID";
    public static final String COL_TEMP_DATE = "Date";
    public static final String COL_TEMP_TEMP = "TempVal";
    public static final String COL_TEMP_FOOT = "FootType";

    // Settings Details
    public static final String TABLE_SETTINGS = "settings";
    public static final String COL_TITLE = "Title";
    public static final String COL_PHONE_NUMBER = "PhoneNumber";



    public DatabaseHelper(Context context) {super(context, DATABASE_NAME, null, 1);}

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("create table " + TABLE_JOURNAL_ENTRY +" (JOURNALID INTEGER PRIMARY KEY AUTOINCREMENT, ENTRYDATE TEXT, RATING INT, DAILYTASK TEXT)");
        db.execSQL("create table " + TABLE_NOTIFICATIONS +" (ITEMID INTEGER PRIMARY KEY AUTOINCREMENT, ID TEXT, ACTIVE TEXT, TITLE TEXT, CONTENT TEXT, INTERVAL TEXT, HOUR TEXT, MIN TEXT)");
        db.execSQL("create table " + TABLE_ANALYSIS +" (ITEMID INTEGER PRIMARY KEY AUTOINCREMENT, WOUNDSIZE TEXT, WOUNDFOOTPERCENT TEXT, FOOTTYPE TEXT, DATE TEXT)");
        db.execSQL("create table " + TABLE_BOUNDING_BOX +" (BOUNDINGBOXID INTEGER PRIMARY KEY AUTOINCREMENT, X FLOAT, Y FLOAT, WIDTH FLOAT, HEIGHT FLOAT)");
        db.execSQL("create table " + TABLE_IMAGES +" (IMAGEID INTEGER PRIMARY KEY AUTOINCREMENT, IMAGETYPE TEXT, IMAGEPATH TEXT, FOOTTYPE TEXT, NOTES TEXT, DATE TEXT, ITEMID INT)");
        db.execSQL("create table " + TABLE_USER_DATA +" (USERID TEXT, ACTIONYEAR TEXT, ACTIONMONTH TEXT, ACTIONDAY TEXT, ACTIONTIME TEXT, SECTIONACCESSED TEXT)");
        db.execSQL("create table " + TABLE_TEMP +" (TEMPID INTEGER PRIMARY KEY AUTOINCREMENT, DATE TEXT, TEMPVAL TEXT, FOOTTYPE TEXT)");
        db.execSQL("create table " + TABLE_SETTINGS +"(SETTINGID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT, PHONENUMBER TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
//        db.execSQL("DROP TABLE IF EXISTS "+TABLE_JOURNAL_ENTRY);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_ANALYSIS);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_BOUNDING_BOX);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_USER_DATA);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_TEMP);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_SETTINGS);
        onCreate(db);
    }
    //For Kyle
    public boolean insertUserData(String id, String section) {
        SQLiteDatabase db = this.getWritableDatabase();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        String theYear = String.valueOf(cal.get(Calendar.YEAR));
        String theMonth = String.valueOf(cal.get(Calendar.MONTH));
        String theDay = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String theTime = String.valueOf(cal.get(Calendar.HOUR))+ ":" + String.valueOf(cal.get(Calendar.MINUTE));
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_ID, Build.SERIAL);
        contentValues.put(COL_ACTION_YEAR, theYear);
        contentValues.put(COL_ACTION_MONTH, theMonth);
        contentValues.put(COL_ACTION_DAY, theDay);
        contentValues.put(COL_ACTION_TIME, theTime);
        contentValues.put(COL_SECTION_ACCESSED, section);
        long result = db.insert(TABLE_USER_DATA,null ,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    //Insert data needs to be copied and changed to suit each insert's needs
    public boolean insertData(String woundsize, String woundfootpercent, String foottype, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_WOUND_SIZE,woundsize);
        contentValues.put(COL_WOUND_FOOT_PERCENT,woundfootpercent);
        contentValues.put(COL_FOOT,foottype);
        contentValues.put(COL_DATE,date);
        long result = db.insert(TABLE_ANALYSIS,null ,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    //For inserting image URI's into the DB
    public boolean insertImage(String imageURI, String foottype, String notes, String imageDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_IMAGE_PATH,imageURI);
        contentValues.put(COL_IMAGE_FOOT,foottype);
        contentValues.put(COL_NOTES,notes);
        contentValues.put(COL_IMAGE_DATE,imageDate);
        long result = db.insert(TABLE_IMAGES,null ,contentValues);
        if (result == -1)
            return false;
        else
             return true;
    }

    public boolean insertNotificationData(String id, String active, String title, String content, String interval, String hour, String min) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NOTIFICATION_ID, id);
        contentValues.put(COL_NOTIFICATION_ACTIVE, active);
        contentValues.put(COL_NOTIFICATION_TITLE, title);
        contentValues.put(COL_NOTIFICATION_TEXT, content);
        contentValues.put(COL_NOTIFICATION_INTERVAL, interval);
        contentValues.put(COL_NOTIFICATION_HOUR, hour);
        contentValues.put(COL_NOTIFICATION_MIN, min);
        long result = db.insert(TABLE_NOTIFICATIONS,null ,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public void updateNotificationData(String id, String active, String title, String content, String interval, String hour, String min){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NOTIFICATION_ID, id);
        contentValues.put(COL_NOTIFICATION_ACTIVE, active);
        contentValues.put(COL_NOTIFICATION_TITLE, title);
        contentValues.put(COL_NOTIFICATION_TEXT, content);
        contentValues.put(COL_NOTIFICATION_INTERVAL, interval);
        contentValues.put(COL_NOTIFICATION_HOUR, hour);
        contentValues.put(COL_NOTIFICATION_MIN, min);
        db.update(TABLE_NOTIFICATIONS,contentValues,"id='"+id+"'",null);
    }

    public Cursor getAllData(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+tableName,null);
        return res;
    }


    public void updateAnalysis(String woundsize, String woundfootpercent, String foottype, String newdate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_FOOT,foottype);
        contentValues.put(COL_WOUND_SIZE,woundsize);
        contentValues.put(COL_WOUND_FOOT_PERCENT,woundfootpercent);
        db.update(TABLE_ANALYSIS,contentValues,"date='"+newdate+"'AND foottype='"+foottype+"'",null);
    }


    //For inserting image URI's into the DB
    public boolean insertTemp(String temp, String foottype, String tempDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TEMP_DATE,tempDate);
        contentValues.put(COL_TEMP_TEMP,temp);
        contentValues.put(COL_TEMP_FOOT,foottype);
        long result = db.insert(TABLE_TEMP,null ,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public void updateTemp(String temp, String foottype, String tempDate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TEMP_DATE,tempDate);
        contentValues.put(COL_TEMP_TEMP,temp);
        contentValues.put(COL_TEMP_FOOT,foottype);
        db.update(TABLE_TEMP,contentValues,"date='"+tempDate+"'AND foottype='"+foottype+"'",null);
    }

    public boolean insertContact(String Title, String Number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TITLE, Title);
        contentValues.put(COL_PHONE_NUMBER, Number);
        long result = db.insert(TABLE_SETTINGS,null ,contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public void updateContact(String Title, String Number, int entry){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TITLE, Title);
        contentValues.put(COL_PHONE_NUMBER, Number);
        db.update(TABLE_SETTINGS,contentValues,"settingid='" + Integer.toString(entry)+ "'",null);
    }
}
