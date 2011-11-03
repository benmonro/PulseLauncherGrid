package com.neudesic.mobile.pulse.ui.launcher;

import java.util.List;

public class LauncherGridItemList {

	private List<LauncherGridItem> items;
	public LauncherGridItemList()
	{
		
	}
	public LauncherGridItemList(List<LauncherGridItem> items) {
		this.setItems(items);
		// TODO Auto-generated constructor stub
	}

	public List<LauncherGridItem> getItems() {
		return items;
	}

	public void setItems(List<LauncherGridItem> items) {
		this.items = items;
	}

}
