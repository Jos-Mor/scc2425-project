package main.java.utils;

import java.util.concurrent.BlockingQueue;

/**
 * A collection of convenience methods for dealing with threads.
 * 
 * @author smduarte (smd@fct.unl.pt)
 * 
 */
final public class Queues {

	private Queues() {
	}

	static public <T> T takeFrom(BlockingQueue<T> queue) {
		while (true)
			try {
				return queue.take();
			} catch (InterruptedException e) {
			}
	}

	static public <T> void putInto(BlockingQueue<T> queue, T val) {
		while (true)
			try {
				queue.put(val);
				return;
			} catch (InterruptedException e) {
			}
	}
}
