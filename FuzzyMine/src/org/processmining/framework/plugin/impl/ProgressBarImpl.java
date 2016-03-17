package org.processmining.framework.plugin.impl;

import org.processmining.framework.plugin.Progress;

public class ProgressBarImpl implements Progress{
	private int value = 0;
	private int min = 0;
	private int max = 1;
	private boolean indeterminate = true;
	private String message = "";
	private static final long serialVersionUID = -3950799546173352932L;
	//private final PluginContext context;
	private boolean canceled = false;
	
	@Override
	public void setMinimum(int value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setMaximum(int value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setValue(int value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setCaption(String message) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getCaption() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void inc() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setIndeterminate(boolean makeIndeterminate) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isIndeterminate() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public int getMinimum() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getMaximum() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}
	
		
}
