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
	Integer userVolumn = 20;			// inicializada com valor inv�lido (m�ximo 15)
	final static Integer MY_VOL = 10; 	// inicializada com volume padr�o das ondas
 
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
    	myAudioManager.setMode(AudioManager.MODE_IN_CALL); // configura sa�da de �udio para p speaker, ou sa�da de "conversa��o".
    	
    	// salva volume do aparelho somente quando o app � carregado para n�o ser sobrescrito pelo volume da app.
    	// a vari�vel � iniciada em 20 para saber que � a primeira execu��o;
    	// o valor do volume varia entre 0 e 15.
        if (userVolumn == 20) {
        	userVolumn = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } else {
        	// o programa j� est� em mem�ria e n�o � necess�rio salvar novamente o volume.
        }
        
        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MY_VOL, AudioManager.MODE_NORMAL);
        Log.i("MEDIA_VOLUME", "save current: "+userVolumn+" and set to : "+MY_VOL);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        wavePlayer = MediaPlayer.create(getApplicationContext(), R.raw.bg); // cria um player
        wavePlayer.setLooping(true);
        wavePlayer.setVolume(1.0f, 1.0f); // volume do player no m�ximo
        isPlayingWaveSounds = false; // controla se o player est� rodando
        
        mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY); // cria instancia do sensor de proximidade
        
        // alterada ordem de testes pois a maioria dos aparelhos dispon�ves possui sensor de proximidade.
        if (myProximitySensor != null){ // verifica exist�ncia do sensor de proximidade no aparelho
        	// cria um listener para o sensor de proximidade 
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
	         alert.show(); // exibe mensagem informando que sem sensor o programa n�o funciona
        }
        
        if (isConnected(getBaseContext())){ // se estiver conectado exibe anuncio do AdMob
            AdView adView = (AdView)findViewById(R.id.adView);
            // Carrega um an�ncio gen�rico para testes que n�o gera monetiza��o.
            //adView.loadAd(new AdRequest().addTestDevice("A783C1868D1441A3CC47E7681566D639"));
            
            // carrega an�ncio v�lido que gera monetiza��o
            adView.loadAd(new AdRequest());
        } else {
        	// n�o existe conex�o e n�o tem como exibir propaganda.
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
			  
			  // event.values[0] retorna a proximidade da orelha ao sensor em centimetros.
			  // ProximitySensor.getMaximumRange() retorna a dist�ncia m�xima que o sensor pode capturar.
			  // Quando o sensor retornar a dist�ncia m�xima considera-se que nada est� pr�ximo.
			  // Alterada ordem de testes pois durante a execu��o do app o aparelho passa mais tempo 
			  // perto da orelha do que longe. Assim que o usu�rio afasta da orelha ele fecha o app.
			  
			  final boolean isCloser = myProximitySensor.getMaximumRange() != event.values[0];
			  // incluida a vari�vel acima para tornar mais f�cil a compreens�o
			  if (isCloser){ 
				  // quando detecta proximidade
			      
				  // verifica se existe fone de ouvido conectado
				  IntentFilter ifPlugPhone = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
			      boolean phoneIsPluged = ifPlugPhone.getAction(0).equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG);
			      
			      // Se existir algum fone conectado desativa sa�da de �udio pelo speaker
			      // Se n�o existir um fone plugado ativa sa�da de �udio pelo speaker
			      myAudioManager.setSpeakerphoneOn(!phoneIsPluged);
			      
			      if (!isPlayingWaveSounds){
			    	  wavePlayer.start();
			      } else {
			    	  // player j� est� executando
			      }
			      
			      isPlayingWaveSounds = true;
			  } else {
				  // quando n�o detecta proximidade
				  
				  if (isPlayingWaveSounds){
					  wavePlayer.pause();
				  } else {
					  //player j� est� pausado.
				  }
				  
				  isPlayingWaveSounds = false;
			  }
		 }
    };
    
    // fun��o renomeada de sair() para deixar mais claro objetivo.
    private void exitApp(){
    	
    	wavePlayer.stop();
    	wavePlayer.reset();
    	wavePlayer.release();
    	isPlayingWaveSounds = false;
    	
    	// retorna o volume do aparelho para o volume antes de executar o app.
    	myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolumn, AudioManager.MODE_NORMAL);
        Log.i("MEDIA_VOLUME", "set to previous: "+userVolumn);
        myAudioManager.setSpeakerphoneOn(false);
        
        // muito importante fazer o unregister para o listener n�o 
        // continuar executando ap�s fechar o app
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
 
   // fun��o renomeada de Conectado() para deixar mais claro objetivo.
   public static boolean isConnected(Context context) {
	   
       try {
           ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
           
           // alterada ordem dos testes de conex�o pois acredita-se que � mais prov�vel que o usu�rios esteja conectado atrav�s de WIFI.
           // inclu�da duas novas vari�veis para tornar melhor a compreens�o do c�digo
           final boolean isWifiConnected = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected(); 
           final boolean isMobileConnected = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected(); 

           if (isWifiConnected) {
           
        	   return true;
           
           } else if(isMobileConnected){
        	   
               return true;
               
           } else {
                   
        	   Log.e("isConnected","Status de conex�o Wifi: "+isWifiConnected);
               Log.e("isConnected","Status de conex�o 3G: "+isMobileConnected);
               return false;
               
           }
       } catch (Exception e) {
               e.printStackTrace();
               return false;
       }
   }
}