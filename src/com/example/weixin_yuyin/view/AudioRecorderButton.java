package com.example.weixin_yuyin.view;

import java.util.Random;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.weixin_yuyin.R;
import com.example.weixin_yuyin.view.AudioManager.AudioStateListener;
import com.uraroji.garage.android.lame.RecMicToMp3;



public class AudioRecorderButton extends Button implements AudioStateListener {

	private static final int STATE_NORMAL = 1;// 默认的状态
	private static final int STATE_RECORDING = 2;// 正在录音
	private static final int STATE_WANT_TO_CANCEL = 3;// 希望取消

	private int mCurrentState = STATE_NORMAL; // 当前的状态
	private boolean isRecording = false;// 已经开始录音

	private static final int DISTANCE_Y_CANCEL = 50;

	private DialogManager mDialogManager;
	//  private AudioManager mAudioManager;

	private float mTime;
	// 是否触发longClick
	private boolean mReady;

	private static final int MSG_AUDIO_PREPARED = 0x110;
	private static final int MSG_VOICE_CHANGED = 0x111;
	private static final int MSG_DIALOG_DIMISS = 0x112;


	private RecMicToMp3 mRecMicToMp3;
	private String mp3Path;
	/*
	 * 获取音量大小的线程
	 */
	private Runnable mGetVoiceLevelRunnable = new Runnable() {

		public void run() {
			while (isRecording) {
				try {
					Thread.sleep(300);
					mTime += 0.3f;
					mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AUDIO_PREPARED:
				// 显示對話框在开始录音以后
				mDialogManager.showRecordingDialog();
				isRecording = true;
				// 开启一个线程
				new Thread(mGetVoiceLevelRunnable).start();
				break;

			case MSG_VOICE_CHANGED:
				// mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
				mDialogManager.updateVoiceLevel(getVoiceLevel(7));
				break;

			case MSG_DIALOG_DIMISS:
				mDialogManager.dimissDialog();
				break;

			}

			super.handleMessage(msg);
		}
	};

	/**
	 * 以下2个方法是构造方法
	 */
	public AudioRecorderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDialogManager = new DialogManager(context);
		// 
		//        //String dir = /storage/sdcard0/my_weixin;
		//        String dir = Environment.getExternalStorageDirectory()+"/my_weixin";
		//       
		//        mAudioManager = AudioManager.getInstance(dir);
		//        mAudioManager.setOnAudioStateListener(this);

		// 由于这个类是button所以在构造方法中添加监听事件
		setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {

				mp3Path=Environment.getExternalStorageDirectory()+"/ZA111.mp3";
				mRecMicToMp3 = new RecMicToMp3(
						mp3Path, 8000);
				mReady = true;
				mRecMicToMp3.start();
				mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
				//  mAudioManager.prepareAudio();
				//  mRecMicToMp3.start();

				return false;
			}
		});
	}

	@Override
	public void wellPrepared() {
		//	mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
	}

	public AudioRecorderButton(Context context) {
		this(context, null);
	}

	/**
	 * 录音完成后的回调
	 */
	public interface AudioFinishRecorderListener {
		void onFinish(float seconds, String filePath);
	}

	private AudioFinishRecorderListener audioFinishRecorderListener;

	public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
		audioFinishRecorderListener = listener;
	}

	/**
	 * 屏幕的触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();
		int x = (int) event.getX();// 获得x轴坐标
		int y = (int) event.getY();// 获得y轴坐标

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			changeState(STATE_RECORDING);
			break;
		case MotionEvent.ACTION_MOVE:

			if (isRecording) {
				// 如果想要取消，根据x,y的坐标看是否需要取消
				if (wantToCancle(x, y)) {
					changeState(STATE_WANT_TO_CANCEL);
				} else {
					changeState(STATE_RECORDING);
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			if (!mReady) {
				reset();
				return super.onTouchEvent(event);
			}
			if (!isRecording || mTime < 0.6f) {
				mDialogManager.tooShort();
				//  mAudioManager.cancel();
				mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1000);// 延迟显示对话框
			} else if (mCurrentState == STATE_RECORDING) { // 正在录音的时候，结束
				mDialogManager.dimissDialog();
				// mAudioManager.release();
				mRecMicToMp3.stop();
				if (audioFinishRecorderListener != null) {
					// audioFinishRecorderListener.onFinish(mTime,mAudioManager.getCurrentFilePath());
					audioFinishRecorderListener.onFinish(mTime,mp3Path);
				}

			} else if (mCurrentState == STATE_WANT_TO_CANCEL) { // 想要取消
				mDialogManager.dimissDialog();
				//  mAudioManager.cancel();
			}
			reset();
			//mRecMicToMp3.stop();
			//mDialogManager.dimissDialog();
			break;

		}
		return super.onTouchEvent(event);
	}

	/**
	 * 恢复状态及标志位
	 */
	private void reset() {
		isRecording = false;
		mTime = 0;
		mReady = false;
		changeState(STATE_NORMAL);
	}

	private boolean wantToCancle(int x, int y) {
		if (x < 0 || x > getWidth()) { // 超过按钮的宽度
			return true;
		}
		// 超过按钮的高度
		if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
			return true;
		}

		return false;
	}

	/**
	 * 改变
	 */
	private void changeState(int state) {
		if (mCurrentState != state) {
			mCurrentState = state;
			switch (state) {
			case STATE_NORMAL:
				setBackgroundResource(R.drawable.btn_recorder_normal);
				setText(R.string.str_recorder_normal);
				break;

			case STATE_RECORDING:
				setBackgroundResource(R.drawable.btn_recording);
				setText(R.string.str_recorder_recording);
				if (isRecording) {
					mDialogManager.recording();
				}
				break;

			case STATE_WANT_TO_CANCEL:
				setBackgroundResource(R.drawable.btn_recording);
				setText(R.string.str_recorder_want_cancel); 
				mDialogManager.wantToCancel();
				break;
			}
		}
	}


	public int getVoiceLevel(int maxlevel) {
		//  if (isPrepare) {
		try {
			Random r=new Random();
			
			int x=r.nextInt(22760);
			// mMediaRecorder.getMaxAmplitude() 1~32767
			return maxlevel * x / 32768 + 1;
		} catch (Exception e) {
		}
		//  }
		return 1;
	}

}