package com.example.quizmachine;

import java.util.ArrayList;

import com.example.quizmachine.R;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {

	private final String TAG = "SettingActivity";

	// Spinner, EditTextのリスト
	// XMLで設置してるが設定読込・保存のため必要
	ArrayList<Spinner> spinnerList;
	ArrayList<EditText> editTextList;

	// 効果音
	private SoundPool sp;
	private int pushSound;
	// 音量 AudioManagerで取得
	private float volumeRate;

	// キーコードとボタンラベルの対応表
	// 押されたボタンのラベル色を変える
	private SparseArray<TextView> keycodeMap;

	// クイズの状態＝直前に押されたボタン
	private TextView pushedButton = null;

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
					Toast.makeText(getApplicationContext(), "音声読込み完了", Toast.LENGTH_LONG).show();
				}
			}
		});

		// 効果音の読み込み
		pushSound = sp.load(this, R.raw.push, 1);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

		// 音声ファイルの読み込み
		loadSound();
		// キーコードの対応付け
		initKeycodeMap();
        // 設定読込み
		loadPreferences();

	}

	// キーコードとボタン（ラベル）の対応付け
	// TODO: 今は持ってるボタンに名前が縛られるので、
	// ボタンに関係なく並び順に応じて表示名と対応付けられるようにしたい。
	private void initKeycodeMap(){
		keycodeMap = new SparseArray<TextView>();
		keycodeMap.put(KeyEvent.KEYCODE_DPAD_LEFT, (TextView) findViewById(R.id.TextView01));
		keycodeMap.put(KeyEvent.KEYCODE_DPAD_UP, (TextView) findViewById(R.id.TextView02));
		keycodeMap.put(KeyEvent.KEYCODE_DPAD_DOWN, (TextView) findViewById(R.id.TextView03));
		keycodeMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, (TextView) findViewById(R.id.TextView04));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_7, (TextView) findViewById(R.id.TextView05));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_8, (TextView) findViewById(R.id.TextView06));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_4, (TextView) findViewById(R.id.TextView07));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_3, (TextView) findViewById(R.id.TextView08));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_2, (TextView) findViewById(R.id.TextView09));
		keycodeMap.put(KeyEvent.KEYCODE_BUTTON_1, (TextView) findViewById(R.id.TextView10));
	}

	private void initSpinnerList(){
		spinnerList = new ArrayList<Spinner>();
		spinnerList.add((Spinner) findViewById(R.id.Spinner01));
		spinnerList.add((Spinner) findViewById(R.id.Spinner02));
		spinnerList.add((Spinner) findViewById(R.id.Spinner03));
		spinnerList.add((Spinner) findViewById(R.id.Spinner04));
		spinnerList.add((Spinner) findViewById(R.id.Spinner05));
		spinnerList.add((Spinner) findViewById(R.id.Spinner06));
		spinnerList.add((Spinner) findViewById(R.id.Spinner07));
		spinnerList.add((Spinner) findViewById(R.id.Spinner08));
		spinnerList.add((Spinner) findViewById(R.id.Spinner09));
		spinnerList.add((Spinner) findViewById(R.id.Spinner10));
	}

	private void initEditTextList(){
		editTextList = new ArrayList<EditText>();
		editTextList.add((EditText) findViewById(R.id.EditText01));
		editTextList.add((EditText) findViewById(R.id.EditText02));
		editTextList.add((EditText) findViewById(R.id.EditText03));
		editTextList.add((EditText) findViewById(R.id.EditText04));
		editTextList.add((EditText) findViewById(R.id.EditText05));
		editTextList.add((EditText) findViewById(R.id.EditText06));
		editTextList.add((EditText) findViewById(R.id.EditText07));
		editTextList.add((EditText) findViewById(R.id.EditText08));
		editTextList.add((EditText) findViewById(R.id.EditText09));
		editTextList.add((EditText) findViewById(R.id.EditText10));
	}

	// SpinnerとEditTextの設定読み込み
	private void loadPreferences(){
        // オブジェクトリストの初期化
        initSpinnerList();
        initEditTextList();

        // 設定読み込み
    	SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);

		for(int i = 0; i < 10; i++){
			int position = pref.getInt("spinner" + i, 0);
			String text = pref.getString("edittext"+ i, "");

			Spinner spinner = (Spinner) spinnerList.get(i);
			spinner.setSelection(position);

			EditText et = (EditText) editTextList.get(i);
			et.setText(text);
		}
	}

	// 現在の設定を保存
	private void savePreferences(){
        // 設定読み込み
    	SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        // Spinnerの選択状態を保存
        // EditTextを設定として保存
		for(int i = 0; i < 10; i++){
			Spinner spinner = (Spinner) spinnerList.get(i);
			int spinnerPosition = spinner.getSelectedItemPosition();
			String spinnerItem =spinner.getSelectedItem().toString();
			EditText et = (EditText) editTextList.get(i);
			String etText = et.getText().toString().trim();

			// GUI部品の設定値保存
			editor.putInt("spinner"+i, spinnerPosition);
			editor.putString("edittext"+i, etText);

			// プレイヤーリストの保存
			// EditText優先
			if(etText.equals("")){
				editor.putString("player"+i, spinnerItem);
			}else{
				editor.putString("player"+i, etText);
			}
		}
		editor.commit();
		Toast.makeText(this, "設定を保存", Toast.LENGTH_LONG).show();
	}

	// すべてのEditTextをクリア
	private void clearEditText(){
		for(EditText et: editTextList){
			et.setText("");
		}
		Toast.makeText(this, "クリアしました", Toast.LENGTH_LONG).show();
	}

	// すべてのSpinnerをクリア
	private void clearSpinnerSelection(){
		for(Spinner spinner: spinnerList){
			spinner.setSelection(0);
		}
		Toast.makeText(this, "クリアしました", Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getAction() == KeyEvent.ACTION_DOWN) { // キーが押された時
			int k = e.getKeyCode();
			// BACK時に現在の設定を保存
			if(k == KeyEvent.KEYCODE_BACK){
				savePreferences();
				setResult(RESULT_OK);
			}else if(KeyEvent.KEYCODE_VOLUME_UP == k || KeyEvent.KEYCODE_VOLUME_DOWN == k){ // 音量UP/DOWN
				// 音量を更新
				volumeRate = getVolumeRate();
				Log.d(TAG, "VOLUME CHANGE:" + volumeRate);
			}else{
				push(k);
			}
		}
		return super.dispatchKeyEvent(e);
	}

	private void push(int keyCode){
		Log.d(TAG, "KeyCode:" + keyCode);
		TextView display = (TextView)findViewById(R.id.displayKeycode);
		display.setText(String.valueOf(keyCode));

		TextView nowPushed = keycodeMap.get(keyCode);
		// ボタンに対応するキーが押された
		if (nowPushed != null){
			// ボタン音を鳴らす
			sp.play(pushSound, volumeRate, volumeRate, 0, 0, 1.0F);
			// 直前に押されたボタンの背景をリセットする
			if(pushedButton != null){
				pushedButton.setBackgroundColor(color.background_light);
			}
			// 直前に押されたボタンを更新
			pushedButton = nowPushed;
			// TextViewの背景色を変える
			pushedButton.setBackgroundColor(Color.YELLOW);
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_setting, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_cancel:
			setResult(RESULT_CANCELED);
			SettingActivity.this.finish();
	    	return true;

	    case R.id.menu_clear_selection:
	    	clearSpinnerSelection();
	    	return true;

	    case R.id.menu_clear_text:
	    	clearEditText();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}