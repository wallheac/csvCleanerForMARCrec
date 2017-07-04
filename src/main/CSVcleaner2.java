package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class CSVcleaner2 {
	String fileLoc = "C:\\Users\\amy\\Documents\\Codergirl\\Neo4j\\MARC to CSV\\larger datasets\\laborAndBusinessPipe.txt";
	String[]headers = null;
	String[]entry = null;
	ArrayList<CSVentry>all = new ArrayList<CSVentry>();
	int topicGreatest = 0;
	int genSubjGreatest = 0;
	int timeGreatest = 0;
	int placeGreatest = 0;
		
	public static void main(String[]args){
		CSVcleaner2 cleaner = new CSVcleaner2();
		cleaner.parseFile();
		cleaner.printFile(cleaner.all);
	}
	public void parseFile(){		
		
		String line = "";
		String splitBy = "\\|";
		try(BufferedReader br = new BufferedReader(new FileReader(this.fileLoc))){
			while((line = br.readLine()) != null){
				this.entry = line.split(splitBy);		
				//save headers for any alterations
				if (entry[0].equals("001")){
					this.headers = entry;
				}
				if (!entry[0].equals("001")){
					//cope with quotation marks
					for(int item = 0; item < entry.length; item++){
						entry[item] = entry[item].replaceAll("\"", "");						
					}
					CSVentry temp = null;
					try{
						temp = new CSVentry(headers, entry);
						temp.cleanAll();
					}catch(InvalidRecordException|ArrayIndexOutOfBoundsException e){
						temp = null;
					}
					if(temp != null){
						if(temp.topic.size() > topicGreatest){
							topicGreatest = temp.topic.size();
						}
						if(temp.genSubj.size() > genSubjGreatest){
							genSubjGreatest = temp.genSubj.size();
						}
						if(temp.time.size() > timeGreatest){
							timeGreatest = temp.time.size();
						}
						if(temp.place.size() > placeGreatest){
							placeGreatest = temp.place.size();
						}					
						this.all.add(temp);
					}
				}				
			}
		
		}catch (IOException e){
			System.out.println("file not found: " + e);
		}
	}
	
	public void printFile(ArrayList<CSVentry>all){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\amy\\Documents\\Codergirl\\Neo4j\\MARC to CSV\\larger datasets\\CleanLibraryCSV.txt"));
			String joinBy = ",";
			String[]fields = {"020","245$a","260$a","260$b","260$c"};
			Iterator<CSVentry> itr = all.iterator();
			
			//write headers
			for(String field: fields){
				writer.write(field + joinBy);
			}
			writer.write("last" + joinBy);
			writer.write("first" + joinBy);
			writer.write("middle" + joinBy);
			writer.write("edited" + joinBy);
			for (int i = 1; i <= topicGreatest; i++){
				String temp = "topic" + (char)(i + 48) + joinBy;
				writer.write(temp);
			}
			for (int i = 1; i <= genSubjGreatest; i++){
				String temp = "genSubj" + (char)(i + 48) + joinBy;
				writer.write(temp);
			}
			for (int i = 1; i <= timeGreatest; i++){
				String temp = "time" + (char)(i + 48) + joinBy;
				writer.write(temp);
			}
			for (int i = 1; i <= placeGreatest; i++){
				String temp = "place" + (char)(i + 48) + joinBy;
				writer.write(temp);
			}
			writer.write('\n');
			
			
			while (itr.hasNext()){
				CSVentry temp = itr.next();
				for(String field: fields){
					writer.write(temp.map.get(field));
					writer.write(joinBy);
				}
				for(String data: temp.author){					
					writer.write(data);
					writer.write(joinBy);
					}
				for(int i = 0; i < topicGreatest; i++){
					if (i < temp.topic.size()){
						writer.write(temp.topic.get(i));
						}
					else{
						writer.write("");
					}
					writer.write(joinBy);
					}
				for(int i = 0; i < genSubjGreatest; i++){
					if (i < temp.genSubj.size()){
						writer.write(temp.genSubj.get(i));
						}
					else{
						writer.write("");
						}
					writer.write(joinBy);
					}
				for(int i = 0; i < timeGreatest; i++){
					if (i < temp.time.size()){
						writer.write(temp.time.get(i));
						}
					else{
						writer.write("");
						}
					writer.write(joinBy);
					}
				for(int i = 0; i < placeGreatest; i++){
					if (i < temp.place.size()){
						writer.write(temp.place.get(i));
						}
					else{
						writer.write("");
						}
					writer.write(joinBy);
					}
				writer.write('\n');
			}
			writer.close();
		}catch (IOException e){
			System.out.println("cannot open file" + e);
		}
		
	}
}