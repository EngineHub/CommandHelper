package com.laytonsmith.PureUtilities;

/**
 * A generic class for creating a text marquee.
 *
 *
 */
public final class Marquee {

	public interface MarqueeCallback {

		/**
		 * Sends the correct portion of the string, as well as a reference to this Marquee object, in case it needs to
		 * be stopped or otherwise changed.
		 *
		 * @param portion
		 * @param self
		 */
		void stringPortion(String portion, Marquee self);
	}
	private String text;
	private final int maxChars;
	private final int delay;
	private boolean run;
	private final MarqueeCallback callback;

	public Marquee(String string, int maxChars, int delay, MarqueeCallback callback) {
		this.maxChars = maxChars;
		setText(string);
		this.delay = delay;
		this.callback = callback;
	}

	public void setText(String text) {
		if(text == null) {
			text = "";
		}
		if(!text.endsWith(" ")) {
			text = text + " ";
		}
		if(text.length() < maxChars) {
			//Pad with spaces, so we still get the marquee effect
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < maxChars - text.length(); i++) {
				b.append(" ");
			}
			text += b.toString();
		}
		this.text = text;
	}

	public void start() {
		if(run) {
			return;
		}
		run = true;
		String name = text;
		if(name.length() > 10) {
			name = text.substring(0, 10);
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				int loopPointer = 0;
				try {
					while(run) {
						final String composite;
						String pseudoText = text + text + text;
						composite = pseudoText.substring(loopPointer, maxChars + loopPointer);
						loopPointer++;
						if(loopPointer > text.length()) {
							//reset it once we go over the length
							loopPointer = 0;
						}

						callback.stringPortion(composite, Marquee.this);

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
