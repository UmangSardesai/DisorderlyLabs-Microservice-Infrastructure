package com.disorderlylabs.cart.controllers;

import com.disorderlylabs.cart.faultInjection.Fault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.jdbc.core.JdbcTemplate;
import com.disorderlylabs.cart.mappers.CartMapper;
import com.disorderlylabs.cart.repositories.Cart;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.util.EntityUtils; 
import org.apache.http.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

//tracing
import brave.Tracing;

@RestController
public class Controller {

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Qualifier("zipkinTracer")
  @Autowired
  private Tracing tracing;

  @RequestMapping("/")
  public String index() {
      return "Greetings from Cart App!";
  }

  
  @RequestMapping("/c")
  public String test(HttpServletRequest request) {

	System.out.println("[TEST] Cart");

    Fault.getHeaders(request);

      return "";
  }

  @RequestMapping(value = "/addToCart", method = RequestMethod.PUT)
  public String addToCart(@RequestParam(value="ItemID", required=true) int ItemID, @RequestParam(value="quantity", required=true) int quantity, @RequestParam(value="total_price", required=true) double total_price)
  {
    try
    {
      String sql = "insert into cart values ("+ ItemID +", "+ quantity+", "+ total_price+")";
      jdbcTemplate.execute(sql);
      return "{\"status\":\"success\"}";      
    }
    catch (Exception e)
    {
      return "{\"status\":\"failure\"}";
    }
  }  

  @RequestMapping(value = "/getCartItems", method = RequestMethod.GET)
  public String addToCart()
  {
    try
    {
      String sql = "select * from cart";
      ArrayList<Cart> cartItems = new ArrayList<Cart>(jdbcTemplate.query(sql, new CartMapper()));
      String ans = "";
      for (Cart cart: cartItems)
        ans = ans + cart.toString();
      return ans;
    }
    catch(Exception e)
    {
      return e.getMessage();
    }
  }

  JsonArray convertToJsonArray(HttpResponse response) throws IOException
  {
    if (response!=null)
    {
      String json = EntityUtils.toString(response.getEntity(), "UTF-8");
      Gson gson = new Gson();
      JsonObject body = gson.fromJson(json, JsonObject.class);
      JsonArray results = body.get("results").getAsJsonArray();
      return results;
    }
    return null;
  }    
}
