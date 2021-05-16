package tag;

import java.util.*;

public class Tag {
    private String tagName;
    private String tagValue;
    private ArrayList<Tag> children = new ArrayList<>();
    private HashMap<String, String> attributes;
    private Map<String, String> copyAttributes;
    private Map<String, String> copyOtherAttributes;
    private int level = 0;
    private boolean isSkipTagEnabled = false;

    public Tag() {
        super();
    }

    public Tag(Tag parentTag, String tagName, String tagValue) {
        this.tagName = obtainFirstTagName(tagName);
        this.attributes = obtainFirstAttributes(tagName);
        this.tagValue = tagValue;
        if (parentTag != null) {
            parentTag.add(this);
            this.level = parentTag.level + 1;
        }
        //this.isSkipEnabled = isSkipEnabled();
    }

    /**
     * Getter dla atrybutów
     * @return
     */
    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Setter dla atrybutów
     * @param keyAttr
     * @param valueAttr
     * @return
     */
    public void setAttributes(String keyAttr, String valueAttr) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(keyAttr, valueAttr);

    }

    /**
     * @param path
     * @return
     */
    private static String obtainFirstFullTag(String path) {
        String[] lPath = path.split("/");
        if (lPath.length == 0)
            return null;
        return lPath[0];
    }

    /**
     * @param path
     * @return
     */
    private static String obtainFirstTagName(String path) {
        String fullTag = obtainFirstFullTag(path);
        return fullTag.split(":")[0];
    }

    /**
     * @param path
     * @return
     */
    private static HashMap<String, String> obtainFirstAttributes(String path) {

        String fullTag = obtainFirstFullTag(path);
        String tName = obtainFirstTagName(path);

        if (fullTag.equals(tName))
            return null;

        HashMap<String, String> result = new HashMap<>();

        String lPath = fullTag.substring(tName.length() + 1);
        String[] attr = lPath.split(":");
        for (int i = 0; i < attr.length; i++) {
            String[] kv = attr[i].split("=");
            result.put(kv[0], kv[1]);
        }

        return result;
    }

    /**
     * @param path
     * @param value
     */
    public void addTagByPath(String path, String value) {
        Tag lTag = null;
        String newFullTag = obtainFirstFullTag(path);
        lTag = findTag(newFullTag, false);
        if (lTag == null)
            lTag = new Tag(this, newFullTag, "");
        if (path.contains("/")) {
            String newPath = path.substring(newFullTag.length() + 1);
            lTag.addTagByPath(newPath, value);
        } else {
            lTag.setTagValue(value);
        }
    }

    /**
     * @param aTagName
     * @param recur
     * @return
     */
    public Tag findTag(String aTagName, boolean recur) {

        String lTagName = obtainFirstTagName(aTagName);
        HashMap<String, String> attr = obtainFirstAttributes(aTagName);

        Tag result = null;
        Iterator<Tag> it = children.iterator();
        while (it.hasNext()) {
            Tag element = it.next();
            if (element.getTagName().equals(lTagName)) {
                if (attr == null || element.attributes == null || attr.equals(element.attributes)) {
                    result = element;
                    if (element.attributes == null)
                        element.attributes = attr;
                    break;
                }
            }
            if (recur)
                result = element.findTag(lTagName, recur);
            if (result != null)
                return result;
        }
        return result;
    }

    /**
     * @param tag
     */
    public void add(Tag tag) {
        children.add(tag);
    }

    public String getTagName() {
        return tagName;
    }

    public String getTagValue() {
        return tagValue;
    }

    public ArrayList<Tag> getChildren() {
        return children;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + level;
        result = prime * result + ((tagName == null) ? 0 : tagName.hashCode());
        result = prime * result + ((tagValue == null) ? 0 : tagValue.hashCode());
        return result;
    }

    /**
     *Rozbudowana metoda equals porównująca zawartość plików wraz z uwzględnieniem włączenia trybu do pomijania węzłów oraz pomijania atrubutów
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tag other = (Tag) obj;
        if (isSkipTagEnabled)
            return true;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!this.attributesEquals(other)){
            System.out.println("EQUALS" + !this.attributesEquals(other));
            return false;
        }
        if (children == null || children.isEmpty()) {
            if (other.children != null && !other.children.isEmpty())
                return false;
            if (!tagValue.equals(other.tagValue))
                return false;
        } else if (!children.equals(other.children))
            return false;
        if (level != other.level)
            return false;
        if (tagName == null) {
            if (other.tagName != null)
                return false;
        } else if (!tagName.equals(other.tagName))
            return false;
        return true;
    }


    String key;
    String value;
    public final String getKey()        { return key; }
    public final String getValue()      { return value; }
    //public final String toString() { return key + "=" + value; }

    /**
     * Metoda porównuje atrybuty w dokumentach xml przy włączonym trybie ich pomijania
     */
    //private boolean attributesEquals(Tag other) {

    public boolean attributesEquals(Tag other) {


        String skipAttribute = "_KSSEWConv_skip_fields";
        List<String> skipList = new ArrayList<>();
        String kssewConv_skip_fields = attributes.get(skipAttribute);
        if (kssewConv_skip_fields != null) {
            String[] split = kssewConv_skip_fields.split(",");
            for (int i = 0; i < split.length; i++) {
                String splittedAttr = split[i].trim();
                System.out.println("skipAttribute: " + skipAttribute);
                skipList.add(splittedAttr);
            }
        }
        Map.Entry<String, String> next = null;
        Set<Map.Entry<String, String>> map = attributes.entrySet();
        Iterator<Map.Entry<String, String>> iterator = map.iterator();
        while (iterator.hasNext()) {
            next = iterator.next();
            System.out.println("next: " + next);
            if (iterator.next().getKey().equals(skipAttribute)) {
                System.out.println("iterator.next().getKey(): " + iterator.next().getKey());
                return true;
            }
//			for (int i = 0; i < skipList.size(); i++) {
//				String skipAttr = skipList.get(i);
//				System.out.println("skipAttr: " + skipAttr);
//				if (next.getKey().equals(skipAttr)) {
//					System.out.println(next.getKey().equals(skipAttr));
//					return true;
//				}
//			}
//		}
        }
//		if (Objects.equals(key, next.getKey()) && Objects.equals(value, next.getValue())) {
//			return true;
//		}
        return Objects.equals(key, next.getKey()) && Objects.equals(value, next.getValue());
    }


//		copyAttributes = new HashMap<>();
//		copyAttributes.putAll(attributes);
//		copyOtherAttributes = new HashMap<>();
//		copyOtherAttributes.putAll(other.attributes);
//
//		String skipAttribute = "_KSSEWConv_skip_fields";
//		String kssewConv_skip_fields = copyAttributes.get(skipAttribute);
//		List<String> skipList = new ArrayList<>();
//		Iterator<Entry<String, String>> attrIterator = copyAttributes.entrySet().iterator();
//		if (kssewConv_skip_fields != null) {
//			String[] split = kssewConv_skip_fields.split(",");
//			for (int i = 0; i < split.length; i++) {
//				String splittedAttr = split[i].trim();
//				skipList.add(splittedAttr);
//			}
//			while (attrIterator.hasNext()) {
//				Entry<String, String> attrMap = attrIterator.next();
//				String attrKey = attrMap.getKey();
//				if (attrKey.equals(skipAttribute)) {
//					attrIterator.remove();
//				}
//			}
//			removeSkipAttributes(skipList);
//		}
//		return copyAttributes.equals(copyOtherAttributes);
//	}

    /**
     * Metoda usuwa z porównywania atrybuty, które są przekazane do skipowania
     */
    private void removeSkipAttributes(List<String> skipList) {

        Iterator<String> iteratorAttrSet = copyAttributes.keySet().iterator();
        Iterator<String> otherIteratorAttrSet = copyOtherAttributes.keySet().iterator();
        while (iteratorAttrSet.hasNext() && otherIteratorAttrSet.hasNext()) {
            String next = iteratorAttrSet.next();
            String otherNext = otherIteratorAttrSet.next();
            for (int i = 0; i < skipList.size(); i++) {
                String skipAttr = skipList.get(i);
                if (next.equals(skipAttr)) {
                    iteratorAttrSet.remove();
                }
                if (otherNext.equals(skipAttr)) {
                    otherIteratorAttrSet.remove();
                }
            }
        }
    }

    /**
     * Włącza tryb pomijania węzłów podczas porównywania zawartości dokumentów
     */
    public void setSkipTagEnabled() {
        this.isSkipTagEnabled = isSkipTagEnabled();
    }

    /**
     * Zwraca informację czy jest ustawiony atrybut dla danego węzła
     */
    public boolean isSkipTagEnabled() {
        if (attributes == null) {
            return false;
        }
        Iterator<Map.Entry<String, String>> iterator = attributes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> map = iterator.next();
            if (map.getKey().equals("_KSSEWConv_skip") && map.getValue().equals("true"))
                return true;
        }
        return false;
    }

    public static final String SKIP_FIELDS = "_KSSEWConv_skip_fields";
    public static boolean compareMaps1(Map<String, String> first, Map<String, String> second){
        String[] splitString = first.get(SKIP_FIELDS)
                .trim()
                .split(",");
        List<String> fieldsSkipped = Arrays.asList(splitString);
        fieldsSkipped.stream().map(String::trim);
        System.out.println("Fields skipped" + fieldsSkipped);
        return second.keySet().stream()
                .filter(key -> !fieldsSkipped.contains(key.trim()))
                .allMatch(key -> first.get(key).equals(second.get(key)));

    }
    public static void main(String[] args) {
        String trim = "                   ELA   ";
        trim = trim.trim();
        System.out.println("'" + trim + "'");

        Map<String, String> firstMap = getFirstMap();
        Map<String, String> secondMap = getSecondMap();
        System.out.println("Maps are equal: " + compareMaps1(firstMap, secondMap));
    }
    private static Map<String, String> getFirstMap() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("_KSSEWConv_skip_fields", "data_wystawienia,             kod_realizacji");
        attributes.put("data_wystawienia", "5021-04-27 12:38:12");
        attributes.put("id_dokumentu", "137921/BRGA/21/1");
        attributes.put("id_podmiotu_odbiorca_102", "83082");
        attributes.put("info_dodatkowe", "");
        attributes.put("kod_realizacji", "5");
        return attributes;
    }
    private static Map<String, String> getSecondMap() {
        Map<String, String> otherAttributes = new HashMap<>();
        otherAttributes.put("data_wystawienia", "5021-04-27 12:38:12");
        otherAttributes.put("id_dokumentu", "137921/BRGA/21/1");
        otherAttributes.put("id_podmiotu_odbiorca_102", "83082");
        otherAttributes.put("info_dodatkowe", "");
        otherAttributes.put("kod_realizacji", "");
        return otherAttributes;
    }

    public static boolean compareMaps(Map<String, String> first, Map<String, String> second){
        String[] splitString = first.get(SKIP_FIELDS)
                .split(",");
        List<String> fieldsSkipped = Arrays.asList(splitString);
        System.out.println("Fields skipped" + fieldsSkipped);
        Set<String> secondMapKeys = second.keySet();
        for (String secondMapKey : secondMapKeys) {
            if (fieldsSkipped.contains(secondMapKey)) {
                continue;
            }
            if (!first.get(secondMapKey).equals(second.get(secondMapKey))) {
                return false;
            }
        }
        return true;
    }

}
