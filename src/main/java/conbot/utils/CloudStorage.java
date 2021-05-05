package conbot.utils;

import java.io.File;

/**
 *
 */
public class CloudStorage {

	// input: s3://bucket/name/of/the.thing.foo
	// output [ "bucket", "/name/of/the.thing.foo" ]
	public static String[] getBucketAndKeyFromPath(String path) {
		path = path.replace("s3://", "");
		path = path.replace("gs://", "");
		String[] splits = path.split("/");
		String bucket = splits[0];
		String key = path.substring(bucket.length());
		return new String[] { bucket, key };
	}

	/**
	 * utility method attempts to hide the details of where we are getting our file
	 * from and any permissions etc.
	 * 
	 * @param path
	 * @param target
	 */
	public File copy(String path, File target) throws Exception {
//		if (FileCopier.isProtocolS3(path)) {
//			S3.copyFromS3(S3.DEFAULT_REGION, path, target);
//		} else 
		if (CloudStorage.isProtocolGCS(path)) {
			GCS.copyFromGCS(path, target);
		}
		return target;
	}

	public File copy(File file, String target) throws Exception {
		if (CloudStorage.isProtocolGCS(target)) {
			GCS.copyToGCS(file, target);
		}
		return file;
	}

	public static boolean isProtocolHTTP(String path) {
		return path.toLowerCase().startsWith("http:");
	}

	public static boolean isProtocolHTTPS(String path) {
		return path.toLowerCase().startsWith("https:");
	}

	public static boolean isProtocolS3(String path) {
		return path.toLowerCase().startsWith("s3:");
	}

	public static boolean isProtocolGCS(String path) {
		return path.toLowerCase().startsWith("gs:");
	}

	public static boolean isProtocolBigQuery(String path) {
		return path.toLowerCase().startsWith("bq:");
	}

	public static boolean isProtocolAzure(String path) {
		return path.toLowerCase().startsWith("az:");
	}

	public static boolean isProtocolCloud(String path) {
		return isProtocolS3(path) || isProtocolGCS(path) || isProtocolBigQuery(path) || isProtocolAzure(path)
				|| isProtocolHTTP(path) || isProtocolHTTPS(path);
	}

	// TODO this is *nix only, make it work on windows
	public static boolean isProtocolFile(String path) {
		return !isProtocolCloud(path);
	}

}
