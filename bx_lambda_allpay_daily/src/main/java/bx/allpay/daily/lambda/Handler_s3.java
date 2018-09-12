package bx.allpay.daily.lambda;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import bx.allpay.daily.lambda.Request_return;
import bx.allpay.daily.lambda.Requests;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class Handler_s3 {

    public void process() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(System.getenv("access_key"),System.getenv("secret_key"));
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                                .withRegion("us-east-1")
                                .build();
        Requests req=new Requests();
        try {
    		Request_return xmlsour = req.getreq("EInfoMini");
            if(xmlsour.record_count>0) {
            	String output = tocsv(xmlsour.soapxml,s3,"allpay.files","einfomini_style.xslt");
            	String final_output = normalize_string(output,5);
            	upload(s3,final_output,"redshift.dump","einfo");
            }
            else {
            	System.out.println("The returned record count is 0. Did not create any EInfoMini file");
            }
             xmlsour = req.getreq("EPayHist");
            if(xmlsour.record_count>0) {
            	String output = tocsv(xmlsour.soapxml,s3,"allpay.files","epayhist_style.xslt");
            	String final_output = normalize_string(output,11);
            	upload(s3,final_output,"redshift.dump","epayhist");
            }
            else {
            	System.out.println("The returned record count is 0. Did not create any file for EPayHist");
            }
            System.out.println("Finished ");
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3 ");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
    static private String getdate() {
    	SimpleDateFormat formatter = new SimpleDateFormat("MMddYYYY");
    	Date date = new Date();
    	String date_cur = formatter.format(date);
    	return date_cur;
    	
    }
    static private String normalize_string(String csv_source,int para_length) {
    	String output = csv_source.trim();
    	String[] output_line = output.split("\\|");
    	String[] output_column= null;
    	String final_output = "";
    	for(int i=0;i<output_line.length;i++) {
    		if(i==0) {
    			final_output+= output_line[i].trim();
    			final_output+="|";
    		}
    		else if(i%para_length==0 && i!=output_line.length-1) {
    			output_column = output_line[i].split(" ",2);
    			final_output+= output_column[0].trim();
    			final_output+="\n";
    			final_output+= output_column[1].trim();
    			final_output+="|";
    		}
    		else if(i%para_length!=0 && i!=output_line.length-1) {
    			final_output+=output_line[i].trim();
    			final_output+="|";
    		}
    		else {
    			final_output+=output_line[i].trim();
    			final_output+="\n";
    		}
    	}
    	return final_output;
    }
    static private void upload(AmazonS3 s3, String final_output,String bucket_name,String subfolder) throws Exception {
    	InputStream inStream = new ByteArrayInputStream(final_output.getBytes(StandardCharsets.UTF_8));
    	Long contentLength = Long.valueOf(final_output.getBytes().length);
    	ObjectMetadata metadata = new ObjectMetadata();
    	metadata.setContentLength(contentLength);
    	System.out.println("Content Byte size "+contentLength);
    	System.out.println("Starting transfer");
    	s3.putObject(new PutObjectRequest(bucket_name, subfolder+"/"+getdate(), inStream, metadata));
    }
    static private String tocsv(String soapxml, AmazonS3 s3,String bucket_name,String key) throws Exception {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder builder = factory.newDocumentBuilder();
    	InputSource inputStream = new InputSource(new StringReader(soapxml));
    	Document document = builder.parse(inputStream);
    	StreamSource stylesource = new StreamSource(download(s3,bucket_name,key));
    	Transformer transformer = TransformerFactory.newInstance()
            .newTransformer(stylesource);
    	Source source = new DOMSource(document);
    	StringWriter writer = new StringWriter();
    	transformer.transform(source,new StreamResult(writer));
    	return writer.getBuffer().toString();
    }
    static private S3ObjectInputStream download(AmazonS3 s3, String bucketName, String key) throws Exception {
    	System.out.println("Downloading an object");
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
        System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
        return object.getObjectContent();
    }
}
