package com.example.quizmachine;
import java.util.ArrayList;

import com.example.quizmachine.R;

import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final String TAG = "MainActivity";

	// プレイヤーリスト
	ArrayList<String> playerList;

	// 正解、誤答キー（キーコード）
	private int correctKey;
	private int wrongKey;

	// 効果音
	private SoundPool sp;
	private int soundIdCorrect;
	private int soundIdWrong;
	private int soundIdPush;
	private float volumeRate;
	// 再生中フラグ
	private boolean isPlayingWrongSound = false;
	private int streamIdWrong;

	// クイズの状態
	private boolean buttonIsPushed = false;
	private String pushPlayer = null;

	// キーコードとプレイヤーの対応
	private SparseArray<String> keycodeMap;

	// 押した人を表示するTextView
	TextView pushedTextView;

	// startActivityForResultのrequestCode
	static final int REQUEST_CODE = 111;

	private float getVolumeRate(){
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_RING);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
		float volumeRate_ = (float)currentVolume / maxVolume;
		return volumeRate_;
	}

	// 効果音のロード
	private void loadSound(){
		volumeRate = getVolumeRate();
		sp = new SoundPool( 5, AudioManager.STREAM_MUSIC, 0 );
		sp.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				if (0 == status) {
					Toast.makeText(getApplicationContext(), "音声読み込み完了", Toast.LENGTH_LONG).show();
				}
			}
		});

		// 効果音の読み込み
		soundIdPush = sp.load(this, R.raw.push, 1);
		soundIdCorrect = sp.load(this, R.raw.correct, 1);
		soundIdWrong = sp.load(this, R.raw.wrong, 1);
	}

	// キーコードとイベントの対応
	private void initKeycodeMap(){
		keycodeMap = new SparseArray<String>();
		keycodeMap.put(KeyEvent.KEYCODE_DPAD_LEFT, playerList.get(0));
		keycodeMap.put(KeyEvent.KEYCODE_DPAD_UP, playerList.get(1));
		keycodeMap.put(KeyEvent.KEYCODE_DPAD_DOWN, playerList.get(2));
		keycodeMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, playerList.get(3));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_7, playerList.get(4));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_8, playerList.get(5));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_4, playerList.get(6));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_3, playerList.get(7));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_2, playerList.get(8));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_1, playerList.get(9));

		correctKey = KeyEvent.KEYCODE_BUTTON_5;
		wrongKey = KeyEvent.KEYCODE_BUTTON_6;
	}

	// プレイヤーリストの設定読み込み
	private void loadPreferences(){
        // 設定読み込み
    	SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);
    	// プレイヤーリスト初期化
    	playerList = new ArrayList<String>();
		for(int i = 0; i < 10; i++){
			String playerName = pref.getString("player"+ i, "no name");
			playerList.add(playerName);
		}

		// 設定読込後(playerList生成後)に実行しないとエラー
		initKeycodeMap();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 押した人表示のTextView
		pushedTextView = (TextView) findViewById(R.id.pushed);

		// 音声ファイルの読み込み
		loadSound();

		// 設定（プレイヤーリスト）の読込
		loadPreferences();
	}

    // アラートダイアログ
    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {

        case 0:
            //ダイアログの作成(AlertDialog.Builder)
            return new AlertDialog.Builder(MainActivity.this)
            .setMessage("「早押し機」を終了しますか?")
            .setCancelable(false)
            // 「終了する」が押された時の処理
            .setPositiveButton("終了する", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	unloadSound();
                    // アクティビティ消去
                    MainActivity.this.finish();
                }
            })
            // 「終了しない」が押された時の処理
            .setNegativeButton("終了しない", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            })
            .create();
        }
        return null;
    }

	@Override
	protected void onDestroy() {
		unloadSound();
		super.onDestroy();
	}

	private void unloadSound(){
		if(sp != null){
			if(soundIdPush != -1){
				//使わなくなったサウンドをunload
				sp.unload(soundIdPush);
			}
			if(soundIdCorrect != -1){
				sp.unload(soundIdCorrect);
			}
			if(soundIdWrong != -1){
				sp.unload(soundIdWrong);
			}
			//SoundPoolが使用しているリソースを全て解放
			sp.release();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		// キーが押された時
		if (e.getAction() == KeyEvent.ACTION_DOWN) {
			int k = e.getKeyCode();
			// 戻るキー
			if(KeyEvent.KEYCODE_BACK == k){
				showDialog(0);
			}

			// 音量UP/DOWN
			if(KeyEvent.KEYCODE_VOLUME_UP == k || KeyEvent.KEYCODE_VOLUME_DOWN == k){
				// 音量を更新
				volumeRate = getVolumeRate();
				Log.d(TAG, "VOLUME CHANGE:" + volumeRate);
			}

			if(correctKey == k){ // 正解ボタン
				sp.play(soundIdCorrect, volumeRate, volumeRate, 0, 0, 1.0F);
				buttonIsPushed = true;
			}else if(wrongKey == k){ // 誤答ボタン
				if(isPlayingWrongSound == false){
					isPlayingWrongSound = true;
					streamIdWrong = sp.play(soundIdWrong, volumeRate, volumeRate, 0, -1, 1.0F);
					buttonIsPushed = true;
				}else{
					sp.resume(streamIdWrong);
				}
			}else{ // その他のボタン→プレイヤー判定
				push(k);
			}
		}
		// 正解・誤答キーを上げた時に初めてリセット
		if (e.getAction() == KeyEvent.ACTION_UP){
			int k = e.getKeyCode();
			if( correctKey == k ){
				reset();
			}else if ( wrongKey == k ){
				sp.pause(streamIdWrong);
				reset();
			}
		}

		return super.dispatchKeyEvent(e);
	}

	// ボタンを押した時の処理
	private void push(int keyCode){
		Log.d(TAG, "KeyCode:" + keyCode);
		TextView kv = (TextView) findViewById(R.id.displayKeycode);
		kv.setText(String.valueOf(keyCode));

		if (buttonIsPushed == false){
			// キーコードからプレイヤーを取得
			// プレイヤーに対応しないキーコードの場合はnull
			pushPlayer = keycodeMap.get(keyCode);
			if (pushPlayer != null){
				// ボタン音を鳴らす
				sp.play(soundIdPush, volumeRate, volumeRate, 0, 0, 1.0F);

				// 押した人表示
				pushedTextView.setText(pushPlayer);

				// ２着以降を受け付けない
				buttonIsPushed = true;
			}
		}
	}

	// リセット処理
	private void reset(){
		// 押している回答者がいる場合、表示をリセットする
		if(pushPlayer != null){
			pushedTextView.setText("");
		}
		// ボタンが押されていない状態に戻す
		buttonIsPushed = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_settings:
	        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
	        this.startActivityForResult(intent, REQUEST_CODE);
	        break;
	    case R.id.menu_emu_correct:
			sp.play(soundIdCorrect, volumeRate, volumeRate, 0, 0, 1.0F);
	    	reset();
	    	break;
	    case R.id.menu_emu_wrong:
			sp.play(soundIdWrong, volumeRate, volumeRate, 0, 0, 1.0F);
	    	reset();
	    	break;
	    case R.id.menu_emu_reset:
	    	reset();
	    	break;
	    }
	    return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE) {
              Log.d(TAG, "requestCode = " + requestCode);
              if (resultCode == RESULT_OK) {
                  Log.d(TAG, "resultCode = " + resultCode);
                  loadPreferences();
              }
              if (resultCode == RESULT_CANCELED){
                  Log.d(TAG, "resultCode = " + resultCode);
                  // 何もしない
              }
         }
    }
}

