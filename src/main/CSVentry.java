package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//figure out replacing periods in regex (author)

public class CSVentry {
	
	HashMap<String,String> map = new HashMap<String,String>();
	String[]keys; //gets headers
	String[]author;
	ArrayList<String> topic = new ArrayList<String>();
	ArrayList<String> genSubj = new ArrayList<String>();
	ArrayList<String> time = new ArrayList<String>();
	ArrayList<String> place = new ArrayList<String>();
	
	
			
	public CSVentry(String[]keys,String[]entry){
		int i = 0;
		while(i < entry.length){
			map.put(keys[i], entry[i]);
			i++;
		}
		while(i < keys.length){
			map.put(keys[i], "");
			i++;
		}
	}	
	public void cleanAll()throws InvalidRecordException{
		String ISBN = cleanISBN(map.get("010"), map.get("020"));
		this.map.put("020", ISBN);
		String title = cleanTitle(map.get("245$a"), map.get("245$b"));
		this.map.put("245$a", title);
		String pubPlace = cleanPubPlace(map.get("260$a"),map.get("264$a"));
		this.map.put("260$a", pubPlace);
		String publisher = cleanPublisher(map.get("260$b"),map.get("264$b"));
		this.map.put("260$b", publisher);
		String pubYear = cleanPubYear(map.get("260$c"),map.get("264$c"));
		this.map.put("260$c", pubYear);
		this.author = cleanAuthor(map.get("100$a"), map.get("245$c")); //last, first, middle, edited
 		this.topic = cleanTopic(map.get("650$a"));//genSubj, time, and place may return arraylists containing only an empty string
		this.genSubj = cleanTopic(map.get("650$x"),map.get("651$x"));
		this.time = cleanTopic(map.get("650$y"), map.get("651$y"));
		this.place = cleanTopic(map.get("650$z"), map.get("651$z"));
		
	}
	private ArrayList<String> cleanTopic(String topic1, String topic2) {
		ArrayList<String>topicList = new ArrayList<String>();
		if(topic1.equals("") && topic2.equals("")){
			topicList.add("");
			return topicList;
		}
		if(!topic1.equals("")){
			String[]temp1 = topic1.split(";");
			for(String item:temp1){
				item = item.replaceAll("\\.", "");
				item = item.trim();
				item = "\"" + item + "\"";
				if(!topicList.contains(item)){
					topicList.add(item);
				}
			}
		}
		if(!topic2.equals("")){
			String[]temp2 = topic2.split(";");
			for(String item: temp2){
				item = item.replaceAll("\\.", "");
				item = item.trim();
				item = "\"" + item + "\"";
				if(!topicList.contains(item)){
					topicList.add(item);
				}
			}
		}
		
		return topicList;
		
	}
	private ArrayList<String> cleanTopic(String topic) throws InvalidRecordException {
		ArrayList<String>topicList = new ArrayList<String>();
		if(topic.equals("")){
			InvalidRecordException e = new InvalidRecordException("invalid record");
			throw e;
		}
		topic = topic.replaceAll("\\.", "");
		boolean slash = false;
		ArrayList<String>tempList = new ArrayList<String>();
		
		if(topic.contains("/")){
			slash = true;
			String[]temp = topic.split("\\/");
			for(String item:temp){
				item = item.trim();
				tempList.add(item);
			}
		}
		if(slash){
			for (String item: tempList){
				String[]temp = item.split(";");
				for(String splitItem: temp){
					splitItem = splitItem.trim();
					splitItem = "\"" + splitItem + "\"";
					if(!topicList.contains(splitItem)){
						topicList.add(splitItem);
					}
				}
			}
		}
		else{
			String[]temp = topic.split(";");
			for(String item:temp){
				item = item.trim();
				item = "\"" + item + "\"";
				if(!topicList.contains(item)){
					topicList.add(item);
				}
			}
		}
		return topicList;
	}
	
	private String cleanPubYear(String pubYear, String pubYearAlt) throws InvalidRecordException {
		if(pubYear.equals("")){
			pubYear = pubYearAlt;
		}
		if(pubYear.equals("")){
			InvalidRecordException e = new InvalidRecordException("invalid record");
		}
		//split on comma
		String[]temp = pubYear.split(",");
		pubYear = temp[0];
		//remove square brackets
		pubYear = pubYear.replaceAll("\\[", "");
		pubYear = pubYear.replaceAll("\\]", "");
		//remove c
		pubYear = pubYear.replaceAll("c", "");
		//remove periods
		pubYear = pubYear.replaceAll("\\.", "");
		return pubYear.substring(0, 4);//gets rid of any double dates
	}
	
	private String cleanPublisher(String publisher, String publisherAlt)throws InvalidRecordException{
		if(publisher.equals("")){
			publisher = publisherAlt;
		}
		if(publisher.equals("")){
			InvalidRecordException e = new InvalidRecordException("invalid record");
			throw e;
		}  
		publisher = publisher.replaceAll(",", "").trim();
		return publisher;
	}
	
	private String cleanPubPlace(String pubPlace, String pubPlaceAlt) throws InvalidRecordException {
		String result = pubPlace;
		for(int i = 0; i < 2; i++){
			if(result.equals("")){
				result = pubPlaceAlt;
			}
			if(result.contains(";")){
				String[]temp = result.split(";");
				result = temp[0];
			}
			// URL that generated this code:
			// http://www.txt2re.com/index-java.php3?s=[Evanston,%20Ill.]&1 
			String re1="(\\[.*?\\])";	//gets everything in square braces
	
		    Pattern p = Pattern.compile(re1,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		    Matcher m = p.matcher(result);
		    if (m.find())
		    {
		    	result = result.replaceAll(re1, "");
		    }
		}
		result = result.replaceAll("\\.", "");
		if(result.length()  > 0){
		char end = result.charAt(result.length()-1);
		if(end == ':' || end == ','){
			result = result.substring(0, result.length()-1);
		}
		}
		String[]rmState = result.split(",");
		result = rmState[0];
		result = result.trim();
		//remove trailing whitespace
		if(result.equals(null) || result.equals("")){
			InvalidRecordException e = new InvalidRecordException("invalid record");
			throw e;
		}  
		else{
			return result;
		}
	}
	
	private String cleanTitle(String title, String subtitle) throws InvalidRecordException{
		String temp = title;
		int tSlash = temp.lastIndexOf("/");
		int tSpace = temp.lastIndexOf(" ");
		if (tSlash != -1){
			temp = temp.substring(0, tSlash - 1);
		}
		temp = temp.trim();
		if( subtitle != "" && temp.charAt(temp.length() - 1) != ':'){
			temp += ":";
		}
		temp += (" ");
		int sub = subtitle.indexOf('/');
		if (sub != -1){
			temp += (subtitle.substring(0, sub - 1).trim());
		}
		else{
			temp +=(subtitle.trim());
		}
		temp = "\"" + temp + "\"";
		return temp;
	}
	
	private String[] cleanAuthor (String last, String first)throws InvalidRecordException {
		String firstName = null;
		String lastName = null;
		String middleName = "";		
		String edited = "";
		//check for and clean value in last
		if(!last.equals("")){
			String[]temp = last.split(",");
			lastName = temp[0];
			String[] firstPlus = temp[1].trim().split(" ");
			firstName = firstPlus[0];
			if(firstPlus.length == 2){
				middleName = firstPlus[1];				
			}
		}
		//use the alt author param in first
		else if(!first.equals("")){
			String[]temp = first.split(" ");
			//edited
			int index = 0;			
			if(!Character.isUpperCase(temp[0].charAt(0))){
				for(int i = 0; i < temp.length; i++){
					if(Character.isUpperCase(temp[i].charAt(0))){
						index = i;
						edited = "edited";
						break;
					}
				}
			}
			//String[]temp = first.split(" ");
			firstName = temp[index];
			if(temp[index+1].matches("[a-zA-Z].")){
				middleName = temp[index+1];
				lastName = temp[index+2].replaceAll(",", "");
				}
			else{
				lastName = (temp[index+1].replaceAll(",", ""));
			}
		}
		else{
			InvalidRecordException e = new InvalidRecordException("invalid record");
			throw e;
		}
		
		String[]result = new String[]{lastName, firstName, middleName, edited};
		for (int i = 0; i < result.length; i++){
			String temp = "";
			temp = result[i].replaceAll("\\.","");
			temp = temp.trim();
			result[i] = temp;
		}
		return result; 	
	}
	
	private String cleanISBN(String ISBN10, String ISBN13)throws InvalidRecordException{
		//DB - not guaranteed to be populated
		//if empty, see if entry[2] - ISBN10 - is populated. If so, use.
		if(ISBN13 == ""){
			if(ISBN10 != ""){
				return ISBN10;
			}
			else{
				InvalidRecordException e = new InvalidRecordException("invalid record");
				throw e;
			}
		}
		//split on semicolon
		String[]ISBNsplit = ISBN13.split(";");
		//get rid of text description
		for(int i = 0; i < ISBNsplit.length; i++){
			int check = ISBNsplit[i].indexOf(" ");
			if (check != -1){
				ISBNsplit[i] = ISBNsplit[i].substring(0,check);
			}			
		}
		//if ends in X, move to second item in string. If no second entry, see entry[2]
		if (ISBNsplit[0].contains("X")){
			if (ISBNsplit.length > 1){
				return ISBNsplit[1];				
			}
			else{
				return ISBN10;
			}
		}
		return ISBNsplit[0];
	}
}