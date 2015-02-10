package com.tyrantapp.olive.listener;

public interface OnOliveKeypadListener {
	public void onKeypadCreate(int sectionNumber);
	public void onKeypadClick(int sectionNumber, int index);
    public void onKeypadLongClick(int sectionNumber, int index);
}
