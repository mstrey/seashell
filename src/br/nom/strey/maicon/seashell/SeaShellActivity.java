package br.nom.strey.maicon.seashell;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class SeaShellActivity extends Activity {
	/** Called when the activity is first created. */
 
	MediaPlayer wavePlayer;
	AudioManager myAudioManager;
	SensorManager mySensorManager;
	Sensor myProximitySensor;
	Boolean isPlayingWaveSounds;
	Integer userVolumn = 20;			// inicializada com valor inválido (máximo 15)
	final static Integer MY_VOL = 10; 	// inicializada com volume padrão do áudio das ondas
 
 	public void onStart(){
 		super.onStart();
 		principal();
 	}
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
	private void principal(){
    	setContentView(R.layout.activity_main);
        
    	myAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    	myAudioManager.setMode(AudioManager.MODE_IN_CALL); // configura saída de áudio para o speaker, ou saída de "conversação".
    	
    	// salva volume do aparelho somente quando o app é carregado para não ser sobrescrito pelo volume da app.
    	// a variável foi iniciada em 20 para saber que é a primeira execução;
    	// o valor do volume varia entre 0 e 15.
        if (userVolumn == 20) {
        	userVolumn = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        
        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MY_VOL, AudioManager.MODE_NORMAL);
        Log.i("MEDIA_VOLUME", "save current: "+userVolumn+" and set to : "+MY_VOL);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        wavePlayer = MediaPlayer.create(getApplicationContext(), R.raw.bg);
        wavePlayer.setLooping(true);
        wavePlayer.setVolume(1.0f, 1.0f); // configura o volume do player no máximo
        isPlayingWaveSounds = false;
        
        mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        
        if (myProximitySensor != null){
	        mySensorManager.registerListener(proximitySensorListener,
	        		 myProximitySensor,
	        		 SensorManager.SENSOR_DELAY_NORMAL);
        } else {
	         AlertDialog.Builder builder = new AlertDialog.Builder(this);
	         
	         builder.setMessage(R.string.NoSensor)
	                .setCancelable(false)
	                .setPositiveButton(R.string.Close, new DialogInterface.OnClickListener() {
	                	
	                	public void onClick(DialogInterface dialog, int id) {
	                    	finish();
	                    }
	                	
	                });
	         
	         AlertDialog alert = builder.create();
	         alert.show();
        }
        
        if (isConnected(getBaseContext())){ // se estiver conectado exibe anuncio do AdMob
            AdView adView = (AdView)findViewById(R.id.adView);
            // Carrega um anúncio genérico para testes que não gera monetização.
            //adView.loadAd(new AdRequest().addTestDevice("A783C1868D1441A3CC47E7681566D639"));
            
            // carrega anúncio válido que gera monetização
            adView.loadAd(new AdRequest());
        }
    }
    
    SensorEventListener proximitySensorListener = new SensorEventListener(){

		  @Override
		  public void onAccuracyChanged(Sensor sensor, int accuracy) {
		   // TODO Auto-generated method stub
		   
		  }
		
		  @Override
		  public void onSensorChanged(SensorEvent event) {
		   // TODO Auto-generated method stub
			  
			  final boolean isCloser = myProximitySensor.getMaximumRange() != event.values[0];
			  if (isCloser){
				  // verifica se existe fone de ouvido conectado
				  IntentFilter ifPlugPhone = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
			      boolean phoneIsPluged = ifPlugPhone.getAction(0).equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG);
			      
			      // Se existir algum fone conectado desativa saída de áudio pelo speaker
			      // Se não existir um fone plugado ativa saída de áudio pelo speaker
			      myAudioManager.setSpeakerphoneOn(!phoneIsPluged);
			      
			      if (!isPlayingWaveSounds){
			    	  wavePlayer.start();
			      }
			      
			      isPlayingWaveSounds = true;
			  } else {
				  // quando não detecta proximidade
				  
				  if (isPlayingWaveSounds){
					  wavePlayer.pause();
				  }
				  
				  isPlayingWaveSounds = false;
			  }
		 }
    };
    
    private void exitApp(){
    	
    	wavePlayer.stop();
    	wavePlayer.reset();
    	wavePlayer.release();
    	isPlayingWaveSounds = false;
    	
    	// retorna o volume do aparelho para o volume antes de executar o app.
    	myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolumn, AudioManager.MODE_NORMAL);
        Log.i("MEDIA_VOLUME", "set to previous: "+userVolumn);
        myAudioManager.setSpeakerphoneOn(false);
        
    	mySensorManager.unregisterListener(proximitySensorListener);
   }

    public void onStop() {
 	   // TODO Auto-generated method stub
 	   super.onStop();
    }
     
   public void onDestroy(){
	   super.onDestroy();
	   exitApp();
   }
 
   public static boolean isConnected(Context context) {
	   
       try {
           ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
           
           final boolean isWifiConnected = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
           final boolean isMobileConnected = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected(); 

           if (isWifiConnected) {
           
        	   return true;
           
           } else if(isMobileConnected){
        	   
               return true;
               
           } else {
                   
        	   Log.e("isConnected","Status de conexão Wifi: "+isWifiConnected);
               Log.e("isConnected","Status de conexão 3G: "+isMobileConnected);
               return false;
               
           }
       } catch (Exception e) {
               e.printStackTrace();
               return false;
       }
   }
}