package common.battle.attack;

import common.battle.data.MaskAtk;
import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import common.util.BattleObj;

import java.util.ArrayList;
import java.util.List;

public abstract class AttackAb extends BattleObj {

	public final int atk, type, abi;
	public final AtkModelAb model;
	public final AttackAb origin;
	public final MaskAtk matk;

	public int touch = TCH_N, dire, canon = -2, waveType = 0;

	protected final Proc proc;
	protected final List<AbEntity> capt = new ArrayList<>();
	protected double sta, end;

	protected AttackAb(AtkModelAb ent, int ATK, int t, int eab, Proc pro, double p0, double p1, MaskAtk matk) {
		dire = ent.getDire();
		origin = this;
		model = ent;
		type = t;
		atk = ATK;
		proc = pro;
		abi = eab;
		sta = p0;
		end = p1;
		this.matk = matk;
	}

	protected AttackAb(AttackAb a, double STA, double END) {
		dire = a.dire;
		origin = a.origin;
		model = a.model;
		atk = a.atk;
		abi = a.abi;
		type = a.type;
		proc = a.proc;
		touch = a.touch;
		canon = a.canon;
		sta = STA;
		end = END;
		this.matk = a.matk;
	}

	/**
	 * capture the entities
	 */
	public abstract void capture();

	/**
	 * apply this attack to the entities captured
	 */
	public abstract void excuse();

	public Proc getProc() {
		return proc;
	}

	protected void process() {
		for (AbEntity ae : capt) {
			if (ae instanceof Entity) {
				Entity e = (Entity) ae;
				if (e.getProc().CRITI.type == 2)
					proc.CRIT.clear();
			}
		}
	}

}
