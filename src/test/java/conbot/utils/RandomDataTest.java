package conbot.utils;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import conbot.utils.CLI;
import conbot.utils.RandomData;

public class RandomDataTest {

	@Test
	public void test() {
		RandomData rd = new RandomData();
		Random random = new Random();
		String pattern = "#AAAAA.999";
		String result = rd.create(pattern, random);
		CLI.out(pattern + "    =     " + result);
		Assert.assertTrue(result.length() == 10);
		Assert.assertTrue(result.substring(0, 1).equals("#"));
		Assert.assertTrue(result.substring(6, 7).equals("."));
		String[] splits = result.split("\\.");
		Assert.assertTrue(splits.length == 2);
		Assert.assertTrue(splits[0].length() == 6);
		Assert.assertTrue(splits[1].length() == 3);
	}

}
