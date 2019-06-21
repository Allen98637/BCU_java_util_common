package common.util.entity;

import common.util.anim.EAnimD;
import common.util.anim.EAnimU;
import common.util.pack.EffAnim;
import common.util.system.P;
import common.util.system.fake.FakeGraphics;
import common.util.system.fake.FakeTransform;

public class WaprCont extends EAnimCont {

	private final EAnimU ent;
	private final EAnimD chara;

	public WaprCont(double p, int pa, int layer, EAnimU a) {
		super(p, layer, EffAnim.effas[A_W].getEAnim(pa));
		ent = a;
		chara = EffAnim.effas[A_W_C].getEAnim(pa);
	}

	@Override
	public void draw(FakeGraphics gra, P p, double psiz) {
		FakeTransform at = gra.getTransform();
		super.draw(gra, p, psiz);
		gra.setTransform(at);
		ent.paraTo(chara);
		ent.draw(gra, p, psiz);
		ent.paraTo(null);
	}

	@Override
	public void update() {
		super.update();
		chara.update(false);
	}

}
