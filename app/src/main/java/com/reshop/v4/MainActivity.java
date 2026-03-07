package com.reshop.v4;

import android.animation.*;
import android.app.*;
import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
	
	private WebView webview1;
	
	private AlertDialog.Builder Exit;

	// ====================================================
	// CONFIG — GitHub Actions ئەمانە ئۆتۆماتیک دەگۆڕێت
	// ====================================================
	private static final String CONFIG_URL    = "https://raw.githubusercontent.com/haremmahmud/app-config/main/stores/default/config.json";
	private static final String FALLBACK_URL  = "https://hallyshop.v4.linkpc.net";
	// ====================================================
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		webview1 = findViewById(R.id.webview1);
		webview1.getSettings().setJavaScriptEnabled(true);
		webview1.getSettings().setSupportZoom(true);
		Exit = new AlertDialog.Builder(this);
		
		webview1.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
				final String _url = _param2;
				
				super.onPageStarted(_param1, _param2, _param3);
			}
			
			@Override
			public void onPageFinished(WebView _param1, String _param2) {
				final String _url = _param2;
				
				super.onPageFinished(_param1, _param2);
			}
		});
	}
	
	private void initializeLogic() {
		// ١. سپیکردنی بارەکە و باکگراوند لە چرکەی سفڕدا
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			    getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		}
		
		// ٢. ڕێکخستنی وێب ڤیو (WebView)
		webview1.getSettings().setJavaScriptEnabled(true);
		webview1.getSettings().setDomStorageEnabled(true);
		webview1.getSettings().setDatabaseEnabled(true);
		webview1.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		
		String originalUserAgent = webview1.getSettings().getUserAgentString();
		webview1.getSettings().setUserAgentString(originalUserAgent + " ReShop-App-Interface");
		
		// ٣. پشکنینی ئینتەرنێت و وەرگرتنی دۆمەین لە GitHub
		android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
		
		if (ni == null || !ni.isConnected()) {
			    android.content.Intent i = new android.content.Intent();
			    i.setClass(getApplicationContext(), NointernetActivity.class);
			    startActivity(i);
			    finish(); 
		} else {
			    // لێرەدا کۆدەکە دەچێت ناونیشانەکە لە GitHub دەهێنێت
			    new Thread(new Runnable() {
				        @Override
				        public void run() {
					            try {
						                java.net.URL url = new java.net.URL(CONFIG_URL);
						                java.util.Scanner s = new java.util.Scanner(url.openStream(), "UTF-8").useDelimiter("\\A");
						                String result = s.hasNext() ? s.next() : "";
						                
						                // دەرهێنانی لینکەکە لە ناو JSON
						                final String finalUrl = result.substring(result.indexOf("https://"), result.lastIndexOf("\""));
						
						                runOnUiThread(new Runnable() {
							                    @Override
							                    public void run() {
								                        webview1.loadUrl(finalUrl);
								                    }
							                });
						            } catch (Exception e) {
						                // ئەگەر کێشەیەک هەبوو لە GitHub، وەک یەدەگ ئەمە لۆد بکە
						                runOnUiThread(new Runnable() {
							                    @Override
							                    public void run() {
								                        webview1.loadUrl(FALLBACK_URL);
								                    }
							                });
						            }
					        }
				    }).start();
		}
		
		// ٤. بەڕێوەبردنی لۆدینگ و گۆڕینی ڕەنگ و کردنەوەی واتسئەپ
		webview1.setWebViewClient(new android.webkit.WebViewClient() {
			
			    @Override
			    public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
				        if (url.startsWith("whatsapp://") || url.contains("api.whatsapp.com") || url.contains("wa.me")) {
					            try {
						                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
						                intent.setData(android.net.Uri.parse(url));
						                startActivity(intent);
						                return true;
						            } catch (android.content.ActivityNotFoundException e) {
						                android.widget.Toast.makeText(getApplicationContext(), "ئەپی واتسئەپ لەسەر مۆبایلەکەت نییە", android.widget.Toast.LENGTH_SHORT).show();
						                return true;
						            }
					        }
				        return false;
				    }
			
			    @Override
			    public void onPageCommitVisible(android.webkit.WebView view, String url) {
				        super.onPageCommitVisible(view, url);
				        injectColorScript();
				    }
			
			    @Override
			    public void onPageFinished(android.webkit.WebView view, String url) {
				        super.onPageFinished(view, url);
				        injectColorScript();
				
				        new android.os.Handler().postDelayed(new Runnable() {
					            @Override
					            public void run() {
						                webview1.loadUrl("javascript:(function() { " +
						                    "var splash = document.getElementById('splash-screen');" +
						                    "if(splash) { splash.style.display='none'; }" +
						                    "})()");
						            }
					        }, 2500); 
				    }
			
			    private void injectColorScript() {
				        webview1.loadUrl("javascript:(function() {" +
				            "var header = document.querySelector('header') || document.querySelector('.header') || document.body;" +
				            "if(header) {" +
				            "  var color = window.getComputedStyle(header).backgroundColor;" +
				            "  if(color !== 'rgba(0, 0, 0, 0)' && color !== 'transparent') {" +
				                "    AndroidInterface.changeStatusBar(color);" +
				            "  }" +
				            "}" +
				            "})()");
				    }
			
			    @Override
			    public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
				        android.content.Intent i = new android.content.Intent();
				        i.setClass(getApplicationContext(), NointernetActivity.class);
				        startActivity(i);
				        finish();
				    }
		});
		
		// ٥. پردی پەیوەندی (Interface)
		webview1.addJavascriptInterface(new Object() {
			    @android.webkit.JavascriptInterface
			    public void changeStatusBar(final String color) {
				        runOnUiThread(new Runnable() {
					            @Override
					            public void run() {
						                try {
							                    int c;
							                    if (color.startsWith("rgb")) {
								                        String[] vals = color.replace("rgb(", "").replace("rgba(", "").replace(")", "").split(",");
								                        c = Color.rgb(Integer.parseInt(vals[0].trim()), Integer.parseInt(vals[1].trim()), Integer.parseInt(vals[2].trim()));
								                    } else {
								                        c = Color.parseColor(color);
								                    }
							
							                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
								                        getWindow().setStatusBarColor(c);
								                        if (isColorLight(c) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
									                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
									                        } else {
									                            getWindow().getDecorView().setSystemUiVisibility(0);
									                        }
								                    }
							                } catch (Exception e) {}
						            }
					        });
				    }
			
			    private boolean isColorLight(int color) {
				        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
				        return darkness < 0.5;
				    }
			
			    @android.webkit.JavascriptInterface
			    public void closeApp() {
				        runOnUiThread(new Runnable() {
					            @Override
					            public void run() {
						                android.app.AlertDialog.Builder x = new android.app.AlertDialog.Builder(MainActivity.this);
						                x.setTitle("ئاگاداری");
						                x.setMessage("ئایا دەتەوێت لە ئەپەکە بچیتە دەرەوە؟");
						                x.setPositiveButton("بەڵێ", new android.content.DialogInterface.OnClickListener() {
							                    @Override
							                    public void onClick(android.content.DialogInterface dialog, int which) {
								                        finishAffinity();
								                    }
							                });
						                x.setNegativeButton("نەخێر", null);
						                x.show();
						            }
					        });
				    }
		}, "AndroidInterface");
		
	}
	
	@Override
	public void onBackPressed() {
		// یەکەم جار هەوڵ دەدات بە مێژووی وێبڤیو بگەڕێتەوە
		if (webview1.canGoBack()) {
			    webview1.goBack();
		} else {
			    // ئەگەر مێژوو نەمابوو، فەرمان دەنێرێت بۆ سایتەکە کە ئایا پەیجێکی تر کراوەیە یان نا
			    webview1.loadUrl("javascript:(function() { " +
			        "var currentPage = document.querySelector('.page.active-page'); " +
			        "if(currentPage && currentPage.id !== 'home-page') { " +
			            "showPage('home-page'); " + // ئەگەر لە ناو پەیجێکی تر بوو، دەگەڕێتەوە هۆم
			        "} else { " +
			            "AndroidInterface.closeApp(); " + // ئەگەر لە هۆم بوو، پەیامی داخستن نیشان دەدات
			        "}" +
			    "})()");
		}
		
	}
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}
