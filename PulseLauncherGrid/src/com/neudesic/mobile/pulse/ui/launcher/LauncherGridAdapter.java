package com.neudesic.mobile.pulse.ui.launcher;

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
import android.graphics.drawable.Drawable;
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

	@JsonIgnore
	private ViewHolder holder;

	@JsonProperty("items")
	private List<LauncherGridItem> items;

	@JsonIgnore
	private DrawableManager drawableManager;

	
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
	
	private ObjectMapper mapper;

	private int deleteZoneDrawableId;

	static class ViewHolder {

		public ImageView image;
		public TextView text;

	}

	public LauncherGridAdapter() {

	}

	
	
	public LauncherGridAdapter(Context c, List<LauncherGridItem> items,
			GridView iconGrid, DragLayer dragLayer, DeleteZone deleteZone) {
		context = c;
		this.setItems(items);
		this.iconGrid = iconGrid;

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mapper = new ObjectMapper();

		mapper.configure(
				DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		this.setDrawableManager(new DrawableManager(c));

		dragController = new DragController(context);
		this.deleteZone = deleteZone;
		this.deleteZone.setOnItemDeleted(this);
		this.deleteZone.setEnabled(true);
		this.dragLayer = dragLayer;
		this.dragLayer.setDragController(dragController);
		this.dragLayer.setDeleteZoneId(getDeleteZoneDrawableId());
		this.dragLayer.setGridView(iconGrid);

		dragController.setDragListener(dragLayer);

	}
	
	public void setDefaultHttpClient(DefaultHttpClient client)
	{
		this.drawableManager.setHttpClient(client);
	}


	public void persist() {
		LauncherGridItemList list = new LauncherGridItemList(this.items);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		Editor editor = prefs.edit();
		String message = "";
		try {
			message = mapper.writeValueAsString(list);
		} catch (Exception e) {
			Log.w(LAUNCHER_GRID_LOG, "Unable to serialize launcher grid", e);
		}
		editor.putString(getPersistenceToken(), message);

		editor.commit();
	}

	public boolean restore() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs.contains(getPersistenceToken())) {
			try {
				LauncherGridItemList list = mapper.readValue(
						prefs.getString(getPersistenceToken(), ""),
						LauncherGridItemList.class);
				this.items = list.getItems();
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

//	public abstract View getView(int position, View convertView, ViewGroup parent);
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			convertView = inflater
					.inflate(R.layout.grid_view_item_layout, null);
			holder = new ViewHolder();

			holder.image = (ImageView) convertView
					.findViewById(R.id.selection_item_image);
			holder.text = (TextView) convertView
					.findViewById(R.id.selection_item_text);

			convertView.setTag(holder);
			// imageView.setPadding(8, 8, 8, 8);
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
		if (item.getUrl() == null) {
			holder.image.setImageDrawable(context.getResources().getDrawable(
					item.getDrawable()));
		} else {
			Drawable d = holder.image.getDrawable();

			// holder.authorImage.setImageBitmap(null);
			if (d != null) {
				d.setCallback(null);
			}
			String imageUrl = item.getUrl();

			holder.image.setImageResource(R.drawable.avatar);
			getDrawableManager()
					.fetchDrawableOnThread(imageUrl, holder.image);
		}
		holder.text.setText(item.getCaption());
		layout.setImage(holder.image);
		layout.setText(holder.text);
		layout.setItem(item);
		layout.canDelete(item.canDelete());
		return convertView;
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

			LauncherGridItem sourceItem = ((DraggableRelativeLayout) source).getItem();
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
				if (insertAt >= getItems().size()) {
					insertAt = getItems().size();
				}
				getItems().add(insertAt, item);
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

	public int getDeleteZoneDrawableId() {
		return deleteZoneDrawableId;
	}

	public void setDeleteZoneDrawableId(int deleteZoneDrawableId) {
		this.deleteZoneDrawableId = deleteZoneDrawableId;
	}

	@JsonIgnore
	public boolean isEditable() {
		return editable;
	}

	@JsonIgnore
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@JsonIgnore
	public DrawableManager getDrawableManager() {
		return drawableManager;
	}

	@JsonIgnore
	public void setDrawableManager(DrawableManager drawableManager) {
		this.drawableManager = drawableManager;
	}

}
