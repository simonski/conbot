package conbot.utils;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

public class GCSTest {

	// 700MB public file we can get
	public static final String GCS_PATH = "gs://gcp-public-data-landsat/index.csv.gz"; 
			
	@Test
	public void testBucketAndKey() {

		String path = GCS_PATH;
		String[] bucketAndKey = CloudStorage.getBucketAndKeyFromPath(path);
		String bucket = bucketAndKey[0];
		String key = bucketAndKey[1];
		if (!bucket.equals("gcp-public-data-landsat")) {
			fail("Bad bucket '" + bucket + "'.");
		}

		if (!key.equals("/index.csv.gz")) {
			fail("Bad key '" + key + "'.");
		}
	}
	
	// @Test
	public void testGSCopy() throws Exception {
		File tempFile = new File("index.csv.gz");
		GCS.copyFromGCS(GCS_PATH, tempFile);
	}

}
