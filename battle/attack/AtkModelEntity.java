package common.battle.attack;

import common.battle.data.CustomEntity;
import common.battle.data.MaskEntity;
import common.battle.data.PCoin;
import common.battle.entity.EAnimCont;
import common.battle.entity.EEnemy;
import common.battle.entity.EUnit;
import common.battle.entity.Entity;
import common.util.BattleObj;
import common.util.Data;
import common.util.Data.Proc.SUMMON;
import common.util.pack.EffAnim.DefEff;
import common.util.unit.Level;

import java.util.Comparator;

public abstract class AtkModelEntity extends AtkModelAb {

	/**
	 * @param e The entity
	 * @param d0 Level multiplication for EUnit, Magnification for EEnemy
	 * @return returns AtkModelEntity with specified magnification values
	 */
	public static AtkModelEntity getEnemyAtk(Entity e, double d0) {
		if (e instanceof EEnemy) {
			EEnemy ee = (EEnemy) e;
			return new AtkModelEnemy(ee, d0);
		}
		return null;
	}

	public static AtkModelEntity getUnitAtk(Entity e, double treasure, double level, PCoin pcoin, Level lv) {
		if(!(e instanceof EUnit))
			return null;

		return new AtkModelUnit(e, treasure, level, pcoin, lv);
	}

	protected final MaskEntity data;
	public final Entity e;
	protected final int[] atks, abis, act;
	protected final BattleObj[] acs;
	private final Proc[] sealed;

	protected AtkModelEntity(Entity ent, double d0, double d1) {
		super(ent.basis);
		e = ent;
		data = e.data;
		int[][] raw = data.rawAtkData();
		atks = new int[raw.length + 4];
		abis = new int[raw.length + 4];
		act = new int[raw.length + 4];
		acs = new BattleObj[raw.length + 4];
		for (int i = 0; i < raw.length; i++) {
			atks[i] = (int) (Math.round(raw[i][0] * d0) * d1);
			atks[i] = atks[i];
			abis[i] = raw[i][2];
			act[i] = data.getAtkModel(i).loopCount();
			acs[i] = new BattleObj();
		}
		setExtraAtks(raw, d0);
		sealed = new Proc[data.getAtkCount()];
		for (int i = 0; i < sealed.length; i++) {
			sealed[i] = Proc.blank();
			if (data.getAtkModel(i).getProc() != null)
				sealed[i].MOVEWAVE.set(data.getAtkModel(i).getProc().MOVEWAVE);
		}
	}

	protected AtkModelEntity(Entity ent, double d0, double d1, PCoin pc, Level lv) {
		super(ent.basis);
		e = ent;
		data = e.data;
		int[][] raw = data.rawAtkData();
		atks = new int[raw.length + 4];
		abis = new int[raw.length + 4];
		act = new int[raw.length + 4];
		acs = new BattleObj[raw.length + 4];
		for (int i = 0; i < raw.length; i++) {
			atks[i] = (int) (Math.round(raw[i][0] * d1) * d0);

			if (pc != null && lv != null && lv.getLvs().size() == pc.max.size())
				atks[i] = (int) Math.round((int) (pc.getAtkMultiplication(lv.getLvs()) * atks[i]) * (1 + ent.basis.b.getInc(Data.C_ATK) * 0.01));
			else
				atks[i] = (int) Math.round(atks[i] * (1 + ent.basis.b.getInc(Data.C_ATK) * 0.01));
			abis[i] = raw[i][2];
			act[i] = data.getAtkModel(i).loopCount();
			acs[i] = new BattleObj();
		}
		setExtraAtks(raw, d0);
		sealed = new Proc[data.getAtkCount()];
		for (int i = 0; i < sealed.length; i++) {
			sealed[i] = Proc.blank();
			if(data.getAtkModel(i).getProc() != null)
				sealed[i].MOVEWAVE.set(data.getAtkModel(i).getProc().MOVEWAVE);
		}
	}

	public void setExtraAtks(int[][] raw, double d0) {
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
		if (data.getGouge() != null) {
			atks[raw.length + 2] = (int) (data.getGouge().atk * d0);
			abis[raw.length + 2] = 1;
			acs[raw.length + 2] = new BattleObj();
			act[raw.length + 2] = data.getGouge().loopCount();
		}
		if (data.getResurface() != null) {
			atks[raw.length + 3] = (int) (data.getResurface().atk * d0);
			abis[raw.length + 3] = 1;
			acs[raw.length + 3] = new BattleObj();
			act[raw.length + 3] = data.getResurface().loopCount();
		}
	}

	@Override
	public int getAbi() {
		return e.getAbi();
	}

	/**
	 * get the attack, for display only
	 */
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

	/**
	 * generate attack entity
	 */
	public final AttackAb getAttack(int ind) {
		if (act[ind] == 0)
			return null;
		act[ind]--;
		Proc proc = Proc.blank();
		int atk = getAttack(ind, proc);
		double[] ints = inRange(ind);
		return new AttackSimple(e, this, atk, e.traits, getAbi(), proc, ints[0], ints[1], e.data.getAtkModel(ind), e.layer, data.isLD(ind) || data.isOmni(ind), ind);
	}

	/**
	 * Generate death surge when this entity is killed and the surge procs
	 */
	public void getDeathSurge() {
		Proc p = Proc.blank();
		int atk = getAttack(0, p);
		AttackSimple as = new AttackSimple(e, this, atk, e.traits, getAbi(), p, 0, 0, e.data.getAtkModel(0), 0, false);
		Proc.VOLC ds = e.getProc().DEATHSURGE;
		int addp = ds.dis_0 + (int) (b.r.nextDouble() * (ds.dis_1 - ds.dis_0));
		double p0 = getPos() + getDire() * addp;
		double sta = p0 + (getDire() == 1 ? W_VOLC_PIERCE : W_VOLC_INNER);
		double end = p0 - (getDire() == 1 ? W_VOLC_INNER : W_VOLC_PIERCE);

		new ContVolcano(new AttackVolcano(e, as, sta, end), p0, e.layer, ds.time, 0);
	}

	@Override
	public int getDire() {
		return e.dire;
	}

	@Override
	public double getPos() {
		return e.pos;
	}

	/**
	 * get the attack box for nth attack
	 */
	public double[] inRange(int ind) {
		int dire = e.dire;
		double d0, d1;
		d0 = d1 = e.pos;
		if (!data.isLD(ind) && !data.isOmni(ind)) {
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
		SUMMON proc = atk.getProc().SUMMON;
		if (proc.exists()) {
			SUMMON.TYPE conf = proc.type;
			if (conf.on_hit || (conf.on_kill && e.health <= 0)) {
				int rst = e.getProc().IMUSUMMON.mult;
				summon(proc, e, e, rst);
			}
		}
	}

	/**
	 * get the collide box bound
	 */
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
			if (getProc(ind).TIME.prob != 0 && (getProc(ind).TIME.prob == 100 || b.r.nextDouble() * 100 < getProc(ind).TIME.prob)) {
				if (getProc(ind).TIME.intensity > 0) {
					b.temp_s_stop = Math.max(b.temp_s_stop, getProc(ind).TIME.time);
					b.temp_inten = getProc(ind).TIME.intensity;
				} else {
					b.sn_temp_stop = Math.max(b.sn_temp_stop, getProc(ind).TIME.time);
					b.temp_n_inten = (float)Math.abs(getProc(ind).TIME.intensity) / b.sn_temp_stop;
				}
			}
			if (getProc(ind).THEME.prob != 0 && (getProc(ind).THEME.prob == 100 || b.r.nextDouble() * 100 < getProc(ind).THEME.prob))
				b.changeTheme(getProc(ind).THEME);
		}
	}

	protected abstract int getAttack(int ind, Proc proc);

	@Override
	protected int getLayer() {
		return e.layer;
	}

	public Proc getProc(int ind) {
		if (e.status[P_SEAL][0] > 0 && ind < sealed.length)
			return sealed[ind];
		return data.getAtkModel(ind).getProc();
	}

	protected void setProc(int ind, Proc proc) {
		String[] par = { "CRIT", "WAVE", "KB", "WARP", "STOP", "SLOW", "WEAK", "POISON", "MOVEWAVE", "CURSE", "SNIPER",
				"BOSS", "SEAL", "BREAK", "SUMMON", "SATK", "POIATK", "VOLC", "ARMOR", "SPEED", "MINIWAVE", "SHIELDBREAK" };

		for (String s0 : par)
			if (getProc(ind).get(s0).perform(b.r))
				if (s0.equals("SUMMON")) {
					SUMMON sprc = getProc(ind).SUMMON;
					SUMMON.TYPE conf = sprc.type;
					if (!conf.on_hit && !conf.on_kill)
						summon(sprc, e, acs[ind],0);
					else
						proc.SUMMON.set(sprc);
				} else
					proc.get(s0).set(getProc(ind).get(s0));

		if (data instanceof CustomEntity)
			for (int b : BCShareable) proc.getArr(b).set(getProc(ind).getArr(b));

		if (proc.CRIT.exists() && proc.CRIT.mult == 0)
			proc.CRIT.mult = 200;
		if (proc.KB.exists() && proc.KB.dis == 0)
			proc.KB.dis = KB_DIS[INT_KB];
		if (proc.KB.exists() && proc.KB.time == 0)
			proc.KB.time = KB_TIME[INT_KB];
		if (proc.BOSS.exists()) {
			b.lea.add(new EAnimCont(e.pos, e.layer, effas().A_SHOCKWAVE.getEAnim(DefEff.DEF)));
			b.lea.sort(Comparator.comparingInt(e -> e.layer));
		}
		if (proc.MINIWAVE.exists() && proc.MINIWAVE.multi == 0)
			proc.MINIWAVE.multi = 20;
	}

	protected abstract void summon(SUMMON sprc, Entity ent, Object acs, int resist);

}
