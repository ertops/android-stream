package com.ertops.stream.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R.string;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	
	public class WVC extends WebViewClient {

		// yeah well, go cleanup the twitter site ~
		@Override
		public void onPageFinished(WebView view, String url) {
			view.loadUrl("javascript:(function() { "
					+ "console.log(document, document.querySelector('.signedout-header'),document.querySelector('#views'), document.querySelector('#views').x.previousElementSibling);"
					+ "header = document.querySelector('.signedout-header') || document.querySelector('header'); "
					+ "if (header) { header.remove(); views = document.querySelector('#views'); if (views) views.style.paddingTop=0; }"
					+ "console.log(document, document.querySelector('.signedout-header'),document.querySelector('#views'), document.querySelector('#views').x.previousElementSibling); })()");
		}

		public void onConsoleMessage(String message, int lineNumber,
				String sourceID) {
			Log.d("MyApplication", message + " -- From line " + lineNumber
					+ " of " + sourceID);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.e("Browse", url);
			if (!url.contains("mobile.twitter.com/ErtSocial")
					&& !url.contains("twitter.com/search?q=%23ert")
					&& !url.contains("www.ertopen.com")) {
				// return super.shouldOverrideUrlLoading(view, url); // Leave
				// webview and use browser
				Log.e("url", url);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				return true;
			} else {
				view.loadUrl(url); // Stay within this webview and load url
				return true;
			}
		}
	}

	public class TVC {
		public int type;
		public int name_res;
		public String params;
		public View v;
		
		public TVC(int type, int name_res, String params) {
			this.type = type;
			this.name_res = name_res;
			this.params = params;
			this.v = null;
		}
	}
	
	public class VPA extends PagerAdapter {

		ViewPager vp;
		PagerTitleStrip pts;
		Context ctx;
		final TVC[] views = new TVC[] {
				new TVC(0,R.string.tabbar_title_streams,""),
				new TVC(1,R.string.tabbar_title_ertopen,"http://www.ertopen.com/?novideo=1"),
				new TVC(1,R.string.tabbar_title_news,"https://twitter.com/ErtSocial"),
				new TVC(1,R.string.tabbar_title_chatter,"https://twitter.com/search?q=%23ert"),
				};
		

		public VPA(Context ctx) { 
			this.ctx = ctx;
			views[0].params = ctx.getString(R.string.streams_list);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			TVC vt = views[position];
			
			switch (vt.type) {
			case 0:
				{
					final View element = LayoutInflater.from(getBaseContext())
							.inflate(R.layout.streams, container, false);
					container.addView(element);

					final ListView listview = (ListView) element.findViewById(R.id.listview);
					final Button btn = (Button) element.findViewById(R.id.playbar_button);
					final TextView txt = (TextView) element.findViewById(R.id.playbar_text);

					txt.setText(R.string.playbar_text_idle);

					btn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							stopService(new Intent(VPA.this.ctx, Player.class));
							removeNotification();
							txt.setText(R.string.playbar_text_idle);
						}
					});
					final String[] values = vt.params.split("\\|\\|");

					final ArrayList<String> list = new ArrayList<String>();
					for (int i = 0; i < values.length; ++i) {
						list.add(values[i].split("\\|")[0]);
					}
					final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							ctx, android.R.layout.simple_list_item_1, list);
					listview.setAdapter(adapter);

					listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								final View view, int position, long id) {

							Intent it = new Intent(VPA.this.ctx, Player.class);
							String name = values[position].split("\\|")[0];
							String url = values[position].split("\\|")[1];
							
							it.putExtra("name", name);
							it.putExtra("url", url);
							startService(it);
							addNotification(name);
							

							txt.setText(name);
						}

					});

					return vt.v = element;
				}
				
			case 1:
				{
					WebView view = new WebView(ctx);
					view.setWebViewClient(new WVC());
					view.getSettings().setJavaScriptEnabled(true);

					container.addView(view);
					view.loadUrl(vt.params);

					return vt.v = view;
				}
			}
			
			return null;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return ctx.getString(views[position].name_res);
		}

		@Override
		public int getCount() {
			return views.length;
		}
	}

	private static final int FM_NOTIFICATION_ID = 0;

	VPA adapter;
	ViewPager vp;
	PagerTitleStrip pts;

	// Add app running notification  

    private void addNotification(String text) {
	    NotificationCompat.Builder builder =  
	            new NotificationCompat.Builder(this)  
	            .setSmallIcon(R.drawable.ic_launcher)  
	            .setContentTitle(text)  
	            ;  
	
	    Intent notificationIntent = new Intent(this, MainActivity.class);  
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,   
	            PendingIntent.FLAG_UPDATE_CURRENT);  
	    builder.setContentIntent(contentIntent);  
	
	    // Add as notification  
	    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
	    manager.notify(FM_NOTIFICATION_ID, builder.build());
	}  
	
	// Remove notification  
	private void removeNotification() {  
	    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
	    manager.cancel(FM_NOTIFICATION_ID);  
	}  

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		adapter = new VPA(this);
		pts = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		vp = (ViewPager) findViewById(R.id.pager);

		vp.setOffscreenPageLimit(10);
		vp.setAdapter(adapter);
		vp.setCurrentItem(0);
	}

	@Override
	public void onBackPressed() {
		View v = adapter.views[vp.getCurrentItem()].v;

		if (v != null && v instanceof WebView) {
			WebView wv = (WebView) v;
			if (wv.canGoBack()) {
				wv.goBack();
				return;
			}
		}
		
		super.onBackPressed();
	}
}