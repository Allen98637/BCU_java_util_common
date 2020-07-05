package common.battle.attack;

import java.util.ArrayList;
import java.util.List;

import common.battle.data.MaskAtk;
import common.battle.entity.AbEntity;

public class AttackSimple extends AttackAb {

	private final boolean range;

	public AttackSimple(AtkModelAb ent, int ATK, int t, int eab, int[][] pro, double p0, double p1, boolean isr, MaskAtk matk) {
		super(ent, ATK, t, eab, pro, p0, p1, matk);
		range = isr;
	}

	public AttackSimple(AtkModelAb ent, int ATK, int t, int eab, int[][] pro, double p0, double p1, MaskAtk mask) {
		this(ent, ATK, t, eab, pro, p0, p1, mask.isRange(), mask);
		touch = mask.getTarget();
		dire *= mask.getDire();
	}

	@Override
	public void capture() {
		double pos = model.getPos();
		List<AbEntity> le = model.b.inRange(touch, dire, sta, end);
		capt.clear();
		if (canon > -2)
			le.remove(model.b.ebase);
		if ((abi & AB_ONLY) == 0)
			capt.addAll(le);
		else
			for (AbEntity e : le)
				if (e.targetable(type))
					capt.add(e);
		if (!range) {
			if (capt.size() == 0)
				return;
			List<AbEntity> ents = new ArrayList<>();
			ents.add(capt.get(0));
			double dis = Math.abs(pos - ents.get(0).pos);
			for (AbEntity e : capt)
				if (Math.abs(pos - e.pos) < dis - 0.1) {
					ents.clear();
					ents.add(e);
					dis = Math.abs(pos - e.pos);
				} else if (Math.abs(pos - e.pos) < dis + 0.1)
					ents.add(e);
			capt.clear();
			int r = (int) (model.b.r.nextDouble() * ents.size());
			capt.add(ents.get(r));
		}
	}

	@Override
	public void excuse() {
		process();
		int layer = model.getLayer();
		if (proc[P_MOVEWAVE][0] > 0) {
			int[] conf = proc[P_MOVEWAVE];
			int dire = model.getDire();
			double p0 = model.getPos() + dire * conf[4];
			new ContMove(this, p0, conf[2], conf[1], 1, conf[3], conf[5], layer);
			return;
		}
		for (AbEntity e : capt)
			e.damaged(this);
		if (capt.size() > 0 && proc[P_WAVE][0] > 0) {
			int dire = model.getDire();
			int wid = dire == 1 ? W_E_WID : W_U_WID;
			int addp = (dire == 1 ? W_E_INI : W_U_INI) + wid / 2;
			double p0 = model.getPos() + dire * addp;
			// generate a wave when hits somebody

			new ContWaveDef(new AttackWave(this, p0, wid, WT_WAVE), p0, layer);
		}

		if (capt.size() > 0 && proc[P_VOLC][0] > 0) {
			int dire = model.getDire();
			int addp = proc[P_VOLC][1] + (int) (model.b.r.nextDouble() * (proc[P_VOLC][2] - proc[P_VOLC][1]));
			double p0 = model.getPos() + dire * addp;
			double sta = p0 + (dire == 1 ? W_VOLC_PIERCE : W_VOLC_INNER);
			double end = p0 - (dire == 1 ? W_VOLC_INNER : W_VOLC_PIERCE);

			new ContVolcano(new AttackVolcano(this, sta, end), p0, layer, proc[P_VOLC][3]);
		}
	}

}
