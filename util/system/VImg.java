package common.util.system;

import java.io.IOException;

import common.util.ImgCore;
import common.util.anim.ImgCut;
import common.util.system.fake.FakeImage;
import common.util.system.files.FileData;
import common.util.system.files.VFile;

public class VImg extends ImgCore {

	private final VFile<? extends FileData> file;

	public String name = "";

	public FakeImage bimg = null;

	private boolean loaded = false;
	private ImgCut ic;
	private String marker;

	public VImg(Object o) {
		if (o instanceof String)
			file = VFile.getFile((String) o);
		else if (o instanceof VFile)
			file = (VFile<?>) o;
		else
			file = null;

		if (file == null)
			try {
				bimg = FakeImage.read(o);
			} catch (IOException e) {
				e.printStackTrace();
			}
		loaded = bimg != null;
	}

	public synchronized void check() {
		if (!loaded)
			load();
	}

	public FakeImage getImg() {
		check();
		return bimg;
	}

	public void mark(String string) {
		marker = string;
		if (bimg != null)
			bimg.mark(string);
	}

	public void setCut(ImgCut cut) {
		ic = cut;
	}

	public void setImg(Object img) {
		try {
			bimg = FakeImage.read(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (ic != null)
			bimg = ic.cut(bimg)[0];
		loaded = true;
	}

	@Override
	public String toString() {
		return file == null ? name.length() == 0 ? "img" : name : file.getName();
	}

	private void load() {
		loaded = true;
		if (file == null)
			return;
		bimg = file.getData().getImg();
		if (bimg == null)
			return;
		if (marker != null)
			bimg.mark(marker);
		if (ic != null)
			bimg = ic.cut(bimg)[0];
		try {
			bimg.getWidth();
		} catch (Exception e) {
			bimg = null;
		}
	}

}
