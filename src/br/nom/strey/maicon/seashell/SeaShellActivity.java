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
	final static Integer MY_VOL = 10; 	// inicializada com volume padrão das ondas
 
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
    	myAudioManager.setMode(AudioManager.MODE_IN_CALL); // configura saída de áudio para p speaker, ou saída de "conversação".
    	
    	// salva volume do aparelho somente quando o app é carregado para não ser sobrescrito pelo volume da app.
    	// a variável é iniciada em 20 para saber que é a primeira execução;
    	// o valor do volume varia entre 0 e 15.
        if (userVolumn == 20) {
        	userVolumn = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } else {
        	// o programa já está em memória e não é necessário salvar novamente o volume.
        }
        
        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MY_VOL, AudioManager.MODE_NORMAL);
        Log.i("MEDIA_VOLUME", "save current: "+userVolumn+" and set to : "+MY_VOL);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        wavePlayer = MediaPlayer.create(getApplicationContext(), R.raw.bg); // cria um player
        wavePlayer.setLooping(true);
        wavePlayer.setVolume(1.0f, 1.0f); // volume do player no máximo
        isPlayingWaveSounds = false; // controla se o player está rodando
        
        mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY); // cria instancia do sensor de proximidade
        
        // alterada ordem de testes pois a maioria dos aparelhos disponíves possui sensor de proximidade.
        if (myProximitySensor != null){ // verifica existência do sensor de proximidade no aparelho
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
	         alert.show(); // exibe mensagem informando que sem sensor o programa não funciona
        }
        
        if (isConnected(getBaseContext())){ // se estiver conectado exibe anuncio do AdMob
            AdView adView = (AdView)findViewById(R.id.adView);
            // Carrega um anúncio genérico para testes que não gera monetização.
            //adView.loadAd(new AdRequest().addTestDevice("A783C1868D1441A3CC47E7681566D639"));
            
            // carrega anúncio válido que gera monetização
            adView.loadAd(new AdRequest());
        } else {
        	// não existe conexão e não tem como exibir propaganda.
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
			  // ProximitySensor.getMaximumRange() retorna a distância máxima que o sensor pode capturar.
			  // Quando o sensor retornar a distância máxima considera-se que nada está próximo.
			  // Alterada ordem de testes pois durante a execução do app o aparelho passa mais tempo 
			  // perto da orelha do que longe. Assim que o usuário afasta da orelha ele fecha o app.
			  
			  final boolean isCloser = myProximitySensor.getMaximumRange() != event.values[0];
			  // incluida a variável acima para tornar mais fácil a compreensão
			  if (isCloser){ 
				  // quando detecta proximidade
			      
				  // verifica se existe fone de ouvido conectado
				  IntentFilter ifPlugPhone = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
			      boolean phoneIsPluged = ifPlugPhone.getAction(0).equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG);
			      
			      // Se existir algum fone conectado desativa saída de áudio pelo speaker
			      // Se não existir um fone plugado ativa saída de áudio pelo speaker
			      myAudioManager.setSpeakerphoneOn(!phoneIsPluged);
			      
			      if (!isPlayingWaveSounds){
			    	  wavePlayer.start();
			      } else {
			    	  // player já está executando
			      }
			      
			      isPlayingWaveSounds = true;
			  } else {
				  // quando não detecta proximidade
				  
				  if (isPlayingWaveSounds){
					  wavePlayer.pause();
				  } else {
					  //player já está pausado.
				  }
				  
				  isPlayingWaveSounds = false;
			  }
		 }
    };
    
    // função renomeada de sair() para deixar mais claro objetivo.
    private void exitApp(){
    	
    	wavePlayer.stop();
    	wavePlayer.reset();
    	wavePlayer.release();
    	isPlayingWaveSounds = false;
    	
    	// retorna o volume do aparelho para o volume antes de executar o app.
    	myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolumn, AudioManager.MODE_NORMAL);
        Log.i("MEDIA_VOLUME", "set to previous: "+userVolumn);
        myAudioManager.setSpeakerphoneOn(false);
        
        // muito importante fazer o unregister para o listener não 
        // continuar executando após fechar o app
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
 
   // função renomeada de Conectado() para deixar mais claro objetivo.
   public static boolean isConnected(Context context) {
	   
       try {
           ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
           
           // alterada ordem dos testes de conexão pois acredita-se que é mais provável que o usuários esteja conectado através de WIFI.
           // incluída duas novas variáveis para tornar melhor a compreensão do código
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