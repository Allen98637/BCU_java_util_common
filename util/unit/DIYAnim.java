package common.util.unit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.util.Animable;
import common.util.anim.AnimC;
import common.util.anim.AnimU;
import common.util.anim.EAnimI;
import common.util.pack.Pack;
import io.InStream;
import io.OutStream;
import main.Opts;
import main.Printer;

public class DIYAnim extends Animable<AnimC> {

	public static final Map<String, DIYAnim> map = new TreeMap<>();

	public static AnimC getAnim(String str, boolean ene) {
		DIYAnim ai = DIYAnim.map.get(str);
		if (ai == null) {
			Collection<DIYAnim> cd = DIYAnim.map.values();
			if (cd.size() > 0)
				ai = cd.iterator().next();
			Printer.e("DIYAnim", 26, "Animation Missing: " + str);
			Opts.loadErr("Animation Missing: " + str);
		}
		if (ai == null) {
			AnimU au;
			if (ene)
				au = EnemyStore.getEnemy(0).anim;
			else
				au = Pack.def.us.ulist.get(0).forms[0].anim;
			return new AnimC("error", au);
		}
		return ai.getAnimC();
	}

	public static List<AnimC> getAnims() {
		List<AnimC> ans = new ArrayList<AnimC>();
		for (DIYAnim da : map.values())
			ans.add(da.anim);
		return ans;
	}

	public static void read() {
		map.clear();
		File f = new File("./res/anim/");
		if (f.exists())
			for (File fi : f.listFiles()) {
				if (!fi.isDirectory())
					continue;
				File check = new File("./res/anim/" + fi.getName() + "/" + fi.getName() + ".png");
				if (!check.exists())
					continue;
				new DIYAnim(fi.getName());
			}
	}

	public static OutStream writeAnim(AnimC anim) {
		OutStream os = OutStream.getIns();
		os.writeString("0.4.1");
		if (anim.inPool) {
			os.writeInt(0);
			os.writeString(anim.toString());
		} else {
			os.writeInt(1);
			os.accept(anim.write());
		}
		os.terminate();
		return os;
	}

	public static AnimC zread(InStream nam, boolean ene) {
		int ver = getVer(nam.nextString());
		if (ver >= 000401)
			return zread$000401(nam, ene);
		return null;
	}

	private static AnimC zread$000401(InStream is, boolean ene) {
		int type = is.nextInt();
		if (type == 0)
			return getAnim(is.nextString(), ene);
		return new AnimC(is.subStream());
	}

	public DIYAnim(AnimC ac) {
		anim = ac;
	}

	public DIYAnim(String str) {
		map.put(str, this);
		anim = new AnimC(str);
		anim.load();
	}

	public DIYAnim(String str, AnimC ac) {
		map.put(str, this);
		anim = ac;
	}

	public boolean deletable() {
		for (Pack pack : Pack.map.values()) {
			for (Enemy e : pack.es.getList())
				if (e.anim == anim)
					return false;
			for (Unit u : pack.us.ulist.getList())
				for (Form f : u.forms)
					if (f.anim == anim)
						return false;
		}
		return true;
	}

	public AnimC getAnimC() {
		return anim;
	}

	@Override
	public EAnimI getEAnim(int t) {
		if (anim == null)
			return null;
		return anim.getEAnim(t);
	}

	@Override
	public String toString() {
		return anim.toString();
	}

}
