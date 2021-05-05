package conbot.utils;

/**
 */
public class S3 {

//	static final Regions DEFAULT_REGION = Regions.DEFAULT_REGION;
//
//	// input: s3://bucket/name/of/the.thing.foo
//	// output [ "bucket", "/name/of/the.thing.foo" ]
//	public static String[] getBucketAndKeyFromPath(String path) {
//		path = path.replace("s3://", "");
//		path = path.replace("gs://", "");
//		String[] splits = path.split("/");
//		String bucket = splits[0];
//		String key = path.substring(bucket.length());
//		return new String[] { bucket, key };
//	}
//
//	public static File copyFromS3(Regions region, String path, File target) throws Exception {
//		String[] bucketAndKey = S3.getBucketAndKeyFromPath(path);
//		String bucket = bucketAndKey[0];
//		String key = bucketAndKey[1];
//		key = key.substring(1);
//		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
//		S3ObjectInputStream s3is = null;
//		FileOutputStream fos = null;
//		try {
//			S3Object o = s3.getObject(bucket, key);
//			s3is = o.getObjectContent();
//			fos = new FileOutputStream(target);
//			byte[] read_buf = new byte[1024];
//			int read_len = 0;
//			while ((read_len = s3is.read(read_buf)) > 0) {
//				fos.write(read_buf, 0, read_len);
//			}
//		} finally {
//			FileUtils.close(s3is);
//			FileUtils.close(fos);
//		}
//		return target;
//	}
//

}
