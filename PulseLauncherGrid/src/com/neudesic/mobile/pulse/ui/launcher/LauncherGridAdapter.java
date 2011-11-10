package com.neudesic.mobile.pulse.ui.launcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.neudesic.mobile.pulse.ui.drawable.DrawableManager;
import com.neudesic.mobile.ui.drag.DeleteLaunchItemHandler;
import com.neudesic.mobile.ui.drag.DeleteZone;
import com.neudesic.mobile.ui.drag.DragController;
import com.neudesic.mobile.ui.drag.DragLayer;
import com.neudesic.mobile.ui.drag.DragListener;
import com.neudesic.mobile.ui.drag.DragSource;
import com.neudesic.mobile.ui.drag.DraggableRelativeLayout;

public class LauncherGridAdapter extends BaseAdapter implements
		OnLongClickListener, OnClickListener, DragListener,
		DeleteLaunchItemHandler {

	public static final String LAUNCHER_GRID_LOG = "LauncherGridLog";

	@JsonIgnore
	private LayoutInflater inflater;

	// @JsonIgnore
	// private ViewHolder holder;

	@JsonProperty("items")
	private List<LauncherGridItem> items;


	@JsonIgnore
	private GridView iconGrid;

	@JsonIgnore
	private DragController dragController;

	@JsonIgnore
	private DragLayer dragLayer;

	@JsonIgnore
	private DeleteZone deleteZone;

	@JsonIgnore
	private boolean editable = true;

	private LoadThumbsTask imageLoader;

	private static ObjectMapper mapper;

	static class ViewHolder {

		public ImageView image;
		public TextView text;

	}

	public LauncherGridAdapter() {

	}

	public LauncherGridAdapter(Context c, List<LauncherGridItem> items,
			GridView iconGrid, DragLayer dragLayer) {
		this(c, items, iconGrid, dragLayer, null);
	}

	public LauncherGridAdapter(Context c, List<LauncherGridItem> items,
			GridView iconGrid, DragLayer dragLayer, DeleteZone deleteZone) {
		context = c;
		this.setItems(items);
		this.iconGrid = iconGrid;

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setMapper(new ObjectMapper());

		getMapper()
				.configure(
						DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);


		dragController = new DragController(context);
		this.dragLayer = dragLayer;
		this.dragLayer.setDragController(dragController);
		this.dragLayer.setGridView(iconGrid);

		if (deleteZone != null) {
			this.deleteZone = deleteZone;
			this.deleteZone.setOnItemDeleted(this);
			this.deleteZone.setEnabled(true);
			this.dragLayer.setDeleteZoneId(deleteZone.getId());
		}

		dragController.setDragListener(dragLayer);
		loadWebImageCache();
	}

	public void loadWebImageCache() {
		imageLoader = new LoadThumbsTask();
//		images = new ArrayList<Image>(items.size()); 
//		for (int i = 0; i < items.size(); i++) {
//			Image image = new Image();
//			image.url = items.get(i).getUrl();
//			images.add(image);
//		}
		imageLoader.execute(null);
	}

	public void persist() {
		LauncherGridItemList list = new LauncherGridItemList(this.items);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		Editor editor = prefs.edit();
		String message = "";
		try {
			message = getMapper().writeValueAsString(list);
		} catch (Exception e) {
			Log.w(LAUNCHER_GRID_LOG, "Unable to serialize launcher grid", e);
		}
		editor.putString(getPersistenceToken(), message);

		editor.commit();
	}

	public static void persist(Context context, String persistenceToken,
			List<LauncherGridItem> items) {
		LauncherGridItemList list = new LauncherGridItemList(items);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		Editor editor = prefs.edit();
		String message = "";
		try {
			message = getMapper().writeValueAsString(list);
		} catch (Exception e) {
			Log.w(LAUNCHER_GRID_LOG, "Unable to serialize launcher grid", e);
		}
		editor.putString(persistenceToken, message);

		editor.commit();
	}

	public static List<LauncherGridItem> loadItems(Context context,
			String persistenceToken) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs.contains(persistenceToken)) {
			try {

				LauncherGridItemList list = getMapper().readValue(
						prefs.getString(persistenceToken, ""),
						LauncherGridItemList.class);
				return list.getItems();
			} catch (Exception e) {

			}
		}
		return new ArrayList<LauncherGridItem>();
	}

	public boolean restore() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs.contains(getPersistenceToken())) {
			try {
				LauncherGridItemList list = getMapper().readValue(
						prefs.getString(getPersistenceToken(), ""),
						LauncherGridItemList.class);
				this.items = list.getItems();
				loadWebImageCache();
				this.notifyDataSetChanged();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public int getCount() {
		return getItems().size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	// public abstract View getView(int position, View convertView, ViewGroup
	// parent);
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater
					.inflate(R.layout.grid_view_item_layout, null);

			holder = new ViewHolder();

			holder.image = (ImageView) convertView
					.findViewById(R.id.selection_item_image);
			holder.text = (TextView) convertView
					.findViewById(R.id.selection_item_text);

			convertView.setTag(holder);
			//holder.image.setPadding(8, 8, 8, 8);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		DraggableRelativeLayout layout = (DraggableRelativeLayout) convertView;
		convertView.setOnLongClickListener(this);
		convertView.setOnClickListener(this);
		LauncherGridItem item = getItems().get(position);
		layout.setItem(item);
		layout.setDragListener(this);
		// holder.image.setOnLongClickListener(this);
		Drawable d = holder.image.getDrawable();
		// if (d != null) {
		d.setCallback(null);
		// }
		holder.image.setImageDrawable(null);
		if (!item.getUrl().startsWith("http")) {

			try {
				holder.image.setImageDrawable(context.getResources()
						.getDrawable(
								context.getResources().getIdentifier(
										context.getApplicationContext()
												.getPackageName()
												+ ":drawable/" + item.getUrl(),
										null, null)));
			} catch (NotFoundException e) {
				holder.image.setImageResource(android.R.drawable.ic_menu_view);
			}
		} else {
//			Image cached = images.get(position);
			if(item.getImage() == null)
			{
				holder.image.setImageResource(R.drawable.avatar);
			} else {

				holder.image.setScaleType(ScaleType.CENTER_INSIDE);
				holder.image.setImageBitmap(item.getImage());
			}
//			String imageUrl = item.getUrl();
//			if (holder.image.getDrawable() == null) {
//				getDrawableManager().fetchDrawableOnThread(imageUrl,
//						holder.image);
//			}
		}
		holder.text.setText(item.getCaption());
		layout.setImage(holder.image);
		layout.setText(holder.text);
		layout.setItem(item);
		layout.canDelete(item.canDelete());
		return convertView;
	}

	public static int getResId(String variableName, Context context, Class<?> c) {

		try {
			Field idField = c.getDeclaredField(variableName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public List<LauncherGridItem> getItems() {
		return items;
	}

	private Context context;

	private String persistenceToken = "LauncherGrid";

	public boolean startDrag(View v) {
		DragSource dragSource = (DragSource) v;

		// We are starting a drag. Let the DragController handle it.
		dragController.startDrag(v, dragSource, dragSource,
				DragController.DRAG_ACTION_MOVE);

		return true;
	}

	@Override
	public boolean onLongClick(View v) {
		if (!v.isInTouchMode() || !isEditable()) {
			// toast
			// ("isInTouchMode returned false. Try touching the view again.");
			return false;
		}
		Log.d(LAUNCHER_GRID_LOG, "Drag started");
		return startDrag(v);
	}

	@Override
	public void onClick(View v) {
		DraggableRelativeLayout layout = (DraggableRelativeLayout) v;
		Intent intent = layout.getItem().getIntent();
		if (intent != null) {
			context.startActivity(intent);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.neudesic.mobile.ui.drag.DragListener#onDropCompleted(android.view
	 * .View, android.view.View, boolean)
	 */
	@Override
	public void onDropCompleted(View source, View target, boolean success) {
		Log.d(LAUNCHER_GRID_LOG, "Drag completed");
		if (success && source != target) {

			LauncherGridItem sourceItem = ((DraggableRelativeLayout) source)
					.getItem();
			if (target instanceof DeleteZone) {
				if (sourceItem.canDelete()) {
					this.getItems().remove(sourceItem);
				} else {
					Toast.makeText(context, "Can't delete this item",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				LauncherGridItem targetItem = ((DraggableRelativeLayout) target)
						.getItem();
				int removeFrom = getItems().indexOf(sourceItem);
				int insertAt = getItems().size();

				if (targetItem != null) {
					insertAt = getItems().indexOf(targetItem);
				}

				LauncherGridItem item = getItems().remove(removeFrom);
//				Image img = images.remove(removeFrom);
				if (insertAt >= getItems().size()) {
					insertAt = getItems().size();
				}
				getItems().add(insertAt, item);
//				images.add(insertAt, img);
			}
			this.notifyDataSetInvalidated();
			this.persist();
		}

		this.dragLayer.getDragListener().onDragEnd();

	}

	@Override
	public void itemDeleted(DragSource source) {
		// this.items.remove(((DraggableRelativeLayout) source).getItem());
		// this.notifyDataSetInvalidated();

	}

	@Override
	public void onDragStarted(View source) {
		// TODO Auto-generated method stub

	}

	public void setDragListener(DragController.DragListener listener) {
		this.dragLayer.setDragListener(listener);

	}

	public void setItems(List<LauncherGridItem> items) {
		this.items = items;
	}

	public String getPersistenceToken() {
		return persistenceToken;
	}

	public void setPersistenceToken(String persistenceToken) {
		this.persistenceToken = persistenceToken;
	}

	@JsonIgnore
	public boolean isEditable() {
		return editable;
	}

	@JsonIgnore
	public void setEditable(boolean editable) {
		this.editable = editable;
	}



	public static ObjectMapper getMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();

			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
		}
		return mapper;
	}

	public static void setMapper(ObjectMapper mapper) {
		LauncherGridAdapter.mapper = mapper;
	}
	


	/**
	 * Download and return a thumb specified by url, subsampling 
	 * it to a smaller size.
	 */
	private Bitmap loadThumb(String url) {

		// the downloaded thumb (none for now!)
		Bitmap thumb = null;

		// sub-sampling options
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;

		try {

			// open a connection to the URL
			// Note: pay attention to permissions in the Manifest file!
			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.connect();
			
			// read data
			BufferedInputStream stream = new BufferedInputStream(c.getInputStream());
			BitmapFactory.Options ops = new BitmapFactory.Options();
			ops.inJustDecodeBounds = false;
			thumb = BitmapFactory.decodeStream(stream, null, ops);
			// decode the data, subsampling along the way
			//thumb = BitmapFactory.decodeStream(stream, null, opts);

			// close the stream
			stream.close();

		} catch (MalformedURLException e) {
			Log.e("Threads03", "malformed url: " + url);
		} catch (IOException e) {
			Log.e("Threads03", "An error has occurred downloading the image: " + url);
		}

		// return the fetched thumb (or null, if error)
		return thumb;
	}
	// an object we'll use to keep our cache data together
	private class Image {
		String url;
		Bitmap thumb;
	}
	
	private void cacheUpdated() {
		this.notifyDataSetChanged();
	}


	// an array of resources we want to display
//	private ArrayList<Image> images;
	
	// the class that will create a background thread and generate thumbs
	private class LoadThumbsTask extends AsyncTask<Void, Void, Void> {

		/**
		 * Generate thumbs for each of the Image objects in the array
		 * passed to this method. This method is run in a background task.
		 */
		@Override
		protected Void doInBackground(Void... cache) {

			
			// define the options for our bitmap subsampling 
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 4;

			// iterate over all images ...
			for (LauncherGridItem i : items) {

				// if our task has been cancelled then let's stop processing
				if(isCancelled()) return null;

				// skip a thumb if it's already been generated
				if(i.getImage() != null) continue;

				// artificially cause latency!
				SystemClock.sleep(500);
				
				// download and generate a thumb for this image
				if(i.getUrl().startsWith("http"))
				{
					i.setImage(loadThumb(i.getUrl()));
				}
				// some unit of work has been completed, update the UI
				publishProgress();
			}
			
			return null;
		}


		/**
		 * Update the UI thread when requested by publishProgress()
		 */
		@Override
		protected void onProgressUpdate(Void... param) {
			cacheUpdated();
		}
		
		
	}

}
