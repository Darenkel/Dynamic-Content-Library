package dynamic.content.library.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dynamic.content.library.json.JsonValue;

/**
 * Root object of an item database JSON file registered via {@code DynamicContentLibrary}.
 * A minimal file looks like {@code {"items": [ { "id": 1, "name": "..." , ... } ]}}.
 */
public class ItemDatabaseFile {
    public List<ItemDefinition> items;

    /** Builds an {@link ItemDatabaseFile} from a parsed JSON object, reading its {@code items} array. */
    public static ItemDatabaseFile fromJson(Map<String, Object> json) {
        ItemDatabaseFile file = new ItemDatabaseFile();
        List<Object> itemsArray = JsonValue.getArray(json, "items");
        if (itemsArray != null) {
            List<ItemDefinition> items = new ArrayList<>(itemsArray.size());
            for (Object element : itemsArray) {
                items.add(ItemDefinition.fromJson(JsonValue.asObject(element)));
            }
            file.items = items;
        }
        return file;
    }
}
