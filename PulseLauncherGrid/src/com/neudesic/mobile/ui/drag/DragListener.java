package com.neudesic.mobile.ui.drag;

import android.view.View;

public interface DragListener {

	void onDragStarted(View source);
	void onDropCompleted(View source, View target, boolean success);

}
