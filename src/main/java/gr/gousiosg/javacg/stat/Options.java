package gr.gousiosg.javacg.stat;

import java.util.ArrayList;


/**
 * Enum class to allow for different option arguments in the future and variations of those argument flags
 */


public enum Options {
	
	MODIFIER( new String[]{"-m", "--modifier"});
	
	private String[] references;
	Options(String[] args){
		references = args;
	}
	public boolean matches(ArrayList<String> options) {
		for(String reference: references) {
			if(options.contains(reference)) {
				return true;
			}
		}
		return false;
	}
	public String[] getReferences() {
		return references;
	}
	public boolean containsReference(String option) {
		for(String ref: references) {
			if(ref.equals(option))
				return true;
		}
		return false;
	}
	public static ArrayList<String> getAllReferences() {
		ArrayList<String> allReferences = new ArrayList<String>();
		Options[] opts = Options.class.getEnumConstants();
		for(Options opt : opts) {
			String[] refs = opt.getReferences();
			for(String ref: refs) {
				allReferences.add(ref);
			}
			
		}
		return allReferences;
	}
	
}