package com.sheca.umandroid.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sheca.umandroid.R;

public class ChangePasswordDialog extends Dialog {

	public ChangePasswordDialog(Context context) {
		super(context);
	}

	public ChangePasswordDialog(Context context, int theme) {
		super(context, theme);

	}

	public static class Builder {
		private Context context;
		private String title;
		private String message1;
		private String message2;
		private String positiveButtonText;
		private String negativeButtonText;
		private View layout;
		private int iconResID = android.R.drawable.ic_dialog_info;
		private DialogInterface.OnClickListener positiveButtonClickListener;
		private DialogInterface.OnClickListener negativeButtonClickListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setMessage1(String message) {
			this.message1 = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage1(int message) {
			this.message1 = (String) context.getText(message);
			return this;
		}

		public Builder setMessage2(String message) {
			this.message2 = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage2(int message) {
			this.message2 = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public void setIcon(int resId) {
			this.iconResID = resId;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(int negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context
					.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(String negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		public String getEditText1Value() {
			String editTextValue;
			editTextValue = ((EditText) layout
					.findViewById(R.id.chpwddlg_pwdtext1)).getText().toString();
			return editTextValue;
		}
		
		public String getEditText2Value() {
			String editTextValue;
			editTextValue = ((EditText) layout
					.findViewById(R.id.chpwddlg_pwdtext2)).getText().toString();
			return editTextValue;
		}


		public ChangePasswordDialog create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final ChangePasswordDialog dialog = new ChangePasswordDialog(
					context, android.R.style.Theme_Dialog);
			dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			layout = inflater.inflate(R.layout.changepassworddlg, null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// set the dialog title
			((TextView) layout.findViewById(R.id.chpwddlg_title)).setText(title);
			// set the confirm button
			if (positiveButtonText != null) {
				((Button) layout.findViewById(R.id.chpwddlg_positiveButton))
						.setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((Button) layout.findViewById(R.id.chpwddlg_positiveButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									positiveButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.chpwddlg_positiveButton).setVisibility(
						View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null) {
				((Button) layout.findViewById(R.id.chpwddlg_negativeButton))
						.setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					((Button) layout.findViewById(R.id.chpwddlg_negativeButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									negativeButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.chpwddlg_negativeButton).setVisibility(
						View.GONE);
			}
			// set the content message
			if (message1 != null) {
				((TextView) layout.findViewById(R.id.chpwddlg_message1))
						.setText(message1);
			}

			if (message2 != null) {
				((TextView) layout.findViewById(R.id.chpwddlg_message2))
						.setText(message2);
			}

			((ImageView) layout.findViewById(R.id.icon))
					.setImageResource(iconResID);

			dialog.setContentView(layout);
			return dialog;
		}

		public ChangePasswordDialog show() {
			ChangePasswordDialog dialog = create();
			dialog.show();
			return dialog;
		}

	}
}
