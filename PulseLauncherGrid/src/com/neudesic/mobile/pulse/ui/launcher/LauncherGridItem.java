package com.neudesic.mobile.pulse.ui.launcher;

import java.net.URISyntaxException;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.content.Intent;

public class LauncherGridItem {
	private String caption;
	private int drawableId;
	private String imageUrl = null;
	// private Intent intent;
	private boolean deletable;
	private String intentUrl;

	public LauncherGridItem() {
	}

	public LauncherGridItem(String imageUrl, String caption) {
		this.imageUrl = imageUrl;
		this.caption = caption;

	}

	public LauncherGridItem(int drawableId, String caption) {
		this.drawableId = drawableId;
		this.caption = caption;
		// TODO Auto-generated constructor stub
	}

	public LauncherGridItem(Integer drawable, String caption, Intent intent) {
		this.drawableId = drawable;
		// TODO Auto-generated constructor stub
		this.caption = caption;
		// this.intent = intent;
		this.setIntentUrl(intent.toUri(Intent.URI_INTENT_SCHEME).toString());
		this.setDeletable(false);
	}

	public LauncherGridItem(String imageUrl, String caption, Intent intent) {
		this.imageUrl = imageUrl;
		this.caption = caption;
		this.setIntentUrl(intent.toUri(Intent.URI_INTENT_SCHEME).toString());
		// this.intent = intent;
		this.setDeletable(false);
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public int getDrawable() {
		return drawableId;
	}

	public void setDrawable(int drawable) {
		this.drawableId = drawable;
	}

	public String getUrl() {
		return imageUrl;
	}

	public void setUrl(String url) {
		this.imageUrl = url;
	}

	@JsonIgnore
	public Intent getIntent() {
		try {
			return Intent.parseUri(getIntentUrl(), Intent.URI_INTENT_SCHEME);
		} catch (URISyntaxException e) {
			return new Intent();
		}
	}

	@JsonIgnore
	public void setIntent(Intent intent) {
		this.setIntentUrl(intent.toUri(Intent.URI_INTENT_SCHEME));
	}

	public void canDelete(boolean canDelete) {
		this.setDeletable(canDelete);
		// TODO Auto-generated method stub

	}

	public boolean canDelete() {
		return this.isDeletable();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LauncherGridItem) {
			LauncherGridItem other = (LauncherGridItem) o;
			try {
				Intent a = Intent.parseUri(this.intentUrl,
						Intent.URI_INTENT_SCHEME);

				Intent b = Intent.parseUri(other.intentUrl,
						Intent.URI_INTENT_SCHEME);
				if (a.filterEquals(b)) {
					if (a.getExtras() != null && b.getExtras() != null) {
						for (String key : a.getExtras().keySet()) {
							if (!b.getExtras().containsKey(key)) {
								return false;
							} else if (!a.getExtras().get(key)
									.equals(b.getExtras().get(key))) {
								return false;

							}
						}
					}
					// all of the extras are the same so return true
					return true;
				}// &&
					// this.intent.toUri(Intent.URI_INTENT_SCHEME).equalsIgnoreCase(other.intent.toUri(Intent.URI_INTENT_SCHEME));

			} catch (URISyntaxException e) {
				return false;
			}
			// return
			// this.getIntentUrl().equalsIgnoreCase(other.getIntentUrl());
			// return this.intent.filterEquals(other.intent) &&
			// this.intent.toUri(Intent.URI_INTENT_SCHEME).equalsIgnoreCase(other.intent.toUri(Intent.URI_INTENT_SCHEME));

		}

		return false;
	}

	public String getIntentUrl() {
		return intentUrl;
	}

	public void setIntentUrl(String intentUrl) {
		this.intentUrl = intentUrl;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}
}