package oliv.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DeleteDirectory {

	public static boolean deleteAll(File directoryRoot) {
		File[] allContents = directoryRoot.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteAll(file);
			}
		}
		return directoryRoot.delete();
	}

	public static boolean deleteDown(String path, int nbParent) throws IllegalArgumentException {
		List<Integer> separatorIndexes = new ArrayList<>();
		String tmpPath = path;
		boolean go = true;
		while (go) {
			int idx = tmpPath.lastIndexOf(File.separatorChar);
			if (idx > -1) {
				separatorIndexes.add(idx);
				tmpPath = tmpPath.substring(0, idx);
			} else {
				go = false;
			}
		}
		if (nbParent < 1 || nbParent > (separatorIndexes.size() - 1)) {
			throw new IllegalArgumentException(String.format("Unsuitable level %d, we have only %d separator(s).", nbParent, separatorIndexes.size()));
		}
		int level = nbParent - 1;
		int indexToUse = separatorIndexes.get(level);
		String dir2Delete = path.substring(0, indexToUse);
		Path pathToDelete = Paths.get(dir2Delete);
		System.out.println(String.format("Will delete %s", pathToDelete.toFile()));
		return deleteAll(pathToDelete.toFile());
	}

	private final static String DIRECTORY_NAME = "akeu";
	private final static String ROOT_DIRECTORY = "/tmp";

	public static void main(String... args) throws Exception {

		InputStream is = new FileInputStream(ROOT_DIRECTORY + File.separator + DIRECTORY_NAME + File.separator + "temp.txt");
		Field pathField = FileInputStream.class.getDeclaredField("path");
		pathField.setAccessible(true);
		String path = pathField.get(is).toString();
		System.out.println("Path:" + path);

		boolean ok = deleteDown(path, 1);
		System.out.println(String.format("Delete: %s", ok ? "OK" : "Not OK"));
	}
}
