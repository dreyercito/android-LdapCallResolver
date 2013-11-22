package sc.luna.ldapcallresolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Rule {
    public String regexpIn;
    public String regexpOut;
    public int prio;
    public boolean stop;
    public int id=-1;
    Rule(int id, int prio, String regexpIn, String regexpOut, boolean stop) {
    	this.id=id;
    	this.prio=prio;
    	this.regexpIn=regexpIn;
    	if(regexpIn==null) {
    		this.regexpIn="";
    	}
    	this.regexpOut=regexpOut;
    	if(regexpOut==null) {
    		this.regexpOut="";
    	}
    	this.stop=stop; 
    }
    
	public boolean matches(String outNumber) {
		if(outNumber==null) {
			return false;
		}
		return outNumber.matches(regexpIn);
	}
	
	// Replaces capture groups in the second string in fromat ${1} .. ${2} etc...
	public String process(String outNumber) {
		int groups;
		
		if(outNumber==null) {
			return null;
		}
		
		String processedNumber=regexpOut;
	    Pattern matchPattern = Pattern
	                .compile(regexpIn, Pattern.CASE_INSENSITIVE);
	    Matcher m = matchPattern.matcher(outNumber);
	    
	    if(!m.matches()) {
	        return null;
	    }
	    
	    groups=m.groupCount();
	    if(groups<=0) {
	    	return processedNumber;	    	
	    }
	    
	    for (int i=1;i<=groups;i++ ){
	    	String captured = m.group(i);
	    	String captureReplace = "${"+Integer.toString(i)+"}";
	    	processedNumber = processedNumber.replace(captureReplace, captured);
	    }	   
		return processedNumber;
	}
}
