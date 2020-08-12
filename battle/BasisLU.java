package common.battle;

import java.util.List;

import common.io.InStream;
import common.io.json.JsonClass;
import common.io.json.JsonClass.RType;
import common.io.json.JsonField;
import common.io.json.JsonField.GenType;
import common.pack.PackData.Identifier;
import common.pack.UserProfile;
import common.system.Copable;
import common.util.BattleStatic;
import common.util.unit.Form;
import common.util.unit.Unit;

@JsonClass(read = RType.FILL)
public class BasisLU extends Basis implements Copable<BasisLU>, BattleStatic {

	public static BasisLU zread(InStream is) {
		int ver = getVer(is.nextString());
		if (ver >= 308)
			return zread$000308(is);
		return zread$000307(is);
	}

	private static int[] getRandom(int n) {
		int[] ans = new int[n];
		int a = 0;
		for (int i = 0; i < n; i++) {
			int x = (int) (Math.random() * 10);
			while ((a & (1 << x)) > 0)
				x = (int) (Math.random() * 10);
			a |= 1 << x;
			ans[i] = x;
		}
		return ans;
	}

	private static BasisLU zread$000307(InStream is) {
		String name = is.nextString();
		BasisLU ans = new BasisLU(307, is);
		ans.nyc = is.nextIntsB();
		ans.name = name;
		return ans;
	}

	private static BasisLU zread$000308(InStream is) {
		String name = is.nextString();
		BasisLU ans = new BasisLU(308, is);
		ans.nyc = is.nextIntsB();
		ans.name = name;
		return ans;
	}

	private final Treasure t;

	@JsonField(gen = GenType.FILL)
	public final LineUp lu;

	@JsonField(gen = GenType.FILL)
	public int[] nyc = new int[3];

	public BasisLU(BasisSet bs, LineUp line, String str) {
		t = new Treasure(this, bs.t());
		name = str;
		lu = line;
	}

	public BasisLU(BasisSet bs, LineUp line, String str, int[] ints) {
		this(bs, line, str);
		nyc = ints;
	}

	protected BasisLU(BasisSet bs) {
		t = new Treasure(this, bs.t());
		lu = new LineUp();
		name = "lineup " + bs.lb.size();
	}

	protected BasisLU(BasisSet bs, BasisLU bl) {
		t = new Treasure(this, bs.t());
		lu = new LineUp(bl.lu);
		name = "lineup " + bs.lb.size();
		nyc = bl.nyc.clone();
	}

	private BasisLU(int ver, InStream is) {
		lu = new LineUp(ver, ver >= 308 ? is.subStream() : is);
		t = new Treasure(this, ver, is);
	}

	@Override
	public BasisLU copy() {
		return new BasisLU(BasisSet.current(), this);
	}

	@Override
	public int getInc(int type) {
		return lu.inc[type];
	}

	public BasisLU randomize(int n) {
		BasisLU ans = copy();
		int[] rad = getRandom(n);
		List<Unit> list = UserProfile.getBCData().units.getList();
		list.remove(Identifier.parseInt(339, Unit.class).get());
		for (Form[] fs : ans.lu.fs)
			for (Form f : fs)
				if (f != null)
					list.remove(f.unit);
		for (int i = 0; i < n; i++) {
			Unit u = list.get((int) (Math.random() * list.size()));
			list.remove(u);
			ans.lu.setFS(u.forms[u.forms.length - 1], rad[i]);
		}
		ans.lu.arrange();
		return ans;
	}

	/**
	 * although the Treasure information is the same, this includes the effects of
	 * combo, so need to be an independent Treasure Object
	 */
	@Override
	public Treasure t() {
		return t;
	}

}
