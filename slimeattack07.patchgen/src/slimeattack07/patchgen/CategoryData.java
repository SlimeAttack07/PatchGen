package slimeattack07.patchgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Utility class for storing and handling data for patch note categories.
 * 
 */
public class CategoryData {
	private JsonArray data;
	
	public static final String CAT_PRIO = "priority";
	
	/** Constructor.
	 * 
	 * @param data Initial data.
	 */
	public CategoryData(JsonArray data) {
		this.data = data;
	}
	
	/** Get the data stored in this instance.
	 * 
	 * @return The stored data.
	 */
	public JsonArray getData() {
		return data;
	}
	
	/** Check if the data contains an entry with the specified id.
	 * 
	 * @param id The id to check for.
	 * @return True if id is contained in data, false otherwise.
	 */
	public boolean contains(String id) {
		for(JsonElement element : data) {
			if(element.isJsonObject()) {
				JsonObject entry = element.getAsJsonObject();
				
				if(entry.has(PatchNoteData.ID) && entry.get(PatchNoteData.ID).getAsString().equals(id))
					return true;
			}
		}
		
		return false;
	}
	
	/** Remove an entry with the specified id.
	 * 
	 * @param id The id to check for.
	 * @return True if id was removed from data, false otherwise.
	 */
	public boolean remove(String id) {
		for(JsonElement element : data) {
			if(element.isJsonObject()) {
				JsonObject entry = element.getAsJsonObject();
				
				if(entry.has(PatchNoteData.ID) && entry.get(PatchNoteData.ID).getAsString().equals(id)) {
					return data.remove(element);
				}
			}
		}
		
		return false;
	}
	
	/** Get the priority of an entry with the specified id.
	 * 
	 * @param id The id to check for.
	 * @return The priority if id is contained in data, -1 otherwise.
	 */
	public int getPriority(String id) {
		for(JsonElement element : data) {
			if(element.isJsonObject()) {
				JsonObject entry = element.getAsJsonObject();
				
				if(entry.has(PatchNoteData.ID) && entry.get(PatchNoteData.ID).getAsString().equals(id))
					return entry.has(CAT_PRIO) ? entry.get(CAT_PRIO).getAsInt() : 0;
			}
		}
		
		return -1;
	}
	
	/** Get the name of an entry with the specified id.
	 * 
	 * @param id The id to check for.
	 * @return The name if id is contained in data, "NOSUCHID" otherwise.
	 */
	public String getName(String id) {
		for(JsonElement element : data) {
			if(element.isJsonObject()) {
				JsonObject entry = element.getAsJsonObject();
				
				if(entry.has(PatchNoteData.ID) && entry.get(PatchNoteData.ID).getAsString().equals(id))
					return entry.has(PatchNoteData.NAME) ? entry.get(PatchNoteData.NAME).getAsString() : "NONAMEPROVIDED";
			}
		}
		
		return "NOSUCHID";
	}
	
	/** Add a new category to the stored data.
	 * 
	 * @param id The id of the category. Must not be contained in the data yet, or else this method will do nothing.
	 * @param name The name of the category.
	 * @param priority The priority of the category. Lower number means more important.
	 */
	public void addCategory(String id, String name, int priority) {
		if(!contains(id)) {
			JsonObject jo = new JsonObject();
			jo.addProperty(PatchNoteData.ID, id);
			jo.addProperty(PatchNoteData.NAME, name);
			jo.addProperty(CAT_PRIO, priority);
			data.add(jo);
		}
	}
}
