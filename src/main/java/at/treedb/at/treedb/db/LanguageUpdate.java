/*
* (C) Copyright 2014-2016 Peter Sauer (http://treedb.at/).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package at.treedb.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import at.treedb.i18n.Locale.LANGUAGE;
import at.treedb.util.text.Text;
import at.treedb.util.text.TextGroup;

/**
 * <p>
 * Helper class for add i18n strings and text.
 * </p>
 * 
 * @author Peter Sauer
 *
 */
public class LanguageUpdate {
    private static String error = "LanguageUpdate.addText(): wrong sequence";

    static private class LText {
        private Enum<?> field;
        private String text;

        public LText(Enum<?> field, String text) {
            this.field = field;
            this.text = text;
        }

        public Enum<?> getField() {
            return field;
        }

        public String getText() {
            return text;
        }
    }

    private HashMap<LANGUAGE, ArrayList<LText>> map;
    private Class<? extends Enum<?>> clazz;
    static private Text text;
    static private LANGUAGE[] allLanguages;
    static private LANGUAGE[] languages;
    static private LANGUAGE primaryLang;

    @SuppressWarnings("unchecked")
    public static LanguageUpdate createUpdate(Class<? extends Enum<?>> clazz, Object... args) throws Exception {
        LanguageUpdate lu = new LanguageUpdate(clazz);
        LANGUAGE lang = null;
        for (int i = 0; i < args.length; ++i) {
            Object o = args[i];
            if (o instanceof LANGUAGE) {
                lang = (LANGUAGE) o;
                continue;
            }
            if (o instanceof String) {
                if (lang == null) {
                    throw new Exception("LanguageUpdate.addText(): Missing language");
                }

                @SuppressWarnings("rawtypes")
                Enum<?> e = Enum.valueOf((Class<? extends Enum>) clazz, (String) o);
                ++i;
                o = args[i];
                if (!(o instanceof String)) {
                    throw new Exception(error);
                }
                String text = (String) o;
                ArrayList<LText> l = lu.map.get(lang);
                if (l == null) {
                    l = new ArrayList<LText>();
                    lu.map.put(lang, l);
                }
                l.add(new LText(e, text));
            } else {
                throw new Exception(error);
            }

        }
        return lu;
    }

    public static LanguageUpdate createUpdate(boolean allLang, Class<? extends Enum<?>> clazz, String group,
            String... args) throws Exception {
        LanguageUpdate lu = new LanguageUpdate(clazz);
        addText(allLang, lu, group, args);
        return lu;
    }

    public static void addText(boolean allLang, LanguageUpdate lu, String group, String... args) throws Exception {
        LANGUAGE[] llist = allLang ? allLanguages : languages;
        for (LANGUAGE l : llist) {
            TextGroup g = text.getGroup(group, l, null);
            for (String s : args) {
                Enum<?> e = Enum.valueOf((Class<? extends Enum>) lu.clazz, (String) s);
                ArrayList<LText> list = lu.map.get(l);
                if (list == null) {
                    list = new ArrayList<LText>();
                    lu.map.put(l, list);
                }
                list.add(new LText(e, g.get(s)));
            }
        }
    }

    public UpdateMap getUpdateMap() throws Exception {
        UpdateMap umap = new UpdateMap(clazz);
        for (LANGUAGE lang : map.keySet()) {
            ArrayList<LText> list = map.get(lang);
            for (LText lt : list) {
                umap.addIstring(lt.getField(), lt.getText(), lang, null);
            }
        }
        return umap;
    }

    public LanguageUpdate(Class<? extends Enum<?>> clazz) {
        this.clazz = clazz;
        map = new HashMap<LANGUAGE, ArrayList<LText>>();
    }

    public static void setText(Text text, LANGUAGE primaryLang, LANGUAGE[] languages) {
        LanguageUpdate.text = text;
        LanguageUpdate.languages = languages;
        LanguageUpdate.primaryLang = primaryLang;
        ArrayList<LANGUAGE> list = new ArrayList<LANGUAGE>(Arrays.asList(languages));
        list.add(primaryLang);
        allLanguages = list.toArray(new LANGUAGE[list.size()]);
    }

}
