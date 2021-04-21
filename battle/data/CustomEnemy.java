package common.battle.data;

import common.battle.Basis;
import common.io.InStream;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.util.pack.Soul;
import common.util.unit.AbEnemy;
import common.util.unit.Enemy;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

@JsonClass
public class CustomEnemy extends CustomEntity implements MaskEnemy {

	public Enemy pack;

	@JsonField
	public int star, drop;

	public CustomEnemy() {
		rep = new AtkDataModel(this);
		atks = new AtkDataModel[1];
		atks[0] = new AtkDataModel(this);
		width = 320;
		speed = 8;
		hp = 10000;
		hb = 1;
		traits = new ArrayList<>();
		traits.add(UserProfile.getBCData().traits.get(TRAIT_RED));
		death = new Identifier<>(Identifier.DEF, Soul.class, 0);
	}

	public CustomEnemy copy(Enemy e) {
		CustomEnemy ce = new CustomEnemy();
		ce.importData(this);
		ce.pack = e;

		return ce;
	}

	public void fillData(int ver, InStream is) {
		zread(ver, is);
	}

	@Override
	public double getDrop() {
		return drop;
	}

	@Override
	public Enemy getPack() {
		return pack;
	}

	@Override
	public int getStar() {
		return star;
	}

	@Override
	public Set<AbEnemy> getSummon() {
		Set<AbEnemy> ans = new TreeSet<>();
		for (AtkDataModel adm : atks)
			if (adm.proc.SUMMON.prob > 0)
				ans.add(Identifier.getOr(adm.proc.SUMMON.id, AbEnemy.class));
		return ans;
	}

	@Override
	public void importData(MaskEntity de) {
		super.importData(de);
		if (de instanceof MaskEnemy) {
			MaskEnemy me = (MaskEnemy) de;
			star = me.getStar();
			drop = (int) me.getDrop();
		}
	}

	@Override
	public double multi(Basis b) {
		if (star > 0)
			return b.t().getStarMulti(star);
		if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_ALIEN)))
			return b.t().getAlienMulti();
		return 1;
	}

	private void zread(int val, InStream is) {
		val = getVer(is.nextString());
		if (val >= 400)
			zread$000400(is);

	}

	private void zread$000400(InStream is) {
		zreada(is);
		star = is.nextByte();
		drop = is.nextInt();
	}
}
