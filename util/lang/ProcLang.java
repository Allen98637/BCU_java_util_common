package common.util.lang;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonEncoder;
import common.io.json.JsonField;
import common.io.json.JsonClass.RType;
import common.io.json.JsonClass.WType;
import common.io.json.JsonField.IOType;
import common.pack.UserProfile;
import common.pack.Context.ErrType;
import common.util.Data;
import common.util.Data.Proc;
import common.util.Data.Proc.IntType;
import common.util.lang.LocaleCenter.Binder;
import common.util.lang.LocaleCenter.ObjBinder;
import common.util.lang.LocaleCenter.DisplayItem;

@JsonClass(read = RType.MANUAL, write = WType.CLASS, generator = "gen", serializer = "ser")
public class ProcLang {

	@JsonClass(read = RType.FILL)
	public static class ItemLang {
		@JsonField
		public String abbr_name, full_name, tooltip, format;

		private final String name;
		private final Class<?> cls;
		private final LinkedHashMap<String, DisplayItem> map = new LinkedHashMap<>();

		private ItemLang(String name, Class<?> cls) {
			this.cls = cls;
			this.name = name;
		}

		public Binder get(String proc) {
			try {
				return new ObjBinder(map.get(proc), name + "." + proc, this::getBinder);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public String[] list() {
			String[] ans = new String[map.size()];
			int i = 0;
			for (Entry<String, DisplayItem> ent : map.entrySet())
				ans[i++] = ent.getKey();
			return ans;
		}

		@JsonField(tag = "class", io = IOType.R)
		public void readClass(JsonElement elem) {
			fill("", cls, elem == null ? null : elem.getAsJsonObject());
		}

		@JsonField(tag = "class", io = IOType.W)
		public JsonObject writeClass() {
			JsonObject ans = new JsonObject();
			fill(ans, "", cls);
			return ans;
		}

		private void fill(JsonObject ans, String pre, Class<?> c) {
			for (Field f : c.getDeclaredFields()) {
				if (IntType.class.isAssignableFrom(f.getType()))
					fill(ans, f.getName() + ".", f.getType());
				else
					ans.add(pre + f.getName(), JsonEncoder.encode(map.get(pre + f.getName())));
			}
		}

		private void fill(String pre, Class<?> c, JsonObject obj) {
			for (Field f : c.getDeclaredFields()) {
				JsonElement elem = obj == null ? null : obj.get(f.getName());
				if (IntType.class.isAssignableFrom(f.getType())) {
					JsonObject data = elem == null ? null : elem.getAsJsonObject();
					fill(f.getName() + ".", f.getType(), data);
				} else {
					DisplayItem pf = elem == null ? new DisplayItem() : JsonDecoder.decode(elem, DisplayItem.class);
					map.put(pre + f.getName(), pf);
				}
			}
		}

		private Binder getBinder(String proc) {
			return ProcLang.get().get(name).get(proc);
		}

	}

	public static class ProcLangStore {

		private final ProcLang[] langs = new ProcLang[CommonStatic.Lang.LOC_CODE.length];

		private ProcLang getLang() {
			int lang = CommonStatic.getConfig().lang;
			if (langs[lang] == null)
				Data.err(ProcLang::read);
			return langs[lang];
		}

		private void setLang(ProcLang lang) {
			langs[CommonStatic.getConfig().lang] = lang;
		}

	}

	public static ProcLang gen(JsonElement elem) throws Exception {
		ProcLang ans = new ProcLang();
		JsonObject obj = elem == null ? null : elem.getAsJsonObject();
		for (Field f : Proc.class.getDeclaredFields()) {
			String name = f.getName();
			ItemLang item = new ItemLang(name, f.getType());
			if (obj != null && obj.has(name))
				JsonDecoder.inject(obj.get(name), ItemLang.class, item);
			else
				item.readClass(null);
			ans.map.put(name, item);
		}
		return ans;
	};

	public static ProcLang get() {
		return store().getLang();
	}

	private static void read() throws Exception {
		File f = CommonStatic.ctx.getLangFile("proc.json");
		if(!f.exists())
			CommonStatic.ctx.printErr(ErrType.FATAL, "cannot find proc.json");
		JsonElement elem = JsonParser.parseReader(new FileReader(f));
		ProcLang proc = JsonDecoder.decode(elem, ProcLang.class);
		store().setLang(proc);
	}

	public static void clear() {
		UserProfile.setStatic("ProcLangStore", null);
	}

	private static ProcLangStore store() {
		return UserProfile.getStatic("ProcLangStore", ProcLangStore::new);
	}

	private final Map<String, ItemLang> map = new LinkedHashMap<>();

	private ProcLang() {
	}

	public ItemLang get(int i) {
		return get(Proc.class.getDeclaredFields()[i].getName());
	}

	public ItemLang get(String str) {
		return map.get(str);
	}

	public JsonObject ser() {
		JsonObject obj = new JsonObject();
		for (Field f : Proc.class.getDeclaredFields()) {
			String name = f.getName();
			obj.add(name, JsonEncoder.encode(map.get(name)));
		}
		return obj;
	}

}
