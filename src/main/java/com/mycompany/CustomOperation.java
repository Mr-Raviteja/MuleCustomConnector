package com.mycompany;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class CustomOperation {

	private static final Logger logger = LoggerFactory.getLogger(CustomOperation.class);

	@MediaType(value = ANY, strict = false)
	@Alias("GET")
	public String getCall(@Config CustomConfiguration c) {
		String response = null;
		String protocol = c.getProtocol().equals("HTTPS") ? "https://" : "http://";
		logger.info("Sending Get Request");
		try {

			URL url = new URL(protocol + c.getHost() + ":" + c.getPort() + c.getBasepath());
			URLConnection con = url.openConnection();
			con.addRequestProperty("User-Agent", "Chrome");
			response=getHttpResponse(con);
			logger.info("Response Recieved");

		} catch (Exception e) {
			logger.info("Error Found ");
			e.printStackTrace();
		}

		return response;

	}


	private String getHttpResponse(URLConnection con) throws UnsupportedEncodingException,IOException{

		StringBuilder response=null;
		try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"))){
			response=new StringBuilder();
			String responseLine=null;
			while(null!= (responseLine=br.readLine())) {
				response.append(responseLine.trim());
			}
		}
		catch(Exception e) {
			logger.info("Error In Response Code");
			e.printStackTrace();
		}
		return response.toString();

	}

	@MediaType(value = ANY, strict =false)
	@Alias("POST")
	public String postCall(@Config CustomConfiguration c, @ParameterGroup(name= "Custom Params") CustomParameters p) {
		
		String response = null;
	
		String protocol = c.getProtocol().equals("HTTPS") ? "https://" : "http://";
		logger.info("Sending POST Request");

		try {					
					URL url = new URL(protocol + c.getHost() + c.getBasepath());
					URLConnection con = url.openConnection();
					con.addRequestProperty("User-Agent", "Chrome");
					String jsonString = "{\"name\": \""+p.getFirstName()+"\", \"job\":\""+p.getJob()+"\"}";
					con.setDoOutput(true);
					if(c.getProtocol().equals("HTTPS")){
						logger.info("Processing HTTPS POST request");
						HttpsURLConnection https = (HttpsURLConnection) con;
						https.setRequestMethod("POST");
						https.setRequestProperty("Content-Type","application/json; utf-8");
						try(OutputStream os = con.getOutputStream()){
							byte[] input = jsonString.getBytes("utf-8");
							os.write(input,0,input.length);
						}
						response = getHttpResponse(https);
					}
					else{
						logger.info("Processing HTTP request");
						HttpURLConnection http = (HttpURLConnection) con;
						http.setRequestMethod("POST");
						http.setRequestProperty("Content-Type","application/json; utf-8");
						try(OutputStream os = con.getOutputStream()){
							byte[] input = jsonString.getBytes("utf-8");
							os.write(input,0,input.length);
						}
						response = getHttpResponse(http);

					}
					
					logger.info("Response received.");
					
					
				}
				catch(Exception e) {
					logger.error("Error occured");
					e.printStackTrace();
				}
				return response;

	}
	
}
