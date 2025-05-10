package com.example.myprojectishe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pvz_delivery.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE roles (" +
                "role_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "role_name TEXT NOT NULL UNIQUE, " +
                "description TEXT)");
        
        db.execSQL("CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "phone TEXT, " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "is_active INTEGER DEFAULT 1)");
        
        db.execSQL("CREATE TABLE user_roles (" +
                "user_id INTEGER, " +
                "role_id INTEGER, " +
                "PRIMARY KEY (user_id, role_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                "FOREIGN KEY (role_id) REFERENCES roles(role_id))");
        
        db.execSQL("CREATE TABLE products (" +
                "product_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "price REAL NOT NULL, " +
                "stock_quantity INTEGER NOT NULL DEFAULT 0, " +
                "created_by INTEGER, " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "is_active INTEGER DEFAULT 1, " +
                "FOREIGN KEY (created_by) REFERENCES users(user_id))");

        db.execSQL("INSERT INTO roles (role_name, description) VALUES " +
                "('admin', 'Полный доступ'), " +
                "('customer', 'Покупатель'), " +
                "('courier', 'Курьер')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS roles");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS user_roles");
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }
}