package defcode_recognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.TreeSet;

public class FindTask implements Runnable {
	protected BufferedReader br;
	protected TreeSet<Range> tree;

	private Range range;

	public FindTask(BufferedReader br, TreeSet<Range> tree) {
		this.br = br;
		this.tree = tree;

		this.range = new Range(0, 0, null);
	}

	@Override
	public void run() {
		String line;

		try {
			while ((line = br.readLine())!=null) {
				processNumber(line);
			}
		} catch (IOException ex) {
			System.err.println("Something wrong when processing numbers base");
			System.err.println(ex.toString());
		}
	}

	protected void processNumber(String input) {
		String[] parts = input.split(";", 2);

		String payload = parts[1];

		long value = Long.parseLong(parts[0]);

		this.range.setLeft(value);
		this.range.setRight(value);
		this.range.setPayload(payload);

		Range r = null;

		try {
			r = tree.ceiling(this.range);
		} catch (Exception e) {
			/* */
		}

		if (
			r != null
			&& r.getLeft() <= this.range.getLeft()
			&& r.getRight() >= this.range.getRight()
		) {
			System.out.println(value+";"+r.getLeft()+";"+r.getRight()+";"+r.getPayload().toString()+";"+payload);
		} else {
			System.out.println(value+";;;;"+payload);
		}

	}
}
