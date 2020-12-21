package com.sheca.umee;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.about);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		((TextView) findViewById(R.id.header_text)).setText("关于我们");
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/font.ttf");
		((TextView) findViewById(R.id.header_text)).setTypeface(typeFace);
		TextPaint tp = ((TextView) findViewById(R.id.header_text)).getPaint();
		tp.setFakeBoldText(true); 

		ImageButton cancelScanButton = (ImageButton) this
				.findViewById(R.id.btn_goback);

		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AboutActivity.this.finish();
			}
		});

		StringBuffer sb = new StringBuffer();
		sb.append("上海市数字证书认证中心有限公司（简称：上海CA中心）成立于1998年，是国内第一家专业的第三方电子认证服务机构和全国最大的依法设立的电子认证服务机构之一。也是上海市信息化发展不可或缺的基础保障设施和网络信任体系的建设运营服务主体。");
		sb.append("\n\n");
		sb.append("上海CA中心致力于信息安全、网络信任等方面产品的研发、生产、销售和服务，作为市政府正版化软件服务集成商，提供政府机关正版化软件、国产化软件和保密强配服务，为电子政务、电子商务和社会信息化等领域提供一体化解决方案，形成了以上海为中心、长三角为重点、辐射全国的服务体系。");
		sb.append("\n\n");
		sb.append("上海CA中心坚持以持续创新引导发展、以可信服务奉献社会、以成果共享凝聚人心的服务理念，致力成为行业领先、国内一流的互联网信任服务机构。首批获得电子认证服务、电子政务电子认证服务、电子认证服务使用密码等运营许可资质。");
		sb.append("\n\n");
		sb.append("上海CA中心通过国际WebTrust认证提供全球信任服务，是国家信息化试点单位、上海市高新技术企业、创新型企业、软件企业、信息安全服务推荐单位、电子认证工程技术研究中心，拥有电子认证全系列知识产权和产品，承担多项国家省部级科技专项，编制多项国家行业和地方标准。");
		//sb.append("\n\n");
		sb.append("");

		TextView aboutTextView = (TextView) findViewById(R.id.text_about);
		aboutTextView.setText(toDBC(sb.toString()));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			AboutActivity.this.finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	private String toDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}
}
