package uk.co.sequoiasolutions.bookcase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class EbookDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_TITLE,
            MySQLiteHelper.COLUMN_AUTHOR,
            MySQLiteHelper.COLUMN_DESCRIPTION,
            MySQLiteHelper.COLUMN_IMAGEURL,
            MySQLiteHelper.COLUMN_EBOOKURL
    };

    public EbookDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Ebook createEbook(Ebook ebook) {
        return createEbook(ebook.getTitle(), ebook.getAuthor(), ebook.getDescription(), ebook.getImageUrl(), ebook.getEbookUrl());
    }

    public Ebook createEbook(String title, String author, String description, byte[] imageUrl, String ebookUrl) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, title);
        values.put(MySQLiteHelper.COLUMN_AUTHOR, author);
        values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description);
        values.put(MySQLiteHelper.COLUMN_IMAGEURL, imageUrl);
        values.put(MySQLiteHelper.COLUMN_EBOOKURL, ebookUrl);
        long insertId = database.insert(MySQLiteHelper.TABLE_EBOOKS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_EBOOKS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Ebook newEbook = cursorToEbook(cursor);
        cursor.close();
        return newEbook;
    }

    public void deleteEbook(Ebook ebook) {
        long id = ebook.getId();
        System.out.println("Ebook deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_EBOOKS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<Ebook> getAllEbooks() {
        List<Ebook> ebooks = new ArrayList<Ebook>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_EBOOKS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Ebook ebook = cursorToEbook(cursor);
            ebooks.add(ebook);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return ebooks;
    }

    public List<Ebook> getBatch(int start, int count) {
        List<Ebook> ebooks = new ArrayList<Ebook>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_EBOOKS,
                allColumns, MySQLiteHelper.COLUMN_ID + ">" + start + " and " + MySQLiteHelper.COLUMN_ID + "<" + start + count, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Ebook ebook = cursorToEbook(cursor);
            ebooks.add(ebook);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return ebooks;
    }

    public boolean ebookExists(String ebookUrl) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_EBOOKS, new String[]{MySQLiteHelper.COLUMN_EBOOKURL}, MySQLiteHelper.COLUMN_EBOOKURL + "= ?", new String[]{ebookUrl}, null, null, null);
        cursor.moveToFirst();
        boolean result = !cursor.isAfterLast();
        cursor.close();
        return result;
    }

    public Cursor getEbookCursor() {
        return database.query(MySQLiteHelper.TABLE_EBOOKS, new String[]{MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_TITLE, MySQLiteHelper.COLUMN_AUTHOR, MySQLiteHelper.COLUMN_IMAGEURL}, null, null, null, null, null);
    }

    private Ebook cursorToEbook(Cursor cursor) {
        Ebook ebook = new Ebook();
        ebook.setId(cursor.getLong(0));
        ebook.setTitle(cursor.getString(1));
        ebook.setAuthor(cursor.getString(2));
        ebook.setDescription(cursor.getString(3));
        ebook.setImageUrl(cursor.getBlob(4));
        ebook.setEbookUrl(cursor.getString(5));
        return ebook;
    }
} 
