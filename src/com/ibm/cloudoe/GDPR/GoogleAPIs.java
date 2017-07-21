package com.ibm.cloudoe.GDPR;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
//import org.eclipse.persistence.internal.oxm.conversion.Base64;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

@Path("/rest")
public class GoogleAPIs {
	private static final String TARGET_URL = "https://vision.googleapis.com/v1/images:annotate?";
	private static final String API_KEY = "key=AIzaSyBtzn_SAfs9EoJ1JRwaaCFrxP6UYpnwYZ8";
	  @POST
	  @Path("/imageText") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON) public JSONObject getContext(String fileName) {
		  URI comProfileURI;
		  
		  JSONObject imageTextObj = new JSONObject() ;
		  String CustomerId = "" ;
		try {
		//	public static void detectText(String filePath, PrintStream out) throws IOException {
			
		
		URL serverUrl = new URL(TARGET_URL + API_KEY);
		URLConnection urlConnection = serverUrl.openConnection();
		HttpURLConnection httpConnection = (HttpURLConnection)urlConnection;
		httpConnection.setRequestMethod("POST");
		httpConnection.setRequestProperty("Content-Type", "application/json");
		httpConnection.setDoOutput(true);
		BufferedWriter httpRequestBodyWriter = new BufferedWriter(new
                OutputStreamWriter(httpConnection.getOutputStream()));
		httpRequestBodyWriter.write
		 ("{\"requests\":  [{ \"features\":  [ {\"type\": \"TEXT_DETECTION\""
		 +"}], \"image\": {\"source\": { \"gcsImageUri\":"
		 +" \"gs://kdimagebucket/"+fileName+"\"}}}]}");
		httpRequestBodyWriter.close();
		String response = httpConnection.getResponseMessage();
		if (httpConnection.getInputStream() == null) {
			   System.out.println("No stream");
			   return imageTextObj;
	}

		Scanner httpResponseScanner = new Scanner (httpConnection.getInputStream());
		String resp = "";
		while (httpResponseScanner.hasNext()) {
		   String line = httpResponseScanner.nextLine();
		   resp += line;
		 //  System.out.println(line);  //  alternatively, print the line of response
		}
		httpResponseScanner.close();
		JSONObject jObj = JSONObject.parse(resp) ;
		String textContent = (String) ((JSONObject) ((JSONObject)((JSONArray)jObj.get("responses")).get(0)).get("fullTextAnnotation")).get("text") ;
		System.out.println(textContent);
		imageTextObj.put("text", textContent) ;		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		  return imageTextObj;
	  }
	  public  HashMap jsonToMap(JSONObject jObject) throws Exception {

	        HashMap<String, Float> map = new HashMap<String, Float>();
	
	        Set keys = jObject.keySet();

	      //  while( keys.hasNext() ){
	        Iterator it = keys.iterator() ;
	   //     for (int i=0;i<keys.size();i++)
	     //   {
	       while (it.hasNext())
	        	{
	        		String key = (String)it.next();
	        		String value = (String)jObject.get(key); 
	        		Float score = Float.valueOf(value) ;
	        		map.put(key, score);

	        }

	        System.out.println("json : "+jObject);
	        System.out.println("map : "+map);
	        return map ;
	    }
	  private String getMaxEmotion(HashMap<String, Float> map)
	  {
	  	String emotion = "";
	      float maxValueInMap=(Collections.max(map.values()));  // This will return max value in the Hashmap
	      for (Entry<String, Float> entry : map.entrySet()) {  // Iterate through hashmap
	          if (entry.getValue()==maxValueInMap) {
	        	  emotion = entry.getKey() ;
//	              System.out.println("The Max value Class is --"+entry.getKey()+" : occur-"+maxValueInMap);     // Print the key with max value
	          }
	      }
	  	return emotion ;
	  }
	private JSONObject getEntitiesOverview(JSONArray entitiesArr)
	{
		JSONObject entityViewObj = new JSONObject() ;
		for (int i=0;i<entitiesArr.size();i++)
		{
			JSONObject entityObj = (JSONObject)entitiesArr.get(i);
			String type = (String)entityObj.get("type") ;
			String text = (String)entityObj.get("text") ;
		//	if (!entityViewObj.containsKey(type))
				entityViewObj.put(type, text) ;

		}
		return entityViewObj ;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
