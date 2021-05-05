package conbot.utils;

import java.util.Random;

public class RANDOM {

	static final Random random = new Random(System.currentTimeMillis());

	public static final int nextInt() {
		return random.nextInt();
	}

	public static final int nextInt(int var) {
		return random.nextInt(var);
	}

	public static final int nextInt(int lower, int upper) {
		return random.nextInt(upper - lower) + lower;
	}

	public static float nextFloat() {
		return random.nextFloat();
	}

	public static boolean nextBoolean() {
		return random.nextBoolean();
	}

}
