package common.util.entity.attack;

import common.util.pack.NyCastle;
import common.util.system.P;
import common.util.system.fake.FakeGraphics;
import io.BCMusic;

public class ContWaveCanon extends ContWaveAb {

	private final int canid;

	public ContWaveCanon(AttackWave a, double p, int id) {
		super(a, p, NyCastle.atks[id].getEAnim(1), 9);
		canid = id;
	}

	@Override
	public void draw(FakeGraphics gra, P p, double psiz) {
		drawAxis(gra, p, psiz);
		if (canid == 0)
			psiz *= 1.25;
		else
			psiz *= 0.5 * 1.25;
		P pus = canid == 0 ? new P(9, 40) : new P(-72, 0);
		anim.draw(gra, p.plus(pus, -psiz), psiz * 2);
	}

	public double getSize() {
		return 2.5;
	}

	@Override
	protected void nextWave() {
		double np = pos - 405;
		new ContWaveCanon(new AttackWave(atk, np, NYRAN[canid]), np, canid);
		BCMusic.setSE(SE_CANNON[canid][1]);
	}

}
