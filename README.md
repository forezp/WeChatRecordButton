# WeChatRecord
A recordingButton  like WeChat ,the recording file is mp3 generate by lame-3.98.4_libmp3lame, which can play in ios device.

一个封装好的Button ，之前从别人的项目中提取，感谢原作者，
因为项目需要，这个Buton 的录音为amr文件，ios 4.3以后播放不了。
所以采用了lame库，安卓录的为mp3，ios也可以播放。

##  使用方法

 mAudioRecorderButton = (AudioRecorderButton) findViewById(R.id.id_recorder_button);
        
        mAudioRecorderButton.setAudioFinishRecorderListener(new AudioFinishRecorderListener() {
			
			@Override
			public void onFinish(float seconds, String filePath) {
				Recorder recorder = new Recorder(seconds, filePath);
				mDatas.add(recorder);
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(mDatas.size() - 1);
				
			}
		});
		
		## 部分源码
		
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
	
	其中RecMicToMp3为封装好的采用 lame库封装的一个录音组件。
	非常的好用，唯一不足的是，作者没有提供在录音过程中获取音量的接口，这需要自己去实现
	，可以根据输入流去计算，音量值，从而可以得到根据音量大小的动态录音效果。


