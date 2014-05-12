package uk.org.openeyes.oink.common;

import java.util.Random;

public class RandomStringGenerator {
	
	private static char[] symbols;
	
	static {
		StringBuilder tmp = new StringBuilder();
		for (char ch = '0'; ch <= '9'; ++ch) {
			tmp.append(ch);
		}
		for (char ch = 'A'; ch <= 'Z'; ++ch) {
			tmp.append(ch);
		}
		symbols = tmp.toString().toCharArray();
	}
	
	private final Random random = new Random();
	
	private final char[] buf;
	
	public RandomStringGenerator(int length) {
		if (length < 1) {
			throw new IllegalArgumentException();
		}
		buf = new char[length];
	}
	
	public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx) {
			buf[idx] = symbols[random.nextInt(symbols.length)];
		}
		return new String(buf);
	}

}
