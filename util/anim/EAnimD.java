package common.util.anim;

import common.CommonStatic;
import common.system.P;
import common.system.fake.FakeGraphics;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class EAnimD<T extends Enum<T> & AnimI.AnimType<?, T>> extends EAnimI {

	public T type;

	protected MaAnim ma;

	public int f = -1;

	public EAnimD(AnimI<?, T> ia, MaModel mm, MaAnim anim, T t) {
		super(ia, mm);
		ma = anim;
		type = t;
	}

	@SuppressWarnings("unchecked")
	public void changeAnim(T t, boolean skip) {
		f = -1;
		ma = ((AnimD<?, T>) anim()).getMaAnim(t);
		type = t;
		if (skip)
			setTime(0);
	}

	public boolean done() {
		return f == ma.max;
	}

	@Override
	public void draw(FakeGraphics g, P ori, double siz) {
		if (f == -1) {
			f = 0;
			setup();
		}
		set(g);
		g.translate(ori.x, ori.y);
		for (EPart e : order) {
			P p = P.newP(siz, siz);
			e.drawPart(g, p);
			P.delete(p);
		}
	}

	/**
	 * Draw parts with specific opacity and size
	 * @param g Graphic
	 * @param ori Position
	 * @param siz Size
	 * @param opacity Opacity, range is 0 ~ 255
	 */
	public void drawBGEffect(FakeGraphics g, P ori, double siz, int opacity, double sizX, double sizY) {
		if(f == -1) {
			f = 0;
			setup();
		}
		set(g);
		g.translate(ori.x, ori.y);
		if(CommonStatic.getConfig().ref) {
			g.colRect(0, 0, 2, 2, 0, 255, 0, 255);
		}
		for (int i = 0; i < order.length; i++) {
			P p = P.newP(siz * sizX, siz * sizY);
			order[i].drawPartWithOpacity(g, p, opacity);
			P.delete(p);
		}
	}

	public double getBaseSizeX() {
		return mamodel.parts[0][8] * 1.0 / mamodel.ints[0];
	}

	public double getBaseSizeY() {
		return mamodel.parts[0][9] * 1.0 / mamodel.ints[0];
	}

	public void removeBasePivot() {
		mamodel.parts[0][6] = 0;
		mamodel.parts[0][7] = 0;
	}

	@Override
	public int ind() {
		return f;
	}

	@Override
	public int len() {
		return ma.max + 1;
	}

	@Override
	public void setTime(int value) {
		setup();
		f = value;
		ma.update(f, this, true);
	}

	public void setup() {
		ma.update(0, this, false);
	}

	@Override
	public void update(boolean rotate) {
		f++;
		ma.update(f, this, rotate);
	}

	@Override
	protected void performDeepCopy() {
		super.performDeepCopy();
		((EAnimD<?>) copy).setTime(f);
	}

	/**
	 * make this animation a component of another, used in warp and kb
	 */
	public void paraTo(EAnimD<?> base) {
		if (base == null)
			ent[0].setPara(null);
		else
			ent[0].setPara(base.ent[1]);
	}
}
