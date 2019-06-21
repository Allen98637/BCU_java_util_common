package common.util.pack;

import common.util.anim.AnimI;
import common.util.anim.EAnimD;
import common.util.anim.MaAnim;
import common.util.anim.MaModel;
import common.util.system.fake.FakeImage;

public class WaveAnim extends AnimI {

	private final Background bg;
	private final MaModel mamodel;
	private final MaAnim maanim;

	private FakeImage[] parts;

	public WaveAnim(Background BG, MaModel model, MaAnim anim) {
		bg = BG;
		mamodel = model;
		maanim = anim;
	}

	@Override
	public void check() {
		if (parts == null)
			load();
	}

	@Override
	public EAnimD getEAnim(int t) {
		return new EAnimD(this, mamodel, maanim);
	}

	@Override
	public void load() {
		bg.check();
		parts = bg.parts;
	}

	@Override
	public String[] names() {
		return new String[] { "wave" };
	}

	@Override
	public FakeImage parts(int i) {
		check();
		return parts[i];
	}

}
