package hu.chriscc.keepmeawake;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class WakeService extends Service {
	private static final String TAG = "MyService";
	private NotificationManager mNotificationManager;
	MediaPlayer player;
    
	//private int KeepMeAwakeActivity.lastMoved = 0; 
	private Runnable m_statusChecker;
    private boolean m_statusCheck = true;
    private float treshold = 0.2f;
    private Handler m_handler;
    private Context CONTEXT;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		//Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		player = MediaPlayer.create(this, R.raw.braincandy);
		player.setLooping(false); // Set looping
		m_handler = new Handler();
		CONTEXT = this.getApplicationContext();
		m_statusChecker = new Runnable()
    	{
    	     @Override 
    	     public void run() {
    	          try{
    	        	  if(!m_statusCheck) return;
    	        	  int now = (int) System.currentTimeMillis();
    	        	  
    	        	  //Log.i("polysleep","Checking "+now+" vs. "+KeepMeAwakeActivity.lastMoved);
    	        	  
    	        	  if(now-KeepMeAwakeActivity.lastMoved > 50000){
    	        		  //Log.i("polysleep","Too long no movement!");
    	        		  Vibrator v = (Vibrator) CONTEXT.getSystemService(CONTEXT.VIBRATOR_SERVICE);

	    	        	  // 1. Vibrate for 1000 milliseconds
    	        		  //Log.i("polysleep","Vibrating...");
    	        		  PowerManager pm = (PowerManager) CONTEXT.getSystemService(Context.POWER_SERVICE);
    	        		  PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
    	        		  wl.acquire();
    	        		  //  ..screen will stay on during this section..
	    	        	  long milliseconds = 1500;
	    	        	  treshold = 5;
	    	        	  v.vibrate(milliseconds);
	    	        	  
	    	        	  player.start();
	    	        	  Runnable r = new Runnable(){
	    	        		  @Override 
	    	         	      public void run() {
	    	        			  //Log.i("polysleep","Vibrate ended");
	    	        			 treshold = 0.2f; 
	    	        			 
	    	        			 try{
	    	        			 player.pause();
    	        				 player.seekTo(0);
	    	        			 }catch(Exception e){}
	    	        		  }
	    	        	  };
	    	        	  boolean th = m_handler.postDelayed(r,1800);
	    	        	  wl.release();

    	        		 // mainBlock.setBackgroundColor(Color.parseColor("#c80000"));
    	        		 // sleepStatus.setText("WAKE UP!");
    	        		 // sleepStatus.setTextColor(Color.WHITE);
    	        	  }else{
    	        		 // mainBlock.setBackgroundColor(Color.parseColor("#00c800"));
    	        		 // sleepStatus.setText("OK, you seem to be still awake...");
    	        		 // sleepStatus.setTextColor(Color.BLACK);
    	        	  }
    	          }catch(Exception e){
    	        	  //Log.i("polysleep","Hiba: "+e.getMessage());
    	          }
    	          if(m_statusCheck) {
					boolean postDelayed = m_handler.postDelayed(m_statusChecker, 5000);
				}
    	     }
    	};
	}

	@Override
	public void onDestroy() {
		//Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		mNotificationManager.cancelAll();
		m_statusCheck = false;
		if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
        }
		Log.d(TAG, "onDestroy");
		player.stop();
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		//Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.notif_icon;        // icon from resources
		CharSequence tickerText = "Watching you...";              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		Context context = getApplicationContext();      // application Context
		CharSequence contentTitle = "KeepMeAwake Active!";  // message title
		CharSequence contentText = "Watching you...";      // message text
		Intent notificationIntent = new Intent(this, KeepMeAwakeActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		
		mNotificationManager.notify(1,notification);
		Log.d(TAG, "onStart");
		
		if (AccelerometerManager.isSupported() || !AccelerometerManager.isListening()) {
            AccelerometerManager.startListening(KeepMeAwakeActivity.CONTEXT);
        }
		
		m_statusCheck = true;
    	m_handler.postDelayed(m_statusChecker, 1000);
		
	}
}