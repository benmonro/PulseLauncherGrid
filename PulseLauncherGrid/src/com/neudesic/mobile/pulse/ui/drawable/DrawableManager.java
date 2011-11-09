package com.neudesic.mobile.pulse.ui.drawable;



/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.    
 */
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;



public class DrawableManager {
	private static final int MAX_CACHE_SIZE = 10;
	private final Map<String, Drawable> drawableMap;
	private Drawable emptyAvatar;
	private final Context context;
	private String imageSizeString;
	private DefaultHttpClient httpClient;

	//private PulseServiceConnection pulse;

	public DrawableManager(Context context) {
		this.context = context;

		drawableMap = new HashMap<String, Drawable>();

		
		//change this to whatever avatar you want as the default loading avatar.
		emptyAvatar = context.getResources().getDrawable(android.R.drawable.ic_menu_view);



	}

	public Drawable fetchDrawable(String urlString) {
		if (drawableMap.containsKey(urlString)) {
			Log.d(getClass().getSimpleName(), "loading image from localcache");
			return drawableMap.get(urlString);
		}

		Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
		try {
			InputStream is = fetch(urlString);

			Drawable drawable = Drawable.createFromStream(is, "src");
			if (drawableMap.size() > MAX_CACHE_SIZE) {
				drawableMap.clear();
			}
			drawableMap.put(urlString, drawable);
			Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: "
					+ drawable.getBounds() + ", "
					+ drawable.getIntrinsicHeight() + ","
					+ drawable.getIntrinsicWidth() + ", "
					+ drawable.getMinimumHeight() + ","
					+ drawable.getMinimumWidth());
			return drawable;
		} catch (MalformedURLException e) {
			Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
			return null;
		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
			return null;
		}
	}

	public void fetchDrawableOnThread(final String urlString,
			final ImageView imageView) {

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				try {
					imageView.setImageDrawable((Drawable) message.obj);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		Thread thread = new Thread() {

			@Override
			public void run() {
				// TODO : set imageView to a "pending" image
				try {
					Drawable drawable;
					if (drawableMap.containsKey(urlString)) {
						// imageView.setImageDrawable(drawableMap.get(urlString));
						drawable = drawableMap.get(urlString);
				
					} else {

						drawable = fetchDrawable(urlString);
					}
					Message message = handler.obtainMessage(1, drawable);
					handler.sendMessage(message);
				} catch (Exception e) {
					Log.w(getClass().getPackage().getName(),
							"Unable to fetch drawable, using default instead",
							e);
					Message message = handler.obtainMessage(1, emptyAvatar);
					handler.sendMessage(message);
				}
			}
		};
		thread.start();
	}

	public Bitmap fetchBitmapImage(String imageUrl)
			throws FileNotFoundException {
		InputStream is = null;
		BufferedInputStream buf = null;


		try {
			is = (InputStream) fetchImage(imageUrl);
			buf = new BufferedInputStream(is);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bitmap bmp = null;
		BitmapFactory.Options ops = new BitmapFactory.Options();
		ops.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeStream(buf, null, ops);

		return bmp;
	}

	private InputStream fetch(String urlString) throws MalformedURLException,
			IOException {
		if(this.getHttpClient() == null)
		{
			setHttpClient(new DefaultHttpClient());
		}
		HttpGet request = new HttpGet(urlString);
		HttpResponse response = getHttpClient().execute(request);
		return response.getEntity().getContent();
	}

	public Object fetchImage(String address) throws MalformedURLException,
			IOException {
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
	}



	public DefaultHttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(DefaultHttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
