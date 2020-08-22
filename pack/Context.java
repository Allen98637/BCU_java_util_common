package common.pack;

import common.io.PackLoader.ZipDesc.FileDesc;

import java.io.File;
import java.io.IOException;

public interface Context {

	enum ErrType {
		FATAL, ERROR, WARN, INFO, NEW, CORRUPT
	}

	interface RunExc {

		void run() throws Exception;

	}

	interface SupExc<T> {

		T get() throws Exception;

	}

	static void check(boolean bool, String str, File f) throws IOException {
		if (!bool)
			throw new IOException("failed to " + str + " file " + f);
	}

	static void check(File f) throws IOException {
		if (!f.getParentFile().exists())
			check(f.getParentFile().mkdirs(), "create", f);
		if (!f.exists())
			check(f.createNewFile(), "create", f);
	}

	static void delete(File f) throws IOException {
		if (f == null || !f.exists())
			return;
		if (f.isDirectory())
			for (File i : f.listFiles())
				delete(i);
		check(f.delete(), "delete", f);
	}

	boolean confirmDelete();

	File getAssetFile(String string);

	File getLangFile(String file);

	File getPackFolder();

	File getUserFile(String string);

	File getWorkspaceFile(String relativePath);

	void initProfile();

	default boolean noticeErr(Context.RunExc r, ErrType t, String str) {
		try {
			r.run();
			return true;
		} catch (Exception e) {
			noticeErr(e, t, str);
			return false;
		}
	}

	default <T> T noticeErr(Context.SupExc<T> r, ErrType t, String str) {
		try {
			return r.get();
		} catch (Exception e) {
			noticeErr(e, t, str);
			return null;
		}
	}

	void noticeErr(Exception e, ErrType t, String str);

	boolean preload(FileDesc desc);

	void printErr(ErrType t, String str);

}