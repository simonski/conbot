package conbot.utils;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

/**
 *
 */
public class GCS {

	public static File copyFromGCS(String path, File target) throws Exception {
		String[] bucketAndKey = CloudStorage.getBucketAndKeyFromPath(path);
		String bucket = bucketAndKey[0];
		String key = bucketAndKey[1];
		key = key.substring(1);

		Storage storage = StorageOptions.getDefaultInstance().getService();
		Blob blob = storage.get(bucket, key);
		Path p = Paths.get(target.getAbsolutePath());
		blob.downloadTo(p);
		return target;
	}
	
	public static File copyToGCS(File file, String path) throws Exception {
		String newPath = path + "/" + file.getName();
		String[] bucketAndKey = CloudStorage.getBucketAndKeyFromPath(newPath);
		String bucket = bucketAndKey[0];
		String key = bucketAndKey[1];
		key = key.substring(1);

		Storage storage = StorageOptions.getDefaultInstance().getService();
	    BlobId blobId = BlobId.of(bucket, key);
	    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
	    storage.create(blobInfo, Files.readAllBytes(Paths.get(file.getPath())));
	    return file;
	}

}
