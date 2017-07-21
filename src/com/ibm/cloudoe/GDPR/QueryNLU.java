package com.ibm.cloudoe.GDPR;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class QueryNLU {
	private static final String Context = "Context";
	private static final String ProductType = "ProductType";
	private static final String CustomerId = "Customer Id";
	private static final String ChargeType = "ChargeType";
	private static final String ProductID = "ProductID";
	private static final String ChargeAmount = "ChargeAmount";
	private static final String Sentiment = "Sentiment";
	
	  @POST
	  @Path("/context") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON) public JSONObject postMessage(String chatObj) {
		  URI comProfileURI;
		  
		  JSONObject ckpObj = null ;
		  String CustomerId = "" ;
		try {
			System.out.println("Chat: "+chatObj);
			JSONObject jObj = JSONObject.parse(chatObj) ;
			String text = (String)jObj.get("text") ;

			if (jObj.containsKey("CustomerId"))
				CustomerId =(String)jObj.get("CustomerId") ;
			comProfileURI = new URI("https://gateway-a.watsonplatform.net/calls/text/TextGetCombinedData?model=b89ece39-ac2a-4e05-ac2b-f39da73225db&apikey=13218112eacf3d42271bc85f652ce0303958c8ae&outputMode=json&extract=entities,typed-rels,doc-sentiment,doc-emotion&verbose=1").normalize();

			List comParams = new ArrayList();
			comParams.add(new BasicNameValuePair("text",text));
			

			String comBody = URLEncodedUtils.format(comParams, "utf-8");

						Request comProfileRequest = Request.Post(comProfileURI)
							.addHeader("Accept", "application/json")
						//	.addHeader("Content-Language", language)
						//	.addHeader("Accept-Language", locale)
							//.addHeader("Content-Type","application/json")
								.bodyString(comBody, ContentType.TEXT_PLAIN);

						Executor comEexecutor = Executor.newInstance();

						String comRespStr = comEexecutor.execute(comProfileRequest).returnContent().asString();
					//	System.out.println("Post execute Combined:"+comRespStr) ;
						JSONObject cCorpus = JSONObject.parse(comRespStr);
						JSONObject entityView = getEntitiesOverview((JSONArray)cCorpus.get("entities")) ;
						String sentiment = "" ;
						String emotion = "" ;
						if (cCorpus.containsKey("docSentiment")) 
						{
							JSONObject sentObj = (JSONObject)cCorpus.get("docSentiment") ;
							sentiment = (String)sentObj.get("type") ;
						}
						if (cCorpus.containsKey("docEmotions")) 
						{
							JSONObject emotObj = (JSONObject)cCorpus.get("docEmotions") ;
							HashMap emotMap = jsonToMap(emotObj) ;
							emotion = getMaxEmotion(emotMap) ;
						}
						 ckpObj = createCKPObject(entityView, emotion,CustomerId) ;
						System.out.println("CKP Object: "+ckpObj.toString()) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		  return ckpObj;
	  }
	  @POST
	  @Path("/personalInfo") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON) public JSONObject getContext(String chatObj) {
		  URI comProfileURI;
		  
		  JSONObject ckpObj = null ;
		  String CustomerId = "" ;
		try {
			System.out.println("Chat: "+chatObj);
			JSONObject jObj = JSONObject.parse(chatObj) ;
			String s = (String)jObj.get("text") ;
			s=s.replaceAll(" ", "%20") ;
        	s=s.replaceAll("\n", "%20") ;
        	s=s.replaceAll("\t", "%20") ;
        	 
        	String text = s ;
			if (jObj.containsKey("CustomerId"))
				CustomerId =(String)jObj.get("CustomerId") ;
			comProfileURI = new URI("https://gateway.watsonplatform.net/natural-language-understanding/api/v1/analyze?version=2017-02-27").normalize();

			List comParams = new ArrayList();
		//	comParams.add(new BasicNameValuePair("text",text));
	//		comParams.add(new BasicNameValuePair("features","relations,entities"));
			//comParams.add(new BasicNameValuePair("entities.model","b89ece39-ac2a-4e05-ac2b-f39da73225db")) ;

			String features = "{\"entities\": {},\"relations\": {},\"emotion\": {}}}";
				
			JSONObject	 jobj = new JSONObject() ;
				jobj.put("text", text);
				jobj.put("features", JSONObject.parse(features));
				Configuration configuration ;
				Client client = ClientBuilder.newClient();
				WebTarget target = client.target(comProfileURI);
				
				
			/*	String name = "da86a47f-18fb-41d9-85fe-25804a1e0e44";
				String password = "uFaft5CoQ2Qm";*/
			//NLU from fn ID -NLU-gd
				/*String modelId="10:028b84e5-1c27-41ac-9c27-1a8e7f0a5a9b";//"10:64afe45b-8146-455c-9ad7-b0901532f81d" ;
				String name = "1a0f618f-1aa9-438f-8831-191124f9b75f";
				String password = "m03RAkYKDL0O";
				*/
				// NLU from fn ID -NLU-1a
				
				String modelId="10:4685d2a4-8a93-449b-91c2-9a93b5c3efdd";
				String mod_id = System.getenv("MODEL_ID") ;
				System.out.println("MODEL_ID: "+mod_id); ;
				
				if (mod_id!=null)
					modelId = mod_id ;
				
				//"10:64afe45b-8146-455c-9ad7-b0901532f81d" ;
				
				String name = "55f7e051-e008-426d-afdf-a145ae360d55";
				String password = "Zdm5NRow74KI";
				//10:4685d2a4-8a93-449b-91c2-9a93b5c3efdd
				
				String authString = name + ":" + password;
			//	"55f7e051-e008-426d-afdf-a145ae360d55:Zdm5NRow74KI"
			//	Base64 objBase64 = new Base64();  
				//String authStringEnc=objBase64.encodeToString(authString.getBytes()); 
				 String authorizationHeaderName = "Authorization";
			     String authorizationHeaderValue = "Basic " + java.util.Base64.getEncoder().encodeToString( authString.getBytes() );
			 
				System.out.println("Base64 encoded auth string: " +authorizationHeaderValue);

				String comRespStr = target.resolveTemplate("profileName", "me")
						.queryParam("text", text).queryParam("features", "relations,entities,emotion")
						.queryParam("version", "2017-02-27")
						.queryParam("entities.model",modelId)
						.queryParam("relations.model",modelId)
						.request(MediaType.APPLICATION_JSON)
						.header(authorizationHeaderName, authorizationHeaderValue)
						.accept(MediaType.APPLICATION_JSON)
						.get(String.class);


		
				Response response = null;
										
			//	url = new URL(urlString);
				

			//	response = HTTPClient.get(comProfileURI,requestProperties, null);
						JSONObject cCorpus = JSONObject.parse(comRespStr);
						System.out.println("NLU output: "+comRespStr) ;
						
						ckpObj = cCorpus;
					/*	JSONObject entityView = getEntitiesOverview((JSONArray)cCorpus.get("entities")) ;
						String sentiment = "" ;
						String emotion = "" ;
						if (cCorpus.containsKey("docSentiment")) 
						{
							JSONObject sentObj = (JSONObject)cCorpus.get("docSentiment") ;
							sentiment = (String)sentObj.get("type") ;
						}
						if (cCorpus.containsKey("docEmotions")) 
						{
							JSONObject emotObj = (JSONObject)cCorpus.get("docEmotions") ;
							HashMap emotMap = jsonToMap(emotObj) ;
							emotion = getMaxEmotion(emotMap) ;
						}
						 ckpObj = createCKPObject(entityView, emotion,CustomerId) ;
						System.out.println("CKP Object: "+ckpObj.toString()) ;*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		  return ckpObj;
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
	private JSONObject createCKPObject(JSONObject entityViewObj,String sent,String CustomerId)
	{
		JSONObject ckpObj = new JSONObject() ;
		
		ckpObj.put("CustomerId",CustomerId) ;
		//Populate Product Type
		String productType="";
		if (entityViewObj.containsKey("Product"))
		{
			if (entityViewObj.containsKey("AccountType"))
				productType = (String)entityViewObj.get("AccountType") ;
			if (entityViewObj.containsKey("CardType"))
				productType = (String)entityViewObj.get("CardType") ;
			productType = productType+" "+(String)entityViewObj.get("Product") ;
			productType=productType.trim() ;
		}
		ckpObj.put(ProductType,productType) ;
		String productId="" ;
		if (entityViewObj.containsKey("AccountId"))
		{
			productId = (String)entityViewObj.get("AccountId") ;
		}
		if (entityViewObj.containsKey("CardNumber"))
		{
			productId = (String)entityViewObj.get("CardNumber") ;
		}
		ckpObj.put(ProductID,productId) ;
		String chargeType="" ;
		boolean charge = false ;
		if (entityViewObj.containsKey("OverdraftCharge"))
		{
			chargeType ="OverdraftCharge" ;
			charge = true ;
		}
		if (entityViewObj.containsKey("FinancialCharge"))
		{
			if (charge)
				chargeType=chargeType+"/";
			chargeType =chargeType+"FinancialCharge " ;
			charge = true ;
		}
		if (entityViewObj.containsKey("LatePaymentCharge"))
		{
			if (charge)
				chargeType=chargeType+"/";
			chargeType ="LatePaymentCharge " ;
			charge = true ;
		}
		chargeType=chargeType.trim() ;
		ckpObj.put(ChargeType,chargeType) ;
		String chargeAmt="" ;
		if (entityViewObj.containsKey("cAmount"))
		{
			chargeAmt = (String)entityViewObj.get("cAmount") ;
		}
		ckpObj.put(ChargeAmount,chargeAmt) ;
		
		ckpObj.put(Sentiment,sent) ;
		String context="" ;
		if (entityViewObj.containsKey("WaiveRequest")) //CancelRequest ReduceRequest ChargeInquiry
		
			context = "Waive Request" ;
			else if (entityViewObj.containsKey("CancelRequest"))
				context = "Cancel Request" ;
			else if (entityViewObj.containsKey("ReduceRequest"))
				context = "ReduceRequest" ;
			else  
				context = "Charge Inquiry" ;
			if (charge)
				context = context+" on "+chargeType ;
		
		
		ckpObj.put(Context,context) ;
		return ckpObj ;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
