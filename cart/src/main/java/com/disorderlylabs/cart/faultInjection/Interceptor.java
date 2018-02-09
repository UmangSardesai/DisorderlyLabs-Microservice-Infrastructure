package com.disorderlylabs.cart.faultInjection;

import java.util.Map;
import java.util.Enumeration;
import java.util.HashMap;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.TextMapExtractAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import static io.opentracing.propagation.Format.Builtin.HTTP_HEADERS;

public class Interceptor extends HandlerInterceptorAdapter {
    private Tracer tracer;

    @Autowired
    public Interceptor(Tracer tracer) {
        this.tracer = tracer;
    }

	@Override
	public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        Map<String, String> httpHeaders = getHeaders(request);

        TextMapExtractAdapter carrier = new TextMapExtractAdapter(httpHeaders);
        SpanContext parentSpan = tracer.extract(HTTP_HEADERS, carrier);

        Span child = tracer.buildSpan("fault injection")
                           .asChildOf(tracer.activeSpan())
                           .startManual();


        /*Fault injection flag will be in the form:
        <string,string> => <inject-[servicename], fault1;fault2;fault3

        where fault is in the form:
        faulttype:param

        Example: <inject-service1, DELAY:1000;DROP_PACKET:service3>
        */
        String headerKey = "inject-service1";

        try {
            if (parentSpan != null) {

                //if our service is targeted for fault injection
                if(httpHeaders.containsKey(headerKey)) {
                    String headerVal = httpHeaders.get(headerKey);
                    String faults[] = headerVal.split(Fault.SEQ_DELIM);

                    for(String a : faults) {
                        String f[] = a.split(Fault.FIELD_DELIM);

                        Fault.FAULT_TYPES fVal = Fault.FAULT_TYPES.valueOf(f[0]);
                        switch(fVal) {
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
            System.out.println("we had an exception");
        } finally {
            child.finish();
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
    public void afterCompletion (HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler,
                                 Exception ex) throws Exception {

        //nothing for now
    }


    @Override
    public void afterConcurrentHandlingStarted (HttpServletRequest request,
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

