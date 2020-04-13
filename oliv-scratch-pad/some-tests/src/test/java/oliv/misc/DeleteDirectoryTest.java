package oliv.misc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DeleteDirectoryTest {

	String treeLocation = "", fileLocation = "";

	@Before
	public void createTreeStructure() {
		System.out.println("Creating directory structure");
		// Create directory, and a file in it
		File root = new File("/tmp");
		if (!root.exists()) {
			fail("There is no /tmp directory on this system");
		} else {
			StringBuffer sb = new StringBuffer()
					.append("/tmp")
					.append(File.separator)
					.append("services")
					.append(File.separator)
					.append(System.currentTimeMillis())
					.append(File.separator)
					.append("DummyServiceName");
			treeLocation = sb.toString();
			File dir = new File(treeLocation);
			if (!dir.exists()) {
				boolean ok = dir.mkdirs();
				assertTrue(String.format("Could not create directory %s", treeLocation), ok);
			}
			fileLocation = sb.append(File.separator).append("DummyFile.txt").toString();
			File newFile = new File(fileLocation);
			try {
				FileOutputStream fos = new FileOutputStream(newFile);
				fos.write("dummy-line\n".getBytes());
				fos.flush();
				fos.close();
			} catch (IOException ioe) {
				fail(ioe.getMessage());
			}
			if (!newFile.exists()) {
				fail("File was not created");
			}
		}
	}

	@Test
	public void testDropDirectory() {
		// Delete containing directory
		try {
			DeleteDirectory.deleteDown(fileLocation, 0);
			fail("Exception should have been thrown here");
		} catch (IllegalArgumentException iae) {
			// As expected.
			System.err.println(String.format("Caught expected exception: %s", iae.getMessage()));
		}
		try {
			DeleteDirectory.deleteDown(fileLocation, 10);
			fail("Exception should have been thrown here");
		} catch (IllegalArgumentException iae) {
			// As expected.
			System.err.println(String.format("Caught expected exception: %s", iae.getMessage()));
		}
		DeleteDirectory.deleteDown(fileLocation, 2); // File's grand-parent folder
		File dirToCheck = new File(treeLocation);
		assertFalse("Directory was not dropped", dirToCheck.exists());
		// Deleted Root
		String rootDirectory = treeLocation.substring(0, treeLocation.lastIndexOf(File.separator));
		System.out.println(String.format("Checking %s", rootDirectory));
		dirToCheck = new File(rootDirectory);
		assertFalse("Directory was not dropped", dirToCheck.exists());
	}

	@After
	public void cleanup() {
		System.out.println("Cleaning up");
		StringBuffer sb = new StringBuffer()
				.append("/tmp")
				.append(File.separator)
				.append("services");
		DeleteDirectory.deleteAll(new File(sb.toString()));
	}
}
