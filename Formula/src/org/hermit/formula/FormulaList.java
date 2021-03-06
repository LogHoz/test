
/**
 * Formula: programmable custom computations.
 * <br>Copyright 2009 Ian Cameron Smith
 *
 * <p>This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation (see COPYING).
 * 
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


package org.hermit.formula;


import org.hermit.formula.provider.FormulaSchema;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 * Displays a list of the available formulae.  Will display formulae
 * from the {@link Uri} provided in the intent if there is one,
 * otherwise defaults to displaying the contents of the
 * {@link org.hermit.formula.provider.FormulaProvider}.
 */
public class FormulaList
	extends ListActivity
{

	// ******************************************************************** //
    // Activity Lifecycle.
    // ******************************************************************** //

	/**
	 * Called when the activity is starting.  This is where most
	 * initialization should go: calling setContentView(int) to inflate
	 * the activity's UI, etc.
	 * 
	 * You can call finish() from within this function, in which case
	 * onDestroy() will be immediately called without any of the rest of
	 * the activity lifecycle executing.
	 * 
	 * Derived classes must call through to the super class's implementation
	 * of this method.  If they do not, an exception will be thrown.
	 * 
	 * @param	icicle			If the activity is being re-initialized
	 * 							after previously being shut down then this
	 * 							Bundle contains the data it most recently
	 * 							supplied in onSaveInstanceState(Bundle).
	 * 							Note: Otherwise it is null.
	 */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null)
            intent.setData(FormulaSchema.Formulae.CONTENT_URI);

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
        
        // Perform a managed query.  The Activity will handle closing and
        // re-querying the cursor when needed.
        Cursor cursor = managedQuery(getIntent().getData(),
        						     PROJECTION, null, null,
        						     FormulaSchema.Formulae.SORT_ORDER);
        
        // Get the columns of our displayed fields.
        titleCol = cursor.getColumnIndexOrThrow(FormulaSchema.Formulae.TITLE);
        validCol = cursor.getColumnIndexOrThrow(FormulaSchema.Formulae.VALID);
        
        // Set up an adaptor to map formulae from the database to the list
        // view.  We set up a mapping from the relevant database field to
        // the text widget in the list.
        String[] dbFields = new String[] { FormulaSchema.Formulae.TITLE,
        								   FormulaSchema.Formulae.DESCR,
        								   FormulaSchema.Formulae.VALID };
        int[] uiFields = new int[] { R.id.title, R.id.description, R.id.icon };
        SimpleCursorAdapter adapter =
        	new SimpleCursorAdapter(this, R.layout.formula_list_item,
        						    cursor, dbFields, uiFields);
        
        // Set a custom view binder, so we can set the indicator icon
        // appropriately.
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
    		@Override
    		public boolean setViewValue(View v, Cursor c, int cindex) {
    			if (cindex == titleCol && v instanceof TextView) {
    				String title = c.getString(cindex);
    				if (title != null)
    					title = title.trim();
    				if (title == null || title.length() == 0)
    					title = "<unnamed>";
    				((TextView) v).setText(title);
    				return true;
    			} else if (cindex == validCol && v instanceof ImageView) {
    				ImageView iv = (ImageView) v;
    				int valid = c.getInt(cindex);
    				int icon;
    				switch (valid) {
    				case 0:
    					icon = R.drawable.indicator_error;
    					break;
    				default:
    					icon = R.drawable.indicator_ok;
    					break;
    				}
    				iv.setImageResource(icon);
    				return true;
    			}
    			
    			// Leave to the base class.
    			return false;
    		}
        });
        
        setListAdapter(adapter);
    }


	// ******************************************************************** //
    // Menu and Preferences Handling.
    // ******************************************************************** //

	/**
     * Initialize the contents of the options menu by adding items
     * to the given menu.
     * 
     * This is only called once, the first time the options menu is displayed.
     * To update the menu every time it is displayed, see
     * onPrepareOptionsMenu(Menu).
     * 
     * When we add items to the menu, we can either supply a Runnable to
     * receive notification of selection, or we can implement the Activity's
     * onOptionsItemSelected(Menu.Item) method to handle them there.
     * 
     * @param	menu			The options menu in which we should
     * 							place our items.  We can safely hold on this
     * 							(and any items created from it), making
     * 							modifications to it as desired, until the next
     * 							time onCreateOptionsMenu() is called.
     * @return					true for the menu to be displayed; false
     * 							to suppress showing it.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, FormulaList.class), null, intent, 0, null);

        return true;
    }

    
    /**
     * Prepare the options menu to be displayed.  This is called right
     * before the menu is shown, every time it is shown.
     * 
     * @param	menu			The options menu as last shown or first
     * 							initialized by onCreateOptionsMenu().
     * @return					You must return true for the menu to be
     * 							displayed; if you return false it will
     * 							not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        final boolean haveItems = getListAdapter().getCount() > 0;
        if (haveItems) {
            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Build menu...  always starts with the EDIT action...
            Intent[] specifics = new Intent[1];
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
            MenuItem[] items = new MenuItem[1];

            // ... is followed by whatever other actions are available...
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0,
                    items);
        } else {
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        return true;
    }
    

    /**
     * This hook is called whenever an item in your options menu is selected.
     * Derived classes should call through to the base class for it to
     * perform the default menu handling.  (True?)
     *
     * @param	item			The menu item that was selected.
     * @return					false to have the normal processing happen.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_insert:
            // Launch activity to insert a new item
            startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        
        return true;
    }

    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);
    }
        
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case MENU_ITEM_DELETE: {
                // Delete the note that the context menu is for
                Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                getContentResolver().delete(noteUri, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a note selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // Launch activity to view/edit the currently selected item.
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }


	// ******************************************************************** //
	// Class Data.
	// ******************************************************************** //

    // Debugging tag.
	private static final String TAG = "formula";

    // Menu item ids
    public static final int MENU_ITEM_DELETE = Menu.FIRST;
    public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;

    // The columns we are interested in from the database
    private static final String[] PROJECTION = new String[] {
    	FormulaSchema.Formulae._ID,
        FormulaSchema.Formulae.TITLE,
        FormulaSchema.Formulae.DESCR,
        FormulaSchema.Formulae.VALID,
    };


	// ******************************************************************** //
	// Private Data.
	// ******************************************************************** //

    // The index of the title column.
    private static final int COLUMN_INDEX_TITLE = 1;
    
    // The column numbers of our displayed fields in the current query.
    private int titleCol = -1;
    private int validCol = -1;


}

