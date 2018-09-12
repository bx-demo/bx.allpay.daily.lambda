package bx.allpay.daily.lambda;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import bx.allpay.daily.lambda.Request_return;;

public class Requests {
		public Request_return getreq(String table_name) throws Exception {
		final String USER_AGENT = "Mozilla/5.0";
		URL obj = new URL(System.getenv("allpay_url"));
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		Request_return req_object = new Request_return();
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("content-type", "application/soap+xml");
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new 	DataOutputStream(con.getOutputStream());
		String body = "";
		if(table_name.equals("EInfoMini")) {
			body = einfomini();
		}
		if(table_name.equals("EPayHist")) {
			body = epayhist();
		}
		wr.writeBytes(body);
		wr.flush();
		wr.close();
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + System.getenv("allpay_url"));
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;	
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		req_object.set_soapxml(response.toString());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
		    db = dbf.newDocumentBuilder();
		    InputSource is = new InputSource();
		    is.setCharacterStream(new StringReader(response.toString()));
		    try {
		        Document doc = db.parse(is);
		        NodeList nList = doc.getElementsByTagName("b:recordCount");
		        for (int temp = 0; temp < nList.getLength(); temp++) {
		            Node nNode = nList.item(temp);
		            req_object.set_count(Integer.parseInt(nNode.getTextContent()));
		            System.out.println("Current Element :" + nNode.getNodeName() + " value : " + nNode.getTextContent());
		        }
		    } catch (SAXException e) {
		    	System.out.println("Something went wrong with parser. "+e);
		    } 
		} catch (ParserConfigurationException e1) {
		    System.out.println("Parser Configuration Exception. "+e1);
		
		}
		return req_object;
	}
	    private String getdate() {
	    	SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd");
	    	Date date = new Date();
	    	String date_cur = formatter.format(date);
	    	return date_cur;
	    }
		private String epayhist() {
			String  request ="  <s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\">"+
		            "<s:Header>"+
		            "<a:Action s:mustUnderstand=\"1\">https://api.hralliance.net/IAllPayData/EPayHist</a:Action>"+
		            "<a:MessageID>urn:uuid:"+System.getenv("uuid")+"</a:MessageID>"+
					"<a:ReplyTo>"+
		            "<a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>"+
		            "</a:ReplyTo>"+
		            "<a:To s:mustUnderstand=\"1\">https://api.hralliance.net/AllPayData.svc/V1</a:To>"+
		            "</s:Header>"+
		            "<s:Body>"+
		            "<EPayHist xmlns=\"https://api.hralliance.net/\">"+
		            "<authorization xmlns:b=\"http://schemas.datacontract.org/2004/07/AllPayAPI\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"+
		            "<b:loginToken i:nil=\"true\"/>"+
		            "<b:password>"+System.getenv("password")+"</b:password>"+
		            "<b:userName>"+System.getenv("username")+"</b:userName>"+
		            "<b:userType>"+System.getenv("user")+"</b:userType>"+
		            "</authorization>"+
		            "<fieldsToPopulate>id,beginDate,endDate,checkDate,net,netCheck,regHours,regDollars,otHours,otDollars,gross,hours</fieldsToPopulate>"+
		            "<sortOrder>id</sortOrder>"+
					"<filter>"+" endDate > '"+getdate()+"T00:00:00'"+"</filter>"+
//					"<filter></filter>"+
		            "<queryOptions xmlns:b=\"http://schemas.datacontract.org/2004/07/AllPayAPI\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"+
		            "<b:batchSize>10000</b:batchSize>"+
		            "<b:queryIdentifier i:nil=\"true\"/>"+
		            "<b:startRecord>1</b:startRecord>"+
		            "</queryOptions>"+
		            "</EPayHist>"+
		            "</s:Body>"+
		            "</s:Envelope>";
			return request;

		}
		private String einfomini() {
			String request ="  <s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\">"+
		            "<s:Header>"+
		            "<a:Action s:mustUnderstand=\"1\">https://api.hralliance.net/IAllPayData/EInfoMini</a:Action>"+
		            "<a:MessageID>urn:uuid:"+System.getenv("uuid")+"</a:MessageID>"+
					"<a:ReplyTo>"+
		            "<a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>"+
		            "</a:ReplyTo>"+
		            "<a:To s:mustUnderstand=\"1\">https://api.hralliance.net/AllPayData.svc/V1</a:To>"+
		            "</s:Header>"+
		            "<s:Body>"+
		            "<EInfoMini xmlns=\"https://api.hralliance.net/\">"+
		            "<authorization xmlns:b=\"http://schemas.datacontract.org/2004/07/AllPayAPI\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"+
		            "<b:loginToken i:nil=\"true\"/>"+
		            "<b:password>"+System.getenv("password")+"</b:password>"+
		            "<b:userName>"+System.getenv("username")+"</b:userName>"+
		            "<b:userType>"+System.getenv("user")+"</b:userType>"+
		            "</authorization>"+
		            "<fieldsToPopulate>id,lastname,firstname,hiredate,termdate,empstatus</fieldsToPopulate>"+
		            "<sortOrder>id</sortOrder>"+
		            "<filter>empStatus='A'</filter>"+
		            "<queryOptions xmlns:b=\"http://schemas.datacontract.org/2004/07/AllPayAPI\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"+
		            "<b:batchSize>10000</b:batchSize>"+
		            "<b:queryIdentifier i:nil=\"true\"/>"+
		            "<b:startRecord>0</b:startRecord>"+
		            "</queryOptions>"+
		            "</EInfoMini>"+
		            "</s:Body>"+
		            "</s:Envelope>";
			return request;
		}

}

