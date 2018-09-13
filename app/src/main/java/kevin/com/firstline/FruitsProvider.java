package kevin.com.firstline;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.litepal.LitePal;

public class FruitsProvider extends ContentProvider {

    static final int URI_FRUIT_TABLE = 0;
    static final int URI_FRUIT_ITEM_NAME = 1;
    static final int URI_FRUIT_ITEM_ID = 2;

    private static UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("kevin.com.firstline.provider", "fruit", URI_FRUIT_TABLE);
        uriMatcher.addURI("kevin.com.firstline.provider", "fruit/*", URI_FRUIT_ITEM_NAME);
        uriMatcher.addURI("kevin.com.firstline.provider", "fruit/#", URI_FRUIT_ITEM_ID);    //this will never hit if fruit/* exists!!
    }

    public FruitsProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_FRUIT_TABLE:
                return "vnd.android.cursor.dir/vnd.kevin.com.firstline.provider.fruit";
            case URI_FRUIT_ITEM_NAME:
                return "vnd.android.cursor.item/vnd.kevin.com.firstline.provider.fruit";
            case URI_FRUIT_ITEM_ID:
                return "vnd.android.cursor.item/vnd.kevin.com.firstline.provider.fruit";
             default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_FRUIT_TABLE:
                Log.i("SS", "hit1");
                return LitePal.getDatabase().query("fruit", projection, selection, selectionArgs, null, null, null);
            case URI_FRUIT_ITEM_NAME:
                Log.i("SS", "hit2");
                String name = uri.getPathSegments().get(1);
                return LitePal.getDatabase().query("fruit", projection, "name=?", new String[]{name}, null, null, null);
            case URI_FRUIT_ITEM_ID:
                Log.i("SS", "hit3, ACTUALLY NEVER HIT!");
                String id = uri.getPathSegments().get(1);
                return LitePal.getDatabase().query("fruit", projection, "id=?", new String[]{id}, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (uriMatcher.match(uri) == URI_FRUIT_TABLE) {
            return LitePal.getDatabase().update("fruit", values, selection, selectionArgs);
        }
        return 0;
    }
}
