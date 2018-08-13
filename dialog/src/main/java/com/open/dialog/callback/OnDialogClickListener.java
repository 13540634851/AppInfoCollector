package com.open.dialog.callback;

import android.app.Dialog;

import com.open.dialog.annotation.ClickPosition;


/**
 * 作者：Alex
 * 时间：2016年09月03日    21:29
 * 简述：
 */
@SuppressWarnings("all")
public interface OnDialogClickListener<D extends Dialog>
{
	public  void onDialogClick(D dialog, @ClickPosition String clickPosition);
}
