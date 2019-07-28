package common.battle.attack;

import common.battle.data.MaskEntity;
import common.battle.entity.EAnimCont;
import common.battle.entity.EEnemy;
import common.battle.entity.EUnit;
import common.battle.entity.Entity;
import common.util.BattleObj;
import common.util.pack.EffAnim;

public abstract class AtkModelEntity extends AtkModelAb {

	public static AtkModelEntity getIns(Entity e, double d0) {
		if (e instanceof EEnemy) {
			EEnemy ee = (EEnemy) e;
			return new AtkModelEnemy(ee, d0);
		}
		if (e instanceof EUnit) {
			EUnit eu = (EUnit) e;
			return new AtkModelUnit(eu, d0);
		}
		return null;

	}

	protected final MaskEntity data;
	protected final Entity e;
	protected final int[] atks, abis, act;
	protected final BattleObj[] acs;
	private final double ratk;

	protected AtkModelEntity(Entity ent, double d0) {
		super(ent.basis);
		ratk = d0;
		e = ent;
		data = e.data;
		int[][] raw = data.rawAtkData();
		atks = new int[raw.length + 2];
		abis = new int[raw.length + 2];
		act = new int[raw.length + 2];
		acs = new BattleObj[raw.length + 2];
		for (int i = 0; i < raw.length; i++) {
			atks[i] = (int) (raw[i][0] * d0);
			abis[i] = raw[i][2];
			act[i] = data.getAtkModel(i).loopCount();
			acs[i] = new BattleObj();
		}
		if (data.getRevenge() != null) {
			atks[raw.length] = (int) (data.getRevenge().atk * d0);
			abis[raw.length] = 1;
			acs[raw.length] = new BattleObj();
			act[raw.length] = data.getRevenge().loopCount();
		}
		if (data.getResurrection() != null) {
			atks[raw.length + 1] = (int) (data.getResurrection().atk * d0);
			abis[raw.length + 1] = 1;
			acs[raw.length + 1] = new BattleObj();
			act[raw.length + 1] = data.getResurrection().loopCount();
		}
	}

	@Override
	public int getAbi() {
		return e.getAbi();
	}

	/** get the attack, for display only */
	public int getAtk() {
		int ans = 0, temp = 0, c = 1;
		int[][] raw = data.rawAtkData();
		for (int i = 0; i < raw.length; i++)
			if (raw[i][1] > 0) {
				ans += temp / c;
				temp = data.getAtkModel(i).getDire() > 0 ? atks[i] : 0;
				c = 1;
			} else {
				temp += data.getAtkModel(i).getDire() > 0 ? atks[i] : 0;
				c++;
			}
		ans += temp / c;
		return ans;
	}

	/** generate attack entity */
	public final AttackAb getAttack(int ind) {
		if (act[ind] == 0)
			return null;
		act[ind]--;
		int[][] proc = new int[PROC_TOT][PROC_WIDTH];
		int atk = getAttack(ind, proc);
		double[] ints = inRange(ind);
		return new AttackSimple(this, atk, e.type, getAbi(), proc, ints[0], ints[1], e.data.getAtkModel(ind));
	}

	@Override
	public int getDire() {
		return e.dire;
	}

	@Override
	public double getPos() {
		return e.pos;
	}

	/** get the attack box for nth attack */
	public double[] inRange(int ind) {
		int dire = e.dire;
		double d0, d1;
		d0 = d1 = e.pos;
		if (!data.isLD()) {
			d0 += data.getRange() * dire;
			d1 -= data.getWidth() * dire;
		} else {
			d0 += data.getAtkModel(ind).getShortPoint() * dire;
			d1 += data.getAtkModel(ind).getLongPoint() * dire;
		}
		return new double[] { d0, d1 };
	}

	@Override
	public void invokeLater(AttackAb atk, Entity e) {
		if (atk.getProc(P_SUMMON)[0] > 0) {
			int[] proc = atk.getProc(P_SUMMON);
			int conf = proc[4];
			if ((conf & 64) > 0 || (conf & 128) > 0 && e.health <= 0)
				summon(proc, e, e);
		}
	}

	/** get the collide box bound */
	public double[] touchRange() {
		int dire = e.dire;
		double d0, d1;
		d0 = d1 = e.pos;
		d0 += data.getRange() * dire;
		d1 -= data.getWidth() * dire;
		return new double[] { d0, d1 };
	}

	protected void extraAtk(int ind) {
		if (data.getAtkModel(ind).getMove() != 0)
			e.pos += data.getAtkModel(ind).getMove() * e.dire;
		if (data.getAtkModel(ind).getAltAbi() != 0)
			e.altAbi(data.getAtkModel(ind).getAltAbi());
		if (abis[ind] == 1) {
			if (b.r.nextDouble() * 100 < getProc(ind, P_TIME, 0))
				b.temp_s_stop = Math.max(b.temp_s_stop, getProc(ind, P_TIME, 1));
			if (b.r.nextDouble() * 100 < getProc(ind, P_THEME, 0))
				b.changeTheme(getProc(ind, P_THEME, 2), getProc(ind, P_THEME, 1));
		}
	}

	protected abstract int getAttack(int ind, int[][] proc);

	@Override
	protected int getLayer() {
		return e.layer;
	}

	protected int getProc(int ind, int type, int ety) {
		if (e.status[P_SEAL][0] > 0 && type != P_MOVEWAVE)
			return 0;
		return data.getAtkModel(ind).getProc(type)[ety];
	}

	protected void setProc(int ind, int[][] proc) {
		if (b.r.nextDouble() * 100 < getProc(ind, P_CRIT, 0)) {
			int crit = getProc(ind, P_CRIT, 1);
			proc[P_CRIT][0] = crit == 0 ? 200 : crit;
		}
		if (b.r.nextDouble() * 100 < getProc(ind, P_WAVE, 0))
			proc[P_WAVE][0] = getProc(ind, P_WAVE, 1);

		if (b.r.nextDouble() * 100 < getProc(ind, P_KB, 0)) {
			int time = getProc(ind, P_KB, 1);
			int dis = getProc(ind, P_KB, 2);
			proc[P_KB][0] = dis == 0 ? KB_DIS[INT_KB] : dis;
			proc[P_KB][1] = time == 0 ? KB_TIME[INT_KB] : time;
		}
		if (b.r.nextDouble() * 100 < getProc(ind, P_WARP, 0)) {
			proc[P_WARP][0] = getProc(ind, P_WARP, 1);
			proc[P_WARP][1] = getProc(ind, P_WARP, 2);
		}
		if (b.r.nextDouble() * 100 < getProc(ind, P_STOP, 0))
			proc[P_STOP][0] = getProc(ind, P_STOP, 1);
		if (b.r.nextDouble() * 100 < getProc(ind, P_SLOW, 0))
			proc[P_SLOW][0] = getProc(ind, P_SLOW, 1);
		if (b.r.nextDouble() * 100 < getProc(ind, P_WEAK, 0)) {
			proc[P_WEAK][0] = getProc(ind, P_WEAK, 1);
			proc[P_WEAK][1] = getProc(ind, P_WEAK, 2);
		}
		if (b.r.nextDouble() * 100 < getProc(ind, P_POISON, 0)) {
			proc[P_POISON][0] = getProc(ind, P_POISON, 1);
			proc[P_POISON][1] = (int) (getProc(ind, P_POISON, 2) * ratk);
			proc[P_POISON][2] = getProc(ind, P_POISON, 3);
			proc[P_POISON][3] = getProc(ind, P_POISON, 4);
		}
		if (b.r.nextDouble() * 100 < getProc(ind, P_MOVEWAVE, 0))
			for (int i = 0; i < PROC_WIDTH; i++)
				proc[P_MOVEWAVE][i] = getProc(ind, P_MOVEWAVE, i);
		if (b.r.nextDouble() * 100 < getProc(ind, P_CURSE, 0))
			proc[P_CURSE][0] = getProc(ind, P_CURSE, 1);

		if (b.r.nextDouble() * 100 < getProc(ind, P_SNIPER, 0))
			proc[P_SNIPER][0] = 1;
		if (b.r.nextDouble() * 100 < getProc(ind, P_BOSS, 0)) {
			proc[P_BOSS][0] = 1;
			b.lea.add(new EAnimCont(e.pos, e.layer, EffAnim.effas[A_SHOCKWAVE].getEAnim(0)));
		}

		if (b.r.nextDouble() * 100 < getProc(ind, P_SEAL, 0))
			proc[P_SEAL][0] = getProc(ind, P_SEAL, 1);

		if (b.r.nextDouble() * 100 < getProc(ind, P_BREAK, 0))
			proc[P_BREAK][0] = 1;
		if (b.r.nextDouble() * 100 < getProc(ind, P_SUMMON, 0)) {
			int[] sprc = data.getAtkModel(ind).getProc(P_SUMMON);
			int conf = sprc[4];
			if ((conf & 192) == 0)
				summon(sprc, e, acs[ind]);
			else
				proc[P_SUMMON] = sprc;
		}
		if (getProc(ind, P_SATK, 0) > 0 && b.r.nextDouble() * 100 < getProc(ind, P_SATK, 0)) {
			proc[P_SATK][0] = getProc(ind, P_SATK, 1);
		}
	}

	protected abstract void summon(int[] proc, Entity ent, Object acs);

}
