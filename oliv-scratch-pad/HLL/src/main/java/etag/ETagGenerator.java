package etag;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.core.EntityTag;

/**
 * Utility class to create eTags for SurveyBuilder object. It is a way to centralize the eTag
 * generation for Ocean Surveys and to avoid using
 */
public class ETagGenerator {

//private static final Logger LOG = LoggerFactory.getLogger(ETagGenerator.class);

	/**
	 * Return an etag for the objectAsString passed as a parameter. The objectAsString should be a
	 * string representation of the entity you want to create a tag
	 */
	public EntityTag createEtag(String objectAsString) {
		try {
			MessageDigest md5Diggest = MessageDigest.getInstance("MD5");
			Preconditions.checkNotNull(objectAsString);
			byte[] bytes = objectAsString.getBytes("UTF-8");
			byte[] digest = md5Diggest.digest(bytes);

			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}

			return new EntityTag(sb.toString());
		} catch (NoSuchAlgorithmException e) {
//		LOG.error("Invalid algorithm used to create eTag", e);
			System.err.println("Invalid algorithm used to create eTag" + e);
			throw Throwables.propagate(e);
		} catch (UnsupportedEncodingException e) {
//		LOG.error("Invalid encoding to get bytes from toString representation creating eTag", e);
			System.err.println("Invalid encoding to get bytes from toString representation creating eTag" + e);
			throw Throwables.propagate(e);
		}
	}
}
