package com.disorderlylabs.app.controllers;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


//tracing
import brave.Span;
import brave.Tracing;
import brave.propagation.ExtraFieldPropagation;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;

@RestController
public class Controller {

  @Autowired
  JdbcTemplate jdbcTemplate;
  private static final String inventory_URL = "http://localhost:7002";

  @Qualifier("zipkinTracer")
  @Autowired
  private Tracing tracing;

  @RequestMapping("/")
  public String index() {
      System.out.println("Hello!");   	  
      return "Greetings from App Microservice!";
  }


  @RequestMapping("/a")
  public String test() {
    System.out.println("[TEST] App");

    OkHttpClient client = new OkHttpClient();
    Span span = tracing.tracer().newTrace().kind(Span.Kind.CLIENT);
    ExtraFieldPropagation.set(span.context(), "InjectFault", "Hello");


	try{
		String inventory = "http://" + System.getenv("inventory_ip") + "/b";
        System.out.println("Inventory_URL: " + inventory);

        Request.Builder request = new Request.Builder().url(inventory);

        tracing.propagation().injector(Request.Builder::addHeader)
                .inject(span.context(), request);

        Response response = client.newCall(request.build()).execute();
	}catch(Exception e) {
		return e.toString();
	}	
	return "";
  }


  @RequestMapping("/checkEnv")
  public String checkEnv() {
      return System.getenv("inventory_ip") + "";
  }

  //******--------For this particular request 'App' is acting like a forwarding node to 'Inventory'--------******.
  @RequestMapping(value = "/takeFromInventory", method = RequestMethod.PUT)
  public String takeFromInventory(@RequestParam(value="name", required=true) String name, @RequestParam(value="quantity", required=true) int quantity) 
  {
    try
    {
      String url = "http://" + System.getenv("inventory_ip") + "/takeFromInventory";
      HttpClient client = new DefaultHttpClient();
      HttpPut put = new HttpPut(url);

      List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
      urlParameters.add(new BasicNameValuePair("name", name));
      urlParameters.add(new BasicNameValuePair("quantity", quantity+""));

      put.setEntity(new UrlEncodedFormEntity(urlParameters));
      HttpResponse response = client.execute(put);

      return convertToString(response);   
    }
    catch(Exception e)
    {
      return e.toString();
    }
  }

  @RequestMapping(value = "/app/addToCart", method = RequestMethod.PUT)
  public String addToCart(@RequestParam(value="name", required=true) String name, @RequestParam(value="quantity", required=true) int quantity) 
  {
    try
    {
      System.out.println("HERE");	    
      String url = "http://" + System.getenv("inventory_ip") + "/takeFromInventory";
      HttpClient client = new DefaultHttpClient();
      HttpPut put = new HttpPut(url);

      List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
      urlParameters.add(new BasicNameValuePair("name", name));
      urlParameters.add(new BasicNameValuePair("quantity", quantity+""));

      put.setEntity(new UrlEncodedFormEntity(urlParameters));
      HttpResponse response = client.execute(put);

      JsonParser parser = new JsonParser();
      JsonObject o = parser.parse(convertToString(response)).getAsJsonObject();
      int ItemID = Integer.parseInt(o.get("ItemID").toString());
      double total_price = Double.parseDouble(o.get("total_price").toString());

      url = "http://" + System.getenv("cart_ip") + "/addToCart";
      put = new HttpPut(url);
      urlParameters = new ArrayList<NameValuePair>();
      urlParameters.add(new BasicNameValuePair("ItemID", ItemID + ""));
      urlParameters.add(new BasicNameValuePair("quantity", quantity+""));
      urlParameters.add(new BasicNameValuePair("total_price", total_price + ""));
      put.setEntity(new UrlEncodedFormEntity(urlParameters));
      response = client.execute(put);   
      return convertToString(response);   
    }
    catch(Exception e)
    {
      return e.toString();
    }    
  }

  String convertToString(HttpResponse response) throws IOException
  {
    if(response!=null)
    {
      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String line = "";
      String line2 = "";
      while ((line = rd.readLine()) != null) 
      {
        line2+=line+"\n";
      }
      return line2;
    }
    return "";
  }  
}  
