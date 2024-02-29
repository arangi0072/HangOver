package arpit.rangi.HangOver.adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "data"; // below int is our database version
    private static final int DB_VERSION = 1; // below variable is for our table name.
    private static final String TABLE_NAME = "login"; // below variable is for our id column. // below variable is for our course name column
    private static final String NAME_COL = "login"; // creating a constructor for our database handler.

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    } // below method is for creating a database by running a sqlite query

    @Override
    public void onCreate(SQLiteDatabase db) { // on below line we are creating // an sqlite query and we are // setting our column names // along with their data types.
        String query = "CREATE TABLE " + TABLE_NAME + " (" + NAME_COL + " TEXT)"; // at last we are calling a exec sql // method to execute above sql query
        db.execSQL(query);
    } // this method is use to add new course to our sqlite database.

    public void addNewCourse(String courseName) { // on below line we are creating a variable for // our sqlite database and calling writable method // as we are writing data in our database.
        SQLiteDatabase db = this.getWritableDatabase(); // on below line we are creating a // variable for content values.
        ContentValues values = new ContentValues(); // on below line we are passing all values // along with its key and value pair.
        values.put(NAME_COL, courseName); // after adding all values we are passing // content values to our table.
        db.insert(TABLE_NAME, null, values); // at last we are closing our // database after adding
        db.close();
    }

    @SuppressLint("Range")
    public String readCourses() {
        SQLiteDatabase db = this.getReadableDatabase();
        String phone = ""; // Query to read data from the database
        Cursor cursorCourses = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        try { // Check if the cursor is not null and it has at least one row
            if (cursorCourses != null && cursorCourses.moveToFirst()) { // Retrieve data from the first row and first column
                phone = cursorCourses.getString(cursorCourses.getColumnIndex(NAME_COL)); // Move the cursor to the next row if needed // This is important to prevent "Couldn't read row 0, col 1" in subsequent calls
                cursorCourses.moveToNext();
            }
        } finally { // Close the cursor in a finally block to ensure it is always closed
            if (cursorCourses != null && !cursorCourses.isClosed()) {
                cursorCourses.close();
            }
        }
        return phone;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public void updatePersonName(String id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("login", newName);

        // Specify the WHERE clause
        String whereClause = "login" + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        // Execute the update
        db.update(TABLE_NAME, values, whereClause, whereArgs);

        // Close the database
        db.close();
    }
}