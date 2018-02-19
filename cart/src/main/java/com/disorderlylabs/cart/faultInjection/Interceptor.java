package com.disorderlylabs.cart.faultInjection;


import java.util.Map;
import java.util.Enumeration;
import java.util.HashMap;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


public class Interceptor extends HandlerInterceptorAdapter {


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {


        /*Fault injection flag will be in the form:
        <string,string> => <InjectFault, serviceName=fault1;fault2;fault3

        where fault is in the form:
        faulttype:param


        Example: <InjectFault, service1=DELAY:1000;DROP_PACKET:service3>


        */
        try {
            //if fault injection is set
            String faultKey = request.getHeader("InjectFault");
            if (faultKey != null) {
                String target[] = faultKey.split("=");

                //if current service is targeted
                String currentService = request.getRequestURI();
                if(target[0].equals(currentService)) {
                    String faultString = target[1];

                    String faults[] = faultString.split(Fault.SEQ_DELIM);

                    for (String a : faults) {
                        String f[] = a.split(Fault.FIELD_DELIM);

                        Fault.FAULT_TYPES fVal = Fault.FAULT_TYPES.valueOf(f[0]);
                        switch (fVal) {
                            case DELAY:
                                try {
                                    int duration = Integer.parseInt(f[1]);
                                    Thread.sleep(duration);
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid sleep duration");
                                }
                                break;
                            case DROP_PACKET:
                                System.out.println("do something");
                                break;
                            default:
                                System.out.println("fault type not supported");
                        }
                    }
                }
            }
        } catch (Exception exception) {
            System.out.println("Exception: " + exception.toString());
        }

        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        //nothing for now
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {

        //nothing for now
    }


    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Object handler) throws Exception {

        //nothing for now
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> httpHeaders = new HashMap<>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            httpHeaders.put(key, value);
            System.out.println("Key : " + key);
            System.out.println("Value : " + value);
        }
        return httpHeaders;
    }


} 

