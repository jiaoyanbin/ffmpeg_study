package com.example.welink_mu_testing2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.welink.mu.test.dispatch.WLMuTestHelperListener;
import com.android.welink.mu.test.dispatch.WLTestManager;
import com.android.welink.mu.test.tool.QRCodeUtil;
import com.android.welink.mu.test.videoController.LocalChannelMuController;
import com.wedrive.android.welink.jni.FFmpegUtils;

public class MainActivity extends Activity implements OnClickListener,
		WLMuTestHelperListener {

	private LocalChannelMuController localChannelMuController;

	private ImageView iv;
	private int codeMode = 1;

	private int temp = 0;

	private Button btn_v;
	private Button btn_i_one;
	private Button btn_i_two;
	TextView tv_status,tv_result;
	private ImageView img_ewm_one, img_ewm_two, img_ewm_three, img_ewm_four,
			img_ewm_five;

	private WLTestManager wlTestManager;

	
	
//	FFmpegUtils ffmpegUtils;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		ffmpegUtils = new FFmpegUtils();
		wlTestManager = WLTestManager.getInstance(this);
		wlTestManager.setOnWLHuHelperListener(this);
		iv = (ImageView) findViewById(R.id.iv);

		tv_status = (TextView) findViewById(R.id.tv_status);
		tv_result = (TextView) findViewById(R.id.tv_result);
		// 二维码控件

		ll_top = (LinearLayout) findViewById(R.id.ll_top);
		btn_start = (Button) findViewById(R.id.btn_start);

		btn_v = (Button) findViewById(R.id.btn_v);
		btn_i_one = (Button) findViewById(R.id.btn_i_one);
		btn_i_two = (Button) findViewById(R.id.btn_i_two);

		img_ewm_one = (ImageView) findViewById(R.id.img_ewm_one);
		img_ewm_two = (ImageView) findViewById(R.id.img_ewm_two);
		img_ewm_three = (ImageView) findViewById(R.id.img_ewm_three);
		img_ewm_four = (ImageView) findViewById(R.id.img_ewm_four);
		img_ewm_five = (ImageView) findViewById(R.id.img_ewm_five);

		img_ewm_one.setOnClickListener(this);
		img_ewm_two.setOnClickListener(this);
		img_ewm_three.setOnClickListener(this);
		img_ewm_four.setOnClickListener(this);
		img_ewm_five.setOnClickListener(this);

		btn_start.setOnClickListener(this);

		btn_v.setOnClickListener(this);
		btn_i_one.setOnClickListener(this);
		btn_i_two.setOnClickListener(this);
		tv_result.setOnClickListener(this);

		// 二维码图片较大时，生成图片、保存文件的时间可能较长，因此放在新线程中
		new Thread(new Runnable() {
			@Override
			public void run() {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						img_ewm_one.setImageBitmap(QRCodeUtil.createQRImage(
								"erweimaone", 100, 100, MainActivity.this));
						img_ewm_two.setImageBitmap(QRCodeUtil.createQRImage(
								"erweimatwo", 100, 100, MainActivity.this));
						img_ewm_three.setImageBitmap(QRCodeUtil.createQRImage(
								"erweimathree", 100, 100, MainActivity.this));
						img_ewm_four.setImageBitmap(QRCodeUtil.createQRImage(
								"erweimafour", 100, 100, MainActivity.this));
						img_ewm_five.setImageBitmap(QRCodeUtil.createQRImage(
								"erweimafive", 100, 100, MainActivity.this));

					}
				});
			}
		}).start();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		wlTestManager.destroy();
	}

	// @Override
	// public void onReadyForLocal() {
	// // if (codeMode == 1) {
	// // localChannelMuController.sendData("SCREEN 800 450");// 图片数据
	// // } else {
	// // localChannelMuController.sendData("svideo  720 1280");
	// // }
	// // localChannelMuController.sendData("resume");
	// // localChannelMuController.sendData("config 22");
	// //
	// // localChannelMuController
	// // .sendData("checktop com.example.welink_mu_testing2;");
	// // localChannelMuController.sendData("startcheck 1");
	//
	// }

	private LinearLayout ll_top;
	private Button btn_start;

	// @Override
	// public void onScreenChanged(final byte[] mImgBytes2) { // 图片流
	//
	// bitmap = BitmapFactory
	// .decodeByteArray(mImgBytes2, 0, mImgBytes2.length);
	// if (bitmap == null) {
	// return;
	// }
	//
	// if (temp == 0) {
	// runOnUiThread(new Runnable() {
	// @Override
	// public void run() {
	// temp++;
	//
	// QRCodeUtil.discernBitmap(MainActivity.this, bitmap);
	// iv.setImageBitmap(bitmap);
	//
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	//
	// int tmp = 80;
	// WindowManager wm = (WindowManager) MainActivity.this
	// .getSystemService(Context.WINDOW_SERVICE);
	//
	// int width = wm.getDefaultDisplay().getWidth();
	// int height = wm.getDefaultDisplay().getHeight();
	// if (width > height) {
	// width = width + height;
	// height = width - height;
	// width = width - height;
	// }
	// // TODO Auto-generated method stub
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + tmp + " down");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + tmp + " up");
	//
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + tmp + " down");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + tmp + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + tmp + " up");
	//
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + (height - tmp) + " down");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + (height - tmp) + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + (height - tmp) + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + (height - tmp) + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + (height - tmp) + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + (height - tmp) + " move");
	// localChannelMuController.sendData("motionevent "
	// + tmp + " " + (height - tmp) + " up");
	//
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + (height - tmp)
	// + " down");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + (height - tmp)
	// + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + (height - tmp)
	// + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + (height - tmp)
	// + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + (height - tmp)
	// + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + (height - tmp)
	// + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) + " " + (height - tmp)
	// + " up");
	//
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) / 2 + " " + (height - tmp)
	// / 2 + " down");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) / 2 + " " + (height - tmp)
	// / 2 + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) / 2 + " " + (height - tmp)
	// / 2 + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) / 2 + " " + (height - tmp)
	// / 2 + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) / 2 + " " + (height - tmp)
	// / 2 + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) / 2 + " " + (height - tmp)
	// / 2 + " move");
	// localChannelMuController.sendData("motionevent "
	// + (width - tmp) / 2 + " " + (height - tmp)
	// / 2 + " up");
	//
	// }
	// }).start();
	//
	// // 发送点击事件
	// // drawable.
	//
	// }
	// });
	// }
	// }

	int clickCount= 0;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_ewm_one:
			Toast.makeText(this, "点中了第一个", Toast.LENGTH_SHORT).show();
			clickCount++;
			break;
		case R.id.img_ewm_two:
			Toast.makeText(this, "点中了第二个", Toast.LENGTH_SHORT).show();
			clickCount++;
			break;
		case R.id.img_ewm_three:
			Toast.makeText(this, "点中了第三个", Toast.LENGTH_SHORT).show();
			clickCount++;
			break;
		case R.id.img_ewm_four:
			Toast.makeText(this, "点中了第四个", Toast.LENGTH_SHORT).show();
			clickCount++;
			break;
		case R.id.img_ewm_five:
			Toast.makeText(this, "点中了第五个", Toast.LENGTH_SHORT).show();
			clickCount++;
			break;
		case R.id.btn_start:
			// TODO 开始录屏
			startCheck(); 
			
			
			
//			byte [] sss = {2,4,6,8,10,12,14,16};
//			ffmpegUtils.decodeStream(sss);
			break;
		case R.id.btn_v:
			// TODO 开始录屏
			wlTestManager.switchMode(3);
			break;
		case R.id.btn_i_one:
			// TODO 开始录屏
			wlTestManager.switchMode(1);
			break;
		case R.id.btn_i_two:
			// TODO 开始录屏
			wlTestManager.switchMode(2);
			break;
		case R.id.tv_result:
			// TODO 开始录屏
			tv_result.setVisibility(View.GONE);
			break;
		default:
			break;
		}

	}

	private void startCheck() {
		tv_status.setText("正在初始化引擎");
		wlTestManager.start();
		ll_top.setVisibility(View.GONE);
	}

	public void kill(View view) {
		wlTestManager.kill();
	}

	@Override
	public void checkStatus(final String str) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				tv_status.setText(str);// TODO Auto-generated method stub

			}
		});

	}

	@Override
	public void onCheckVideoMode(int mode, Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckTouchEvent(boolean isSuccess, Object obj) {

		final String str = (String) obj;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String stroo = new String(str);
				tv_status.setText("检测完成");
				tv_result.setVisibility(View.VISIBLE);
				stroo+="\n检测点击事件 共5个点\n 成功"+clickCount+"点";
				tv_result.setText(stroo);
				clickCount = 0;
			}
		});

	}

}
