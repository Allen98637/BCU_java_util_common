package common.util.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import common.CommonStatic;
import common.battle.BasisLU;
import common.battle.BasisSet;
import common.system.files.VFile;
import common.util.stage.MapColc.DefMapColc;

public class RandStage {

	private static final int[][] randRep = new int[5][];

	public static BasisLU getLU(int att) {
		double r = Math.random() * 100;
		for (int i = 0; i < 10; i++)
			if (r < randRep[att][i])
				return BasisSet.current().sele.randomize(10 - i);
			else
				r -= randRep[att][i];
		return BasisSet.current().sele;
	}

	public static Stage getStage(int sta) {
		DefMapColc mc = DefMapColc.getMap("N");
		if (sta == 47)
			return mc.maps[48].list.get(0);
		List<Stage> l = new ArrayList<Stage>();
		l.addAll(mc.maps[sta].list);
		l.addAll(mc.maps[sta + 1].list);
		return l.get((int) (Math.random() * l.size()));
	}

	public static void read() {
		Queue<String> qs = VFile.readLine("./org/stage/D/RandomDungeon_000.csv");
		for (int i = 0; i < 5; i++)
			randRep[i] = CommonStatic.parseIntsN(qs.poll());
	}

}
