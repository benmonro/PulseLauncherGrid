package com.neuesic.mobile.pulse.ui.sample;

import java.util.ArrayList;

import com.neudesic.mobile.pulse.ui.launcher.LauncherGridAdapter;
import com.neudesic.mobile.pulse.ui.launcher.LauncherGridItem;
import com.neudesic.mobile.ui.drag.DeleteZone;
import com.neudesic.mobile.ui.drag.DragController.DragListener;
import com.neudesic.mobile.ui.drag.DragLayer;
import com.neudesic.mobile.ui.drag.DragSource;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.GridView;

public class PulseLauncherGridSampleActivity extends Activity implements
		DragListener {
	private GridView grid;
	private ArrayList<LauncherGridItem> items;
	private DeleteZone deleteZone;
	private DragLayer dragLayer;
	private LauncherGridAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		grid = (GridView) findViewById(R.id.grid);

		if (getScreenOrientation() == 1)
			grid.setNumColumns(3);
		else
			grid.setNumColumns(4);

		items = new ArrayList<LauncherGridItem>();
		deleteZone = (DeleteZone) findViewById(R.id.delete_zone_view);
		dragLayer = (DragLayer) findViewById(R.id.drag_layer);

		// delete zone is optional if you want just the grid, omit the
		// deleteZone parameter like this:
		// adapter = new LauncherGridAdapter(c, items, grid, dragLayer);
		adapter = new LauncherGridAdapter(this, items, grid, dragLayer,
				deleteZone);

		// TODO: add support for adding new items to default.
		adapter.setPersistenceToken("MyLauncherGrid"); // optional, but if you
														// need to persist the
														// launcher
		adapter.setDragListener(this);
		
		adapter.setEditable(true); // change this to false to disable drag & drop.
		grid.setAdapter(adapter);

		if (!adapter.restore()) {
			
			//Note: the intent for the grid item needs to have unique actions, data & extras in order for sorting/removal to work correctly.
			
			
			Intent intent = new Intent(this, Activity2.class);
			intent.putExtra("Param1", "Child Activity"); 
			
			//intsead of using R.drawable.ic_launcher, just reference it as a string.  This allows
			//persisted images to continue loading correctly even if the R.drawable.x integer value changes.
			
			items.add(new LauncherGridItem("ic_launcher", "Item 1",
					intent));
			
			
			Intent intent2 = new Intent(this, Activity2.class);
			intent2.putExtra("Param1", "Child Activity 2");
			LauncherGridItem item2 = new LauncherGridItem(
					"ic_launcher", "Item 2", intent2);
			item2.canDelete(true); // allow this item to be deleted
			items.add(item2);
			
			Intent intent3 = new Intent(this, Activity2.class);
			intent3.putExtra("Param1", "Child Activity 3");
			items.add(new LauncherGridItem("ic_launcher", "Item 3",
					intent3));
			
			Intent intent4 = new Intent(Intent.ACTION_VIEW);
			intent4.setData(Uri.parse("http://www.facebook.com/zuck"));
			items.add(new LauncherGridItem(
					"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc4/49942_4_1525300_n.jpg",
					"Zuck Dawg", intent4));
			
			
			adapter.persist();
		}

	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		deleteZone.setVisibility(View.VISIBLE);

	}

	@Override
	public void onDragEnd() {
		deleteZone.setVisibility(View.INVISIBLE);

	}

	public int getScreenOrientation() {
		Display getOrient = getWindowManager().getDefaultDisplay();

		int orientation = getOrient.getOrientation();

		Configuration config = getResources().getConfiguration();
		orientation = config.orientation;

		if (orientation == Configuration.ORIENTATION_UNDEFINED
				|| orientation == Configuration.ORIENTATION_SQUARE) {

			if (getOrient.getWidth() == getOrient.getHeight()) {
				orientation = Configuration.ORIENTATION_SQUARE;
			} else {
				if (getOrient.getWidth() < getOrient.getHeight()) {
					orientation = Configuration.ORIENTATION_PORTRAIT;
				} else {
					orientation = Configuration.ORIENTATION_LANDSCAPE;
				}
			}
		}
		return orientation; // return value 1 is portrait and 2 is Landscape
							// Mode
	}

	//
	// @Override
	// public void onConfigurationChanged(Configuration newConfig) {
	// // TODO Auto-generated method stub
	// super.onConfigurationChanged(newConfig);
	;
	// }

}