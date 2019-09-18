package common.battle.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.CommonStatic;
import common.CommonStatic.BattleConst;
import common.battle.StageBasis;
import common.battle.attack.AtkModelEntity;
import common.battle.attack.AttackAb;
import common.battle.data.AtkDataModel;
import common.battle.data.MaskEntity;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.BattleObj;
import common.util.Data;
import common.util.ImgCore;
import common.util.anim.AnimD;
import common.util.anim.EAnimD;
import common.util.anim.EAnimU;
import common.util.pack.EffAnim;
import common.util.pack.Soul;
import common.util.pack.SoulStore;

/** Entity class for units and enemies */
public abstract class Entity extends AbEntity {

	public static class AnimManager extends BattleObj {

		private final Entity e;
		private final int[][] status;

		/**
		 * dead FSM time <br>
		 * -1 means not dead<br>
		 * positive value means time remain for death anim to play
		 */
		public int dead = -1;

		/** KB anim, null means not being KBed, can have various value during battle */
		private EAnimD back;

		/** entity anim */
		private final EAnimU anim;

		/** corpse anim */
		private EAnimD corpse;

		/** soul anim, null means not dead yet */
		private EAnimD soul;

		/** responsive effect FSM time */
		private int efft;

		/** responsive effect FSM type */
		private int eftp;

		/**
		 * on-entity effect icons<br>
		 * index defined by Data.A_()
		 */
		private final EAnimD[] effs = new EAnimD[A_TOT];

		private AnimManager(Entity ent, EAnimU ea) {
			e = ent;
			anim = ea;
			status = e.status;
		}

		/** draw this entity */
		public void draw(FakeGraphics gra, P p, double siz) {
			if (dead > 0) {
				soul.draw(gra, p, siz);
				return;
			}
			FakeTransform at = gra.getTransform();
			if (corpse != null)
				corpse.draw(gra, p, siz);
			if (corpse == null || status[P_REVIVE][1] < Data.REVIVE_SHOW_TIME) {
				if (corpse != null) {
					gra.setTransform(at);
					anim.changeAnim(0);
				}
			} else
				return;

			anim.paraTo(back);
			if (e.kbTime == 0 || e.kb.kbType != INT_WARP)
				anim.draw(gra, p, siz);
			anim.paraTo(null);
			gra.setTransform(at);
			if (ImgCore.ref)
				e.drawAxis(gra, p, siz);
		}

		/** draw the effect icons */
		public void drawEff(FakeGraphics g, P p, double siz) {
			if (dead != -1)
				return;
			FakeTransform at = g.getTransform();
			int EWID = 48;
			double x = p.x;
			if (effs[eftp] != null) {
				effs[eftp].draw(g, p, siz);
			}
			for (EAnimD eae : effs) {
				if (eae == null)
					continue;
				g.setTransform(at);
				eae.draw(g, new P(x, p.y), siz);
				x -= EWID * e.dire * siz;
			}
		}

		/** get a effect icon */
		public void getEff(int t) {
			int dire = e.dire;
			if (t == INV) {
				effs[eftp] = null;
				eftp = A_EFF_INV;
				effs[eftp] = EffAnim.effas[eftp].getEAnim(0);
				efft = EffAnim.effas[eftp].len(0);
			}
			if (t == P_WAVE) {
				int id = dire == -1 ? A_WAVE_INVALID : A_E_WAVE_INVALID;
				effs[id] = EffAnim.effas[id].getEAnim(0);
				status[P_WAVE][0] = EffAnim.effas[id].len(0);
			}
			if (t == STPWAVE) {
				effs[eftp] = null;
				eftp = dire == -1 ? A_WAVE_STOP : A_E_WAVE_STOP;
				effs[eftp] = EffAnim.effas[eftp].getEAnim(0);
				efft = EffAnim.effas[eftp].len(0);
			}
			if (t == INVWARP) {
				effs[eftp] = null;
				eftp = dire == -1 ? A_FARATTACK : A_E_FARATTACK;
				effs[eftp] = EffAnim.effas[eftp].getEAnim(0);
				efft = EffAnim.effas[eftp].len(0);
			}
			if (t == P_STOP) {
				int id = dire == -1 ? A_STOP : A_E_STOP;
				effs[id] = EffAnim.effas[id].getEAnim(0);
			}
			if (t == P_IMUATK) {
				effs[A_IMUATK] = EffAnim.effas[A_IMUATK].getEAnim(0);
			}
			if (t == P_SLOW) {
				int id = dire == -1 ? A_SLOW : A_E_SLOW;
				effs[id] = EffAnim.effas[id].getEAnim(0);
			}
			if (t == P_WEAK) {
				int id = dire == -1 ? A_DOWN : A_E_DOWN;
				effs[id] = EffAnim.effas[id].getEAnim(0);
			}
			if (t == P_CURSE) {
				int id = A_CURSE;
				effs[id] = EffAnim.effas[id].getEAnim(0);
			}
			if (t == P_POISON) {
				int mask = status[P_POISON][0];
				for (int i = 0; i < A_POIS.length; i++)
					if ((mask & (1 << i)) > 0) {
						int id = A_POIS[i];
						effs[id] = EffAnim.effas[id].getEAnim(0);
					}

			}
			if (t == P_SEAL) {
				int id = A_SEAL;
				effs[id] = EffAnim.effas[id].getEAnim(0);
			}
			if (t == P_STRONG) {
				int id = dire == -1 ? A_UP : A_E_UP;
				effs[id] = EffAnim.effas[id].getEAnim(0);
			}
			if (t == P_LETHAL) {
				int id = dire == -1 ? A_SHIELD : A_E_SHIELD;
				AnimD ea = EffAnim.effas[id];
				status[P_LETHAL][1] = ea.len(0);
				effs[id] = ea.getEAnim(0);
				CommonStatic.def.setSE(SE_LETHAL);
			}
			if (t == P_WARP) {
				AnimD ea = EffAnim.effas[A_W];
				int pa = status[P_WARP][2];
				e.basis.lea.add(new WaprCont(e.pos, pa, e.layer, anim));
				CommonStatic.def.setSE(pa == 0 ? SE_WARP_ENTER : SE_WARP_EXIT);
				status[P_WARP][pa] = ea.len(pa);

			}

			if (t == BREAK_ABI) {
				int id = dire == -1 ? A_U_E_B : A_E_B;
				effs[id] = EffAnim.effas[id].getEAnim(0);
				status[P_BREAK][0] = effs[id].len();
				CommonStatic.def.setSE(SE_BARRIER_ABI);
			}
			if (t == BREAK_ATK) {
				int id = dire == -1 ? A_U_E_B : A_E_B;
				effs[id] = EffAnim.effas[id].getEAnim(1);
				status[P_BREAK][0] = effs[id].len();
				CommonStatic.def.setSE(SE_BARRIER_ATK);
			}
			if (t == BREAK_NON) {
				int id = dire == -1 ? A_U_B : A_B;
				effs[id] = EffAnim.effas[id].getEAnim(4);
				status[P_BREAK][0] = effs[id].len();
				CommonStatic.def.setSE(SE_BARRIER_NON);
			}
		}

		/** update effect icons animation */
		private void checkEff() {
			int dire = e.dire;
			if (efft == 0)
				effs[eftp] = null;
			if (status[P_STOP][0] == 0) {
				int id = dire == -1 ? A_STOP : A_E_STOP;
				effs[id] = null;
			}
			if (status[P_SLOW][0] == 0) {
				int id = dire == -1 ? A_SLOW : A_E_SLOW;
				effs[id] = null;
			}
			if (status[P_WEAK][0] == 0) {
				int id = dire == -1 ? A_DOWN : A_E_DOWN;
				effs[id] = null;
			}
			if (status[P_CURSE][0] == 0) {
				int id = A_CURSE;
				effs[id] = null;
			}
			if (status[P_IMUATK][0] == 0) {
				int id = A_IMUATK;
				effs[id] = null;
			}
			if (status[P_POISON][0] == 0) {
				int id = A_POI0;
				effs[id] = null;
			}
			if (status[P_SEAL][0] == 0) {
				int id = A_SEAL;
				effs[id] = null;
			}
			if (status[P_LETHAL][1] == 0) {
				int id = dire == -1 ? A_SHIELD : A_E_SHIELD;
				effs[id] = null;
			} else
				status[P_LETHAL][1]--;
			if (status[P_WAVE][0] == 0) {
				int id = dire == -1 ? A_WAVE_INVALID : A_E_WAVE_INVALID;
				effs[id] = null;
			} else
				status[P_WAVE][0]--;
			if (status[P_STRONG][0] == 0) {
				int id = dire == -1 ? A_UP : A_E_UP;
				effs[id] = null;
			}
			if (status[P_BREAK][0] == 0) {
				int id = dire == -1 ? A_U_B : A_B;
				effs[id] = null;
			} else
				status[P_BREAK][0]--;
			efft--;

		}

		/**
		 * process kb animation <br>
		 * called when kb is applied
		 */
		private void kbAnim() {
			int t = e.kb.kbType;
			if (t != INT_SW && t != INT_WARP)
				setAnim(3);
			else {
				setAnim(0);
				anim.update(false);
			}
			if (t == INT_WARP) {
				e.kbTime = status[P_WARP][0];
				getEff(P_WARP);
				status[P_WARP][2] = 1;
			}
			if (t == INT_KB)
				e.kbTime = status[P_KB][0];
			if (t == INT_HB)
				back = EffAnim.effas[A_KB].getEAnim(0);
			if (t == INT_SW)
				back = EffAnim.effas[A_KB].getEAnim(1);
			if (t == INT_ASS)
				back = EffAnim.effas[A_KB].getEAnim(2);

			// Z-kill icon
			if (e.health <= 0 && e.zx.tempZK && e.zx.canRevive() > 0) {
				EAnimD eae = EffAnim.effas[A_Z_STRONG].getEAnim(0);
				e.basis.lea.add(new EAnimCont(e.pos, e.layer, eae));
				CommonStatic.def.setSE(SE_ZKILL);
			}
		}

		/** set kill anim */
		private void kill() {
			Soul s = SoulStore.getSoul(e.data.getDeathAnim());
			dead = s == null ? 0 : (soul = s.getEAnim(0)).len();
			CommonStatic.def.setSE(e.basis.r.irDouble() < 0.5 ? SE_DEATH_0 : SE_DEATH_1);
		}

		private int setAnim(int t) {
			if (anim.type != t)
				anim.changeAnim(t);
			return anim.len();
		}

		private void update() {
			checkEff();

			for (int i = 0; i < effs.length; i++)
				if (effs[i] != null)
					effs[i].update(false);

			if (status[P_STOP][0] == 0 && (e.kbTime == 0 || e.kb.kbType != INT_SW))
				anim.update(false);
			if (back != null)
				back.update(false);
			if (dead > 0) {
				soul.update(false);
				dead--;
			}
			if (e.data.getResurrection() != null && dead >= 0) {
				AtkDataModel adm = e.data.getResurrection();
				if (soul == null || adm.pre == soul.len() - dead)
					e.basis.getAttack(e.aam.getAttack(e.data.getAtkCount() + 1));
			}
		}

	}

	private static class AtkManager extends BattleObj {

		/** atk FSM time */
		private int atkTime;

		/** attack times remain */
		private int loop;

		/** atk id primarily for display */
		private int tempAtk = -1;

		private final Entity e;

		/** const field, attack count */
		private final int multi;

		/** atk loop FSM type */
		private int preID;

		/** pre-atk time const field */
		private final int[] pres;

		/** atk loop FSM time */
		private int preTime;

		private AtkManager(Entity ent) {
			e = ent;
			int[][] raw = e.data.rawAtkData();
			pres = new int[multi = raw.length];
			for (int i = 0; i < multi; i++)
				pres[i] = raw[i][1];
			loop = e.data.getAtkLoop();
		}

		private void setUp() {
			atkTime = e.data.getAnimLen();
			loop--;
			preID = 0;
			preTime = pres[0];
			e.anim.setAnim(2);
		}

		private void stopAtk() {
			if (atkTime > 0) {
				atkTime = 0;
				preTime = 0;
				if (preID == multi)
					e.waitTime = e.data.getTBA();
			}
		}

		/** update attack state */
		private void updateAttack() {
			atkTime--;
			if (preTime >= 0) {
				if (preTime == 0) {
					int atk0 = preID;
					while (++preID < multi && pres[preID] == 0)
						;
					tempAtk = (int) (atk0 + e.basis.r.nextDouble() * (preID - atk0));
					e.basis.getAttack(e.aam.getAttack(tempAtk));
					if (preID < multi)
						preTime = pres[preID];
				}
				preTime--;
			}
			if (atkTime == 0) {
				e.waitTime = e.data.getTBA();
				e.anim.setAnim(1);
			}
		}
	}

	private static class KBManager extends BattleObj {

		/** KB FSM type */
		private int kbType;

		private final Entity e;

		/** remaining distance to KB */
		private double kbDis;

		/** temp field to store wanted KB length */
		private double tempKBdist;

		/** temp field to store wanted KB type */
		private int tempKBtype = -1;

		private KBManager(Entity ent) {
			e = ent;
		}

		/** process the interruption received */
		private void doInterrupt() {
			int t = tempKBtype;
			if (t == -1)
				return;
			double d = tempKBdist;
			tempKBtype = -1;
			e.clearState();
			kbType = t;
			e.kbTime = KB_TIME[t];
			kbDis = d;
			e.anim.kbAnim();
		}

		private void interrupt(int t, double d) {
			if (t == INT_ASS && (e.getAbi() & AB_SNIPERI) > 0) {
				e.anim.getEff(INV);
				return;
			}
			if (t == INT_SW && (e.getAbi() & AB_IMUSW) > 0) {
				e.anim.getEff(INV);
				return;
			}
			int prev = tempKBtype;
			if (prev == -1 || KB_PRI[t] >= KB_PRI[prev]) {
				tempKBtype = t;
				tempKBdist = d;
			}
		}

		private void kbmove(double mov) {
			if (mov < 0)
				e.updateMove(-mov, -mov);
			else {
				double lim = e.getLim();
				e.pos -= (mov < lim ? mov : lim) * e.dire;
			}
		}

		/**
		 * update KB state <br>
		 * in KB state: deal with warp, KB go back, and anim change <br>
		 * end of KB: check whether it's killed, deal with revive
		 */
		private void updateKB() {
			if (kbType != INT_WARP) {
				double mov = kbDis / e.kbTime;
				kbDis -= mov;
				kbmove(mov);
			} else {
				e.anim.setAnim(0);
				if (e.status[P_WARP][0] > 0)
					e.status[P_WARP][0]--;
				if (e.status[P_WARP][1] > 0)
					e.status[P_WARP][1]--;
				EffAnim ea = EffAnim.effas[A_W];
				if (e.kbTime == ea.len(1)) {
					kbmove(kbDis);
					kbDis = 0;
					e.anim.getEff(P_WARP);
					e.status[P_WARP][2] = 0;
				}
			}
			if (kbType == INT_HB && e.data.getRevenge() != null)
				if (KB_TIME[INT_HB] - e.kbTime == e.data.getRevenge().pre)
					e.basis.getAttack(e.aam.getAttack(e.data.getAtkCount()));
			e.kbTime--;
			if (e.kbTime == 0) {
				e.anim.back = null;
				e.anim.setAnim(0);

				if (e.health <= 0)
					e.preKill();
			}
		}

	}

	private static class PoisonToken extends BattleObj {

		private final Entity e;

		private final List<int[]> list = new ArrayList<>();

		private PoisonToken(Entity ent) {
			e = ent;
		}

		private void add(int[] is) {
			if ((is[4] & 4) > 0)
				list.removeIf(e -> (e[4] & 4) > 0 && type(e) == type(is));
			list.add(is);
			getMax();
		}

		private void damage(int dmg, int type) {
			type &= 7;
			long mul = type == 0 ? 100 : type == 1 ? e.maxH : type == 2 ? e.health : (e.maxH - e.health);
			e.damage += mul * dmg / 100;

		}

		private void getMax() {
			int max = 0;
			for (int[] ws : list)
				max |= 1 << type(ws);
			e.status[P_POISON][0] = max;
		}

		private int type(int[] ws) {
			return (ws[4] & 3) + (ws[1] < 0 ? 4 : 0);
		}

		private void update() {
			for (int[] ws : list)
				if (ws[0] > 0) {
					ws[0]--;
					ws[3]--;
					if (e.health > 0 && ws[3] <= 0) {
						damage(ws[1], type(ws));
						ws[3] += ws[2];
					}
				}
			list.removeIf(w -> w[0] <= 0);
			getMax();
		}

	}

	private static class WeakToken extends BattleObj {

		private final Entity e;

		private final List<int[]> list = new ArrayList<>();

		private WeakToken(Entity ent) {
			e = ent;
		}

		private void add(int[] is) {
			list.add(is);
			getMax();
		}

		private void getMax() {
			int max = 0;
			int val = 100;
			for (int[] ws : list) {
				max = Math.max(max, ws[0]);
				val = Math.min(val, ws[1]);
			}
			e.status[P_WEAK][0] = max;
			e.status[P_WEAK][1] = val;
		}

		private void update() {
			for (int[] ws : list)
				ws[0]--;
			list.removeIf(w -> w[0] <= 0);
			getMax();
		}

	}

	private static class ZombX extends BattleObj {

		private final Entity e;

		private final Set<Entity> list = new HashSet<>();

		/** temp field: marker for zombie killer */
		private boolean tempZK;

		private int extraRev = 0;

		private ZombX(Entity ent) {
			e = ent;
		}

		private int canRevive() {
			if (e.status[P_REVIVE][0] != 0)
				return 1;
			int tot = totExRev();
			if (tot == -1 || tot > extraRev)
				return 2;
			return 0;
		}

		private boolean canZK() {
			if ((e.getProc(P_REVIVE, 5) & 4) > 0)
				return false;
			for (Entity zx : list)
				if ((zx.getProc(P_REVIVE, 5) & 4) > 0)
					return false;
			return true;
		}

		private void damaged(AttackAb atk) {
			tempZK |= (atk.abi & AB_ZKILL) > 0 && canZK();
		}

		private void doRevive(int c) {
			if (c == 1)
				e.status[P_REVIVE][0]--;
			else if (c == 2)
				extraRev++;
			int deadAnim = minRevTime();
			EffAnim ea = EffAnim.effas[A_ZOMBIE];
			deadAnim += ea.getEAnim(0).len();
			e.status[P_REVIVE][1] = deadAnim;
			e.health = e.maxH * maxRevHealth() / 100;
		}

		private int maxRevHealth() {
			int max = e.getProc(P_REVIVE, 2);
			if (e.getProc(P_REVIVE, 0) == 0)
				max = 0;
			for (Entity zx : list) {
				int val = zx.getProc(P_REVIVE, 2);
				max = Math.max(max, val);
			}
			return max;
		}

		private int minRevTime() {
			int min = e.getProc(P_REVIVE, 1);
			if (e.getProc(P_REVIVE, 0) == 0)
				min = Integer.MAX_VALUE;
			for (Entity zx : list) {
				int val = zx.getProc(P_REVIVE, 1);
				min = Math.min(min, val);
			}
			return min;
		}

		private void postUpdate() {
			if (e.health > 0)
				tempZK = false;
		}

		private boolean prekill() {
			int c = canRevive();
			if (!tempZK && c > 0) {
				int[][] status = e.status;
				doRevive(c);
				// clear state
				e.waitTime = 0;
				e.bdist = 0;
				status[P_BURROW][2] = 0;
				status[P_STOP] = new int[PROC_WIDTH];
				status[P_SLOW] = new int[PROC_WIDTH];
				status[P_WEAK] = new int[PROC_WIDTH];
				status[P_CURSE] = new int[PROC_WIDTH];
				status[P_SEAL] = new int[PROC_WIDTH];
				status[P_STRONG] = new int[PROC_WIDTH];
				status[P_LETHAL] = new int[PROC_WIDTH];
				status[P_POISON] = new int[PROC_WIDTH];
				return true;
			}
			return false;
		}

		private int totExRev() {
			int sum = 0;
			for (Entity zx : list) {
				int val = zx.getProc(P_REVIVE, 0);
				if (val == -1)
					return -1;
				sum += val;
			}
			return sum;
		}

		/** update revive status */
		private void updateRevive() {
			int[][] status = e.status;
			AnimManager anim = e.anim;

			list.removeIf(em -> {
				int conf = em.getProc(P_REVIVE, 5) & 3;
				if (conf == 3)
					return false;
				if (conf == 2 || em.kbTime == -1)
					return em.kbTime == -1;
				return true;
			});
			List<AbEntity> lm = e.basis.inRange(TCH_ZOMBX, -e.dire, 0, e.basis.st.len);
			for (AbEntity m : lm) {
				if (m == e)
					continue;
				Entity em = ((Entity) m);
				double d0 = em.pos + em.getProc(P_REVIVE, 3);
				double d1 = em.pos + em.getProc(P_REVIVE, 4);
				if ((d0 - e.pos) * (d1 - e.pos) > 0)
					continue;
				int conf = em.getProc(P_REVIVE, 5);
				if ((conf & 8) == 0 && (e.type & TB_ZOMBIE) == 0)
					continue;
				conf &= 3;
				if (conf == 0 && (em.touchable() & (TCH_N | TCH_EX)) == 0)
					continue;
				list.add(em);
			}

			if (status[P_REVIVE][1] > 0) {
				e.acted = true;
				int id = e.dire == -1 ? A_U_ZOMBIE : A_ZOMBIE;
				EffAnim ea = EffAnim.effas[id];
				if (anim.corpse == null)
					anim.corpse = ea.getEAnim(1);
				if (status[P_REVIVE][1] == ea.getEAnim(0).len())
					anim.corpse = ea.getEAnim(0);
				status[P_REVIVE][1]--;
				if (anim.corpse != null)
					anim.corpse.update(false);
				if (status[P_REVIVE][1] == 0)
					anim.corpse = null;
			}
		}

	}

	public final AnimManager anim;

	private final AtkManager atkm;

	private final ZombX zx = new ZombX(this);

	/** game engine, contains environment configuration */
	public final StageBasis basis;

	/** entity data, read only */
	public final MaskEntity data;

	/** group, used for search */
	public int group;

	private final KBManager kb = new KBManager(this);

	/** layer of display, constant field */
	public int layer;

	/** proc status, contains ability-specific status data */
	public final int[][] status = new int[PROC_TOT][PROC_WIDTH];

	/** trait of enemy, also target trait of unit, use bitmask */
	public int type;

	/** attack model */
	protected final AtkModelEntity aam;

	/** temp field: damage accumulation */
	private long damage;

	/** const field */
	protected boolean isBase;

	/**
	 * KB FSM time, values: <br>
	 * 0: not KB <br>
	 * -1: dead <br>
	 * positive: KB time count-down <br>
	 * negative: burrow FSM type
	 */
	private int kbTime;

	/** temp field: marker for double income */
	protected boolean tempearn;

	/** wait FSM time */
	private int waitTime;

	/** acted: temp field, for update sync */
	private boolean acted;

	/** barrier value, 0 means no barrier or broken */
	private int barrier;

	/** remaining burrow distance */
	private double bdist;

	/** poison proc processor */
	private final PoisonToken pois = new PoisonToken(this);

	/** abilities that are activated after it's attacked */
	private final List<AttackAb> tokens = new ArrayList<>();

	/**
	 * temp field within an update loop <br>
	 * used for moving determination
	 */
	private boolean touch;

	/** temp field: whether it can attack */
	private boolean touchEnemy;

	/** weak proc processor */
	private final WeakToken weaks = new WeakToken(this);

	private int altAbi = 0;

	protected Entity(StageBasis b, MaskEntity de, EAnimU ea, double d0, double d1) {
		super(d1 < 0 ? b.st.health : (int) (de.getHp() * d1));
		basis = b;
		data = de;
		aam = AtkModelEntity.getIns(this, d0);
		anim = new AnimManager(this, ea);
		atkm = new AtkManager(this);
		barrier = de.getShield();
		status[P_BURROW][0] = getProc(P_BURROW, 0);
		status[P_REVIVE][0] = getProc(P_REVIVE, 0);
	}

	public void altAbi(int alt) {
		altAbi ^= alt;

	}

	/** accept attack */
	@Override
	public void damaged(AttackAb atk) {

		int dmg = getDamage(atk, atk.atk);
		// if immune to wave and the attack is wave, jump out
		if ((atk.waveType & WT_WAVE) > 0) {
			if (getProc(P_IMUWAVE, 0) > 0)
				anim.getEff(P_WAVE);
			if (getProc(P_IMUWAVE, 0) == 100)
				return;
			else
				dmg = dmg * (100 - getProc(P_IMUWAVE, 0)) / 100;
		}

		if ((atk.waveType & WT_MOVE) > 0)
			if ((getAbi() & AB_MOVEI) > 0) {
				anim.getEff(P_WAVE);
				return;
			}

		tokens.add(atk);

		int[] imuatk = data.getProc(P_IMUATK);
		if ((atk.type == -1 || receive(atk.type, -1)) && imuatk[0] > 0) {
			if (status[P_IMUATK][0] == 0 && basis.r.nextDouble() * 100 < imuatk[0]) {
				status[P_IMUATK][0] = (int) (imuatk[1] * (1 + 0.2 / 3 * getFruit(atk.type, -1)));
				anim.getEff(P_IMUATK);
			}
			if (status[P_IMUATK][0] > 0)
				return;
		}

		if (barrier > 0) {
			if (atk.getProc(P_BREAK)[0] > 0) {
				barrier = 0;
				anim.getEff(BREAK_ABI);
			} else if (getDamage(atk, atk.atk) >= barrier) {
				barrier = 0;
				anim.getEff(BREAK_ATK);
				return;
			} else {
				anim.getEff(BREAK_NON);
				return;
			}
		}

		CommonStatic.def.setSE(isBase ? SE_HIT_BASE : (basis.r.irDouble() < 0.5 ? SE_HIT_0 : SE_HIT_1));

		damage += dmg;
		zx.damaged(atk);
		tempearn |= (atk.abi & AB_EARN) > 0;
		if (atk.getProc(P_CRIT)[0] > 0) {
			basis.lea.add(new EAnimCont(pos, layer, EffAnim.effas[A_CRIT].getEAnim(0)));
			CommonStatic.def.setSE(SE_CRIT);
		}
		if (atk.getProc(P_SATK)[0] > 0) {
			basis.lea.add(new EAnimCont(pos, layer, EffAnim.effas[A_SATK].getEAnim(0)));
			CommonStatic.def.setSE(SE_SATK);
		}
		// process proc part
		if (atk.type != -1 && !receive(atk.type, 1))
			return;
		double f = getFruit(atk.type, 1);
		double time = 1 + f * 0.2 / 3;
		double dist = 1 + f * 0.1;
		if (atk.type < 0 || atk.canon != -2)
			dist = time = 1;
		if (atk.getProc(P_STOP)[0] > 0) {
			int val = (int) (atk.getProc(P_STOP)[0] * time);
			int rst = getProc(P_IMUSTOP, 0);
			val = val * (100 - rst) / 100;
			status[P_STOP][0] = Math.max(status[P_STOP][0], val);
			if (rst < 100)
				anim.getEff(P_STOP);
			else
				anim.getEff(INV);
		}
		if (atk.getProc(P_SLOW)[0] > 0) {
			int val = (int) (atk.getProc(P_SLOW)[0] * time);
			int rst = getProc(P_IMUSLOW, 0);
			val = val * (100 - rst) / 100;
			status[P_SLOW][0] = Math.max(status[P_SLOW][0], val);
			if (rst < 100)
				anim.getEff(P_SLOW);
			else
				anim.getEff(INV);
		}
		if (atk.getProc(P_WEAK)[0] > 0) {
			int val = (int) (atk.getProc(P_WEAK)[0] * time);
			int rst = getProc(P_IMUWEAK, 0);
			val = val * (100 - rst) / 100;
			if (rst < 100) {
				weaks.add(new int[] { val, atk.getProc(P_WEAK)[1] });
				anim.getEff(P_WEAK);
			} else
				anim.getEff(INV);
		}
		if (atk.getProc(P_CURSE)[0] > 0) {
			int val = atk.getProc(P_CURSE)[0];
			int rst = getProc(P_IMUCURSE, 0);
			val = val * (100 - rst) / 100;
			status[P_CURSE][0] = Math.max(status[P_CURSE][0], val);
			if (rst < 100)
				anim.getEff(P_CURSE);
			else
				anim.getEff(INV);
		}

		if (atk.getProc(P_KB)[0] != 0) {
			int rst = getProc(P_IMUKB, 0);
			if (rst < 100) {
				status[P_KB][0] = atk.getProc(P_KB)[1];
				interrupt(P_KB, atk.getProc(P_KB)[0] * dist * (100 - rst) / 100);
			} else
				anim.getEff(INV);
		}
		if (atk.getProc(P_SNIPER)[0] > 0)
			interrupt(INT_ASS, KB_DIS[INT_ASS]);

		if (atk.getProc(P_BOSS)[0] > 0)
			interrupt(INT_SW, KB_DIS[INT_SW]);

		if (atk.getProc(P_WARP)[0] > 0)
			if (getProc(P_IMUWARP, 0) < 100) {
				interrupt(INT_WARP, atk.getProc(P_WARP)[1]);
				EffAnim e = EffAnim.effas[A_W];
				int len = e.len(0) + e.len(1);
				int val = atk.getProc(P_WARP)[0];
				int rst = getProc(P_IMUWARP, 0);
				val = val * (100 - rst) / 100;
				status[P_WARP][0] = val + len;
			} else
				anim.getEff(INVWARP);

		if (atk.getProc(P_SEAL)[0] > 0)
			if ((getAbi() & AB_SEALI) == 0) {
				status[P_SEAL][0] = atk.getProc(P_SEAL)[0];
				anim.getEff(P_SEAL);
			} else
				anim.getEff(INV);

		if (atk.getProc(P_POISON)[0] > 0)
			if ((getAbi() & AB_POII) == 0 || atk.getProc(P_POISON)[1] < 0) {
				int[] ws = new int[5];
				ws[0] = atk.getProc(P_POISON)[0];
				ws[1] = atk.getProc(P_POISON)[1];
				ws[2] = ws[3] = atk.getProc(P_POISON)[2];
				ws[4] = atk.getProc(P_POISON)[3];
				if (ws[4] % 4 == 1)
					ws[1] = getDamage(atk, ws[1]);
				pois.add(ws);
				anim.getEff(P_POISON);
			} else
				anim.getEff(INV);
	}

	/** get the current ability bitmask */
	@Override
	public int getAbi() {
		if (status[P_SEAL][0] > 0)
			return (data.getAbi() ^ altAbi) & (AB_ONLY | AB_METALIC | AB_GLASS);
		return data.getAbi() ^ altAbi;
	}

	/** get the currently attack, only used in display */
	public int getAtk() {
		return aam.getAtk();
	}

	/** get the current proc array */
	public int getProc(int ind, int ety) {
		if (status[P_SEAL][0] > 0) {
			if (ind != P_BURROW && ind != P_REVIVE)
				return 0;
			if (ind == P_REVIVE && ety > 2)
				return 0;
		}
		return data.getProc(ind)[ety];
	}

	/** receive an interrupt */
	public void interrupt(int t, double d) {
		kb.interrupt(t, d);
	}

	@Override
	public boolean isBase() {
		return isBase;
	}

	/** mark it dead, proceed death animation */
	public void kill() {
		if (kbTime == -1)
			return;
		kbTime = -1;
		anim.kill();

	}

	/** update the entity after receiving attacks */
	@Override
	public void postUpdate() {
		int hb = data.getHb();
		long ext = health * hb % maxH;
		if (ext == 0)
			ext = maxH;
		if (!isBase && damage > 0 && kbTime <= 0 && kbTime != -1 && (ext <= damage * hb || health < damage))
			interrupt(INT_HB, KB_DIS[INT_HB]);
		health -= damage;
		if (health > maxH)
			health = maxH;
		damage = 0;

		// increase damage
		int strong = getProc(P_STRONG, 0);
		if (status[P_STRONG][0] == 0 && strong > 0 && health * 100 <= maxH * strong) {
			status[P_STRONG][0] = getProc(P_STRONG, 1);
			anim.getEff(P_STRONG);
		}
		// lethal strike
		if (getProc(P_LETHAL, 0) > 0 && health <= 0) {
			boolean b = basis.r.nextDouble() * 100 < getProc(P_LETHAL, 0);
			if (status[P_LETHAL][0] == 0 && b) {
				health = 1;
				anim.getEff(P_LETHAL);
			}
			status[P_LETHAL][0]++;
		}

		for (AttackAb atk : tokens)
			atk.model.invokeLater(atk, this);
		tokens.clear();

		kb.doInterrupt();

		if ((getAbi() & AB_GLASS) > 0 && atkm.atkTime == 0 && kbTime == 0 && atkm.loop == 0)
			kill();

		// update animations
		anim.update();
		zx.postUpdate();

		if (health > 0)
			tempearn = false;
		acted = false;
	}

	public void setSummon(int conf) {
		// conf 1
		if (conf == 1) {
			kb.kbType = INT_WARP;
			kbTime = EffAnim.effas[A_W].len(1);
			status[P_WARP][2] = 1;
		}
		// conf 2
		if (conf == 2 && data.getPack().anim.anims.length >= 7)
			kbTime = -3;
		if (conf == 3 && data.getPack().anim.anims.length >= 7) {
			kbTime = -3;
			status[P_BURROW] = new int[PROC_WIDTH];
		}
	}

	/** can be targeted by the cat with Targer ability of trait t */
	@Override
	public boolean targetable(int t) {
		return (type & t) > 0 || isBase;
	}

	/** get touch mode bitmask */
	@Override
	public int touchable() {
		int n = (getAbi() & AB_GHOST) > 0 ? TCH_EX : TCH_N;
		int ex = (getProc(P_REVIVE, 5) & 16) > 0 ? TCH_ZOMBX : 0;
		if (kbTime == -1)
			return TCH_SOUL | ex;
		if (status[P_REVIVE][1] > 0)
			return TCH_CORPSE | ex;
		if (status[P_BURROW][2] > 0)
			return n | TCH_UG | ex;
		if (kbTime < -1)
			return TCH_UG | ex;
		return (kbTime == 0 ? n : TCH_KB) | ex;
	}

	/**
	 * update the entity. order of update: <br>
	 * KB -> revive -> move -> burrow -> attack -> wait
	 */
	@Override
	public void update() {
		// if this entity is in kb state, do kbmove()
		// eneity can move right after kbmove, no need to mark acted
		if (kbTime > 0)
			kb.updateKB();

		// update revive status, mark acted
		zx.updateRevive();

		// do move check if available, move if possible
		if (kbTime == 0 && !acted && atkm.atkTime == 0)
			updateTouch();

		boolean nstop = status[P_STOP][0] == 0;

		// update burrow state if not stopped
		if (nstop)
			updateBurrow();

		// update wait and attack state
		if (kbTime == 0) {
			boolean binatk = kbTime >= 0 && waitTime + kbTime + atkm.atkTime == 0;
			binatk = binatk && touchEnemy && atkm.loop != 0 && nstop;

			// if it can attack, setup attack state
			if (!acted && binatk)
				atkm.setUp();

			// update waiting state
			if ((waitTime > 0 || !touchEnemy) && touch && atkm.atkTime == 0)
				anim.setAnim(1);
			if (waitTime > 0)
				waitTime--;

			// update attack status when in attack state
			if (status[P_STOP][0] == 0 && atkm.atkTime > 0)
				atkm.updateAttack();
		}

		// update proc effects
		updateProc();
	}

	protected int critCalc(boolean isMetal, int ans, AttackAb atk) {
		int satk = atk.getProc(P_SATK)[0];
		if (satk > 0)
			ans *= (100 + satk) * 0.01;
		int crit = atk.getProc(P_CRIT)[0];
		if (getProc(P_CRITI, 0) == 1)
			crit = 0;
		if (isMetal)
			if (crit > 0)
				ans *= 0.01 * crit;
			else if (crit < 0)
				ans = (int) Math.ceil(health * crit / -100.0);
			else
				ans = ans > 0 ? 1 : 0;
		else if (crit > 0)
			ans *= 0.01 * crit;
		else if (crit < 0)
			ans = (int) Math.ceil(maxH * 0.0001);
		return ans;
	}

	/** determine the amount of damage received from this attack */
	protected abstract int getDamage(AttackAb atk, int ans);

	/** get max distance to go back */
	protected abstract double getLim();

	protected abstract int traitType();

	/**
	 * move forward <br>
	 * maxl: max distance to move <br>
	 * extmov: distance try to add to this movement return false when movement reach
	 * endpoint
	 */
	protected boolean updateMove(double maxl, double extmov) {
		acted = true;
		double max = (basis.getBase(dire).pos - pos) * dire - data.touchBase();
		if (maxl >= 0)
			max = Math.min(max, maxl);

		double mov = isBase ? 0 : status[P_SLOW][0] > 0 ? 0.5 : data.getSpeed() * 0.5;
		mov += extmov;
		pos += Math.min(mov, max) * dire;
		return max > mov;
	}

	/** interrupt whatever this entity is doing */
	private void clearState() {
		atkm.stopAtk();
		if (kbTime < -1 || status[P_BURROW][2] > 0) {
			status[P_BURROW][2] = 0;
			bdist = 0;
			kbTime = 0;
		}
		if (status[P_REVIVE][1] > 0) {
			status[P_REVIVE][1] = 0;
			anim.corpse = null;
		}
	}

	private void drawAxis(FakeGraphics gra, P p, double siz) {
		// after this is the drawing of hit boxes
		siz *= 1.25;
		double rat = BattleConst.ratio;
		double poa = p.x - pos * rat * siz;
		int py = (int) p.y;
		int h = (int) (640 * rat * siz);
		gra.setColor(FakeGraphics.RED);
		for (int i = 0; i < data.getAtkCount(); i++) {
			double[] ds = aam.inRange(i);
			double d0 = Math.min(ds[0], ds[1]);
			double ra = Math.abs(ds[0] - ds[1]);
			int x = (int) (d0 * rat * siz + poa);
			int y = (int) (p.y + 100 * i * rat * siz);
			int w = (int) (ra * rat * siz);
			if (atkm.tempAtk == i)
				gra.fillRect(x, y, w, h);
			else
				gra.drawRect(x, y, w, h);
		}
		gra.setColor(FakeGraphics.YELLOW);
		int x = (int) ((pos + data.getRange() * dire) * rat * siz + poa);
		gra.drawLine(x, py, x, py + h);
		gra.setColor(FakeGraphics.BLUE);
		int bx = (int) ((dire == -1 ? pos : pos - data.getWidth()) * rat * siz + poa);
		int bw = (int) (data.getWidth() * rat * siz);
		gra.drawRect(bx, (int) p.y, bw, h);
		gra.setColor(FakeGraphics.CYAN);
		gra.drawLine((int) (pos * rat * siz + poa), py, (int) (pos * rat * siz + poa), py + h);
		atkm.tempAtk = -1;
	}

	/** get the extra proc time due to fruits, for EEnemy only */
	private double getFruit(int atktype, int dire) {
		if (traitType() != dire)
			return 0;
		return basis.b.t().getFruit(atktype & type);
	}

	/** called when last KB reached */
	private void preKill() {
		if (zx.prekill())
			return;
		kill();
	}

	/** can be effected by the ability targeting trait t */
	private boolean receive(int t, int dire) {
		if (traitType() != dire)
			return true;
		return (type & t) > 0;
	}

	/** update burrow state */
	private void updateBurrow() {
		if (!acted && kbTime == 0 && touch && status[P_BURROW][0] != 0) {
			double bpos = basis.getBase(dire).pos;
			boolean ntbs = (bpos - pos) * dire > data.touchBase();
			if (ntbs) {
				// setup burrow state
				status[P_BURROW][0]--;
				status[P_BURROW][2] = anim.setAnim(4);
				kbTime = -2;
			}
		}
		if (!acted && kbTime == -2) {
			acted = true;
			// burrow down
			status[P_BURROW][2]--;
			if (status[P_BURROW][2] == 0) {
				kbTime = -3;
				anim.setAnim(5);
				bdist = data.getRepAtk().getProc(P_BURROW)[1];
			}
		}
		if (!acted && kbTime == -3) {
			// move underground
			double oripos = pos;
			boolean b = updateMove(bdist, 0);
			bdist -= (pos - oripos) * dire;
			if (!b) {
				bdist = 0;
				kbTime = -4;
				status[P_BURROW][2] = anim.setAnim(6);
			}
		}
		if (!acted && kbTime == -4) {
			// burrow up
			acted = true;
			status[P_BURROW][2]--;
			if (status[P_BURROW][2] == 0)
				kbTime = 0;
		}

	}

	/** update proc status */
	private void updateProc() {
		if (status[P_STOP][0] > 0)
			status[P_STOP][0]--;
		if (status[P_SLOW][0] > 0)
			status[P_SLOW][0]--;
		if (status[P_CURSE][0] > 0)
			status[P_CURSE][0]--;
		if (status[P_SEAL][0] > 0)
			status[P_SEAL][0]--;
		if (status[P_IMUATK][0] > 0)
			status[P_IMUATK][0]--;
		// update tokens
		weaks.update();
		pois.update();
	}

	/** update touch state, move if possible */
	private void updateTouch() {
		touch = true;
		double[] ds = aam.touchRange();
		List<AbEntity> le = basis.inRange(data.getTouch(), dire, ds[0], ds[1]);
		boolean blds = false;
		if (data.isLD()) {
			double bpos = basis.getBase(dire).pos;
			blds = (bpos - pos) * dire > data.touchBase();
			if (blds)
				le.remove(basis.getBase(dire));
			blds &= le.size() == 0;
		} else
			blds = le.size() == 0;
		if (status[P_STOP][0] == 0 && blds) {
			touch = false;
			anim.setAnim(0);
			updateMove(-1, 0);
		}
		touchEnemy = touch;
		if ((getAbi() & AB_ONLY) > 0) {
			touchEnemy = false;
			for (AbEntity e : le)
				if (e.targetable(type))
					touchEnemy = true;
		}
	}

}
