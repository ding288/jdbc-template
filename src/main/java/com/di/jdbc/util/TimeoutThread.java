package com.di.jdbc.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author di
 */
public class TimeoutThread extends Thread {
	static AtomicBoolean run = new AtomicBoolean(false);

	@Override
	public void run() {
		run = new AtomicBoolean(true);
		while (ConnectionUtil.RUN) {
			try {
				ConnectionUtil.repairTimeout();
				sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		run = new AtomicBoolean(false);
	}
}