package uk.nhs.fhir.makehtml.data;

public enum ResourceFlag {
	SUMMARY("Σ", "This element is included in summaries"),
	MODIFIER("?!", "This element is a modifier element"),
	CONSTRAINED("I", "This element has or is affected by some invariants"),
	MUSTSUPPORT("S", "This element must be supported"),
	NOEXTEND("NE", "This element cannot have extensions");
	
	private final String flag;
	private final String desc;
	
	ResourceFlag(String flag, String desc) {
		this.flag = flag;
		this.desc = desc;
	}
	
	public String getFlag() {
		return flag;
	}
	
	public String getDesc() {
		return desc;
	}
}