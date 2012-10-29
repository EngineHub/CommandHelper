package com.laytonsmith.PureUtilities;

/**
 * A generic class for creating a text marquee.
 *
 * @author lsmith
 */
public final class Marquee {

	public interface MarqueeCallback {

		void stringPortion(String portion);
	}
	private String text;
	private int maxChars;
	private int delay;
	private boolean run;
	private MarqueeCallback callback;

	public Marquee(String string, int maxChars, int delay, MarqueeCallback callback) {
		setText(string);
		this.maxChars = maxChars;
		this.delay = delay;
		this.callback = callback;
	}

	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		if (!text.endsWith(" ")) {
			text = text + " ";
		}
		this.text = text;
	}

	public void start() {
		if (run) {
			return;
		}
		run = true;
		String name = text;
		if (name.length() > 10) {
			name = text.substring(0, 10);
		}
		new Thread(new Runnable() {
			public void run() {
				int loopPointer = 0;
				try {
					while (run) {
						final String composite;
						if (loopPointer + maxChars > text.length()) {
							//We want to grab from loop pointer to string max length, then from 0 to the number
							//of characters left to show.
							String s = text.substring(loopPointer);
							s += text.substring(0, maxChars - (text.length() - loopPointer));
							composite = s;
						} else {
							//Else the entire string in one run
							composite = text.substring(loopPointer, maxChars + loopPointer);
						}
						loopPointer++;
						if (loopPointer > text.length()) {
							//reset it once we go over the length
							loopPointer = 0;
						}
						
						callback.stringPortion(composite);
						
						Thread.sleep(delay);
					}
				} catch (Exception ex) { //We want an exception to kill us, but we also want to rethrow it as a runtime exception.
					throw new RuntimeException(ex);
				}
			}
		}, "Marquee - " + name).start();
	}

	public void stop() {
		run = false;
	}
}
