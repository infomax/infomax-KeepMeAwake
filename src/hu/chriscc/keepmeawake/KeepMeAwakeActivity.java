package hu.chriscc.keepmeawake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class KeepMeAwakeActivity extends Activity implements AccelerometerListener  {
	  private static final String TAG = "ServicesDemo";
	  Button buttonStart, buttonStop;
	  public static KeepMeAwakeActivity CONTEXT;
	  private float[] buff_x = new float[5];
	  private float[] buff_y = new float[5];
	  private float[] buff_z = new float[5];
	  private int ptr = 0;
	  private boolean inited;
	  private float diff_avg = 0;
	  private float treshold = 0.2f;
	  public static int lastMoved = 0; 

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    CONTEXT = this;

	    buttonStart = (Button) findViewById(R.id.buttonStart);
	    buttonStop = (Button) findViewById(R.id.buttonStop);

	    buttonStart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick: starting srvice");
			      startService(new Intent(KeepMeAwakeActivity.this, WakeService.class));
			}
	    });
	    buttonStop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onClick: starting srvice");
			      stopService(new Intent(KeepMeAwakeActivity.this, WakeService.class));
			}
	    });
	  }
	  
	  @Override
	  protected void onDestroy(){
		  super.onDestroy();
		  CONTEXT = null;
		  buff_x = null;
		  buff_y = null;
		  buff_z = null;
		  inited = false;
		  lastMoved = 0;
		  stopService(new Intent(KeepMeAwakeActivity.this, WakeService.class));
	  }

	@Override
	public void onAccelerationChanged(float x, float y, float z) {
		// TODO Auto-generated method stub
		buff_x[ptr] = x;
    	buff_y[ptr] = y;
    	buff_z[ptr] = z;
    	ptr++;
    	
    	
    	if(inited){
    		int i=0;
    		for(i=0;i<4;i++){
    			if(Math.abs(x-buff_x[i])>treshold || Math.abs(y-buff_y[i])>treshold || Math.abs(z-buff_z[i])>treshold){
    				lastMoved = (int) System.currentTimeMillis();
    				Log.i("Accel","Phone moved at "+lastMoved);
    			}
    		}
    	}

    	if(ptr==4){
    		if(!inited){
    			inited = true;
    			lastMoved = (int) System.currentTimeMillis();
    		}
    		ptr=0;
    	}
	}

	@Override
	public void onShake(float force) {
		// TODO Auto-generated method stub
		
	}
}