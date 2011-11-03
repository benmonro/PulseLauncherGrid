package com.neudesic.mobile.ui.drag;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neudesic.mobile.pulse.ui.launcher.LauncherGridItem;

public class DraggableRelativeLayout extends RelativeLayout implements DragSource, DropTarget{

	private ImageView image;
	private TextView text;
	private int cellNumber;
	private LauncherGridItem gridItem;
	private DragListener listener;
	public DraggableRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DraggableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DraggableRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {

	    // The view being dragged does not actually change its parent and switch over to the ImageCell.
	    // What we do is copy the drawable from the source view.
//	    DraggableRelativeLayout sourceView = (DraggableRelativeLayout) source;
//	    
//	    Drawable d = sourceView.image.getDrawable ();
//	    if (d != null) {
//	       this.image.setImageDrawable (d);
//	    }
//	    
//	    this.text.setText(sourceView.text.getText());

	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		return (cellNumber >= 0) && source != this;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDragController(DragController dragger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDropCompleted(View target, boolean success) {
	    // If the drop succeeds, the image has moved elsewhere. 
	    // So clear the image cell.
		
//	    if (success) {
	    	if(listener != null)
	    	{
	    		listener.onDropCompleted(this, target, success);
	    	}
//	       isEmpty = true;
//	       if (cellNumber >= 0) {
////	          int bg = isEmpty ? R.color.cell_empty : R.color.cell_filled;
////	          setBackgroundResource (bg);
//	          image.setImageDrawable (null);
//	       } else {
//	         // For convenience, we use a free-standing ImageCell to
//	         // take the image added when the Add Image button is clicked.
//	         image.setImageResource (0);
//	       }
//	    }
	}

	public void setImage(ImageView image) {
		this.image = image;
		// TODO Auto-generated method stub
		
	}
	
	public ImageView getImage()
	{
		return image;
	}
	
	
	public void setText(TextView text) {
		this.text = text;
		// TODO Auto-generated method stub
		
	}

	public TextView getText()
	{
		return text;
	}

	public void setItem(LauncherGridItem gridItem) {
		this.gridItem = gridItem;
		// TODO Auto-generated method stub
		
	}

	public LauncherGridItem getItem()
	{
		return this.gridItem;
	}

	public void setDragListener(DragListener listener) {
		this.listener = listener;

		
	}

	@Override
	public boolean canDelete() {
		// TODO Auto-generated method stub
		return true;
//		return canDelete;
	}

	public void canDelete(boolean canDelete) {
		
	}


}
