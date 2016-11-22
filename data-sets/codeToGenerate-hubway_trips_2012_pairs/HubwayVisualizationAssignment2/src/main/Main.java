package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Main {

    public static void main( String[] args )
    {
    	HashMap<String, int[]> mapPairsToAttributes = new HashMap<String, int[]>();
    	
    	System.out.println("File processing - STARTING...");
    	processFile(mapPairsToAttributes); 
    	System.out.println("File processing - DONE!");	
    	
    	System.out.println("Generating output - STARTING!");
	   	createFile(mapPairsToAttributes);
		System.out.println("Generating output - DONE!");
    }
	
    public static void processLine(String line, HashMap<String, int[]> mapPairsToAttributes)
    {
//    	System.out.println(line);
    	
    	String[] splitLine = line.split(",",-1);
    	
//    	System.out.println(splitLine[12]);
    	
////	    String seq_id = splitLine[0].trim();
////	    String hubway_id = splitLine[1].trim();
////	    String status = splitLine[2].trim();
//	    String duration = splitLine[3].trim();
//	    String start_date = splitLine[4].trim();
//	    String strt_statn = splitLine[5].trim();
//	    String end_date= splitLine[6].trim();
//	    String end_statn= splitLine[7].trim();
////	    String bike_nr= splitLine[8].trim();
//	    String subsc_type= splitLine[9].trim();
////	    String zip_code= splitLine[10].trim();
//	    String birth_date= splitLine[11].trim();
//	    String gender = splitLine[12].trim();
    	
	    String duration = splitLine[0].trim();
	    String start_date = splitLine[1].trim();
	    String strt_statn = splitLine[2].trim();
	    String end_statn= splitLine[3].trim();
	    String subsc_type= splitLine[4].trim();
	    String birth_date= splitLine[5].trim();
	    String gender = splitLine[6].trim();
    			
	    DateFormat readFormat = new SimpleDateFormat( "M/dd/yyyy KK:mm", Locale.US);
	    Date date = null;
	    String[] dateSplit = null;
	    
	    try {
			date = readFormat.parse( start_date );
			dateSplit = date.toString().split(" ");
		} 
	    catch (ParseException e) 
	    {
			e.printStackTrace();
		}
	    
	    String day = dateSplit[0];
	    String month = dateSplit[1];
	    String year = dateSplit[5];
	    String hour = dateSplit[3].substring(0,2);
	    
//    	System.out.println(date);

	    
	    int durationInt = Integer.valueOf(duration);
	    int hourInt = Integer.valueOf(hour);

//    	System.out.println(hourInt);
    	
    	String pairStr = strt_statn + "," + end_statn;
    	
    	if ( mapPairsToAttributes.containsKey(pairStr) )
    	{
    		int[] thisPairAttributes = mapPairsToAttributes.get(pairStr);
    
    		thisPairAttributes[0] += 1;
    		thisPairAttributes[1] += durationInt;
    		
    		mapPairsToAttributes.put(pairStr, thisPairAttributes);
    		
    		thisPairAttributes = determineTimeSlot(thisPairAttributes, hourInt);
    		thisPairAttributes = determineSubscriptionType(thisPairAttributes, subsc_type);
    		thisPairAttributes = determineAgeGroup(thisPairAttributes, year, birth_date);
    		thisPairAttributes = determineGender(thisPairAttributes, gender);
    	}
    	else
    	{
    		int[] newPairAttributes = new int[18]; // frequency, meanDuration, timeSlot1F, timeSlot2F, timeSlot3F, timeSlot4F, timeSlotMissingF,
    											   // registeredF, casualF, missingSubscF, ageGrp1, ageGrp2, ageGrp3, ageGrp4, ageGrpMissing,
    											   // maleF, femaleF, missingGenderF
    		newPairAttributes[0] = 1;
    		newPairAttributes[1] = durationInt;
    		
    		newPairAttributes[2] = 0;
    		newPairAttributes[3] = 0;
    		newPairAttributes[4] = 0;
    		newPairAttributes[5] = 0;
    		newPairAttributes[6] = 0;
    		
    		newPairAttributes[7] = 0;
    		newPairAttributes[8] = 0;
    		newPairAttributes[9] = 0;
    		
    		newPairAttributes[10] = 0;
    		newPairAttributes[11] = 0;
    		newPairAttributes[12] = 0;
    		newPairAttributes[13] = 0;
    		newPairAttributes[14] = 0;
    		
    		newPairAttributes[15] = 0;
    		newPairAttributes[16] = 0;
    		newPairAttributes[17] = 0;
    		
    		newPairAttributes = determineTimeSlot(newPairAttributes, hourInt);
    		newPairAttributes = determineSubscriptionType(newPairAttributes, subsc_type);
    		newPairAttributes = determineAgeGroup(newPairAttributes, year, birth_date);
    		newPairAttributes = determineGender(newPairAttributes, gender);
    		
    		mapPairsToAttributes.put(pairStr, newPairAttributes);
    	}
    }
    
    public static int[] determineTimeSlot(int[] newPairAttributes, int hour)
    {
    	if (hour > 0 && hour <= 6)
    	{
    		newPairAttributes[2] += 1;
    	}
    	else if (hour > 6 && hour <= 12)
    	{
    		newPairAttributes[3] += 1;
    	}
    	else if (hour > 12 && hour <= 18)
    	{
    		newPairAttributes[4] += 1;
    	}
    	else if (hour > 18 && hour <= 0)
    	{
    		newPairAttributes[5] += 1;
    	}
    	else
    	{
    		newPairAttributes[6] += 1;
    	}
    	
    	return newPairAttributes;
    }
    
    public static int[] determineSubscriptionType(int[] newPairAttributes, String subsc_type)
    {
		switch(subsc_type)
		{
			case "Registered": newPairAttributes[7] += 1;
				break;    	
			case "Casual": newPairAttributes[8] += 1;
				break;		
			default: newPairAttributes[9] += 1;
				break;
		}
    	
    	return newPairAttributes;
    }
    
    public static int[] determineAgeGroup(int[] newPairAttributes, String year, String birth_date)
    {
    	int age = -1;
    	
    	if (!birth_date.equals(""))
    	{
    	   	int birthYr = Integer.valueOf(birth_date);
        	int currentYr = Integer.valueOf(year);
        	
        	age = currentYr - birthYr;
    	}
    	
    	if (age >= 0 && age <= 20)
    	{
    		newPairAttributes[10] += 1;
    	}
    	else if (age > 20 && age <= 40)
    	{
    		newPairAttributes[11] += 1;
    	}
    	else if (age > 40 && age <= 60)
    	{
    		newPairAttributes[12] += 1;
    	}
    	else if (age > 60)
    	{
    		newPairAttributes[13] += 1;
    	}
    	else // age = -1
    	{
    		newPairAttributes[14] += 1;	
    	}

    	return newPairAttributes;
    }
    
    public static int[] determineGender(int[] newPairAttributes, String gender)
    {
		switch(gender)
		{
			case "Male": newPairAttributes[15] += 1;
				break;    	
			case "Female": newPairAttributes[16] += 1; 
				break;		
			default: newPairAttributes[17] += 1; 
				break;
		}
    	
    	return newPairAttributes;
    }
    
    public static void calculateMeanDuration(HashMap<String, int[]> mapPairsToAttributes)
    {
    	for (String thisPair : mapPairsToAttributes.keySet())
    	{
    		int[] thisPairAttributes = mapPairsToAttributes.get(thisPair);
    		thisPairAttributes[1] = thisPairAttributes[1]/thisPairAttributes[0];
    	}
    }
    
    public static String processFile(HashMap<String, int[]> mapPairsToAttributes) {
        
    	String text = "";
    	
        try (BufferedReader br = new BufferedReader(new FileReader("/Users/arzam/Dropbox/d3/"
        		+ "Arzam_Saurabh_Vis_A2_V1/Hubway_Historical_Data_Explorer/hubway_trips_2012.csv"))) {
            
        	StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                
            	processLine(line, mapPairsToAttributes);
            	
            	sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            calculateMeanDuration(mapPairsToAttributes);
            
            text = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }	
    
    private static void createFile(HashMap<String, int[]> mapPairsToAttributes)
    {
     	try
     	{
     		FileWriter writer = new FileWriter("/Users/arzam/Desktop/new_hubway_trips_pairs_2012.csv");

     		for (String pair : mapPairsToAttributes.keySet())
     		{
         		writer.append(pair);
         		writer.append(",");
         	
         		int[] attributes = mapPairsToAttributes.get(pair);
         		
         		writer.append(String.valueOf(attributes[0]));
//         		
//         		int counter = 0;
//         		int attrCount = attributes.length;
//         		
//         		for (int attr : attributes)
//         		{
//         			counter++;
//             		writer.append(String.valueOf(attr));
//             		
//             		if (attrCount != counter)
//             		{
//                 		writer.append(",");             			
//             		}
//         		}

         		writer.append("\n");
     		}
	 			
     	    writer.flush();
     	    writer.close();
     	}
     	catch(IOException e)
     	{
     	     e.printStackTrace();
     	} 
     }
    
//    private static void createFile(String info)
//    {
//     	try
//     	{
////     		String previousContent = readVehicleFile();
//     		
//     		FileWriter writer = new FileWriter("/Users/arzam/Desktop/hubway_trips_pairs.csv");
////     		FileWriter writer = new FileWriter("/HubwayTripPairs.csv");
//     		 
////     	    writer.append(previousContent);
//     	    writer.append(info);
//	 			
//     	    writer.flush();
//     	    writer.close();
//     	}
//     	catch(IOException e)
//     	{
//     	     e.printStackTrace();
//     	} 
//     }
}
