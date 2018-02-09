package com.disorderlylabs.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//tracing 
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;

import brave.Tracing;
import brave.sampler.Sampler;
import brave.opentracing.BraveTracer;

import io.opentracing.Tracer;
import io.opentracing.contrib.okhttp3.TracingInterceptor;

import okhttp3.OkHttpClient;

import zipkin.Span;
import zipkin.reporter.Encoding;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

import java.util.Arrays;


@SpringBootApplication
public class AppApplication {

	@Bean
    public Tracer zipkinTracer() {        
        String zipkinURL = "http://10.0.0.24:9411/api/v1/spans";
        System.out.println("ZIPKIN_URL: " + zipkinURL);
        OkHttpSender okHttpSender = OkHttpSender.builder()
                .encoding(Encoding.JSON)
                .endpoint(zipkinURL)
                .build();
        AsyncReporter<Span> reporter = AsyncReporter.builder(okHttpSender).build();
        Tracing braveTracer = Tracing.newBuilder()
                .localServiceName("fault")
                .reporter(reporter)
                .traceId128Bit(true)
                .sampler(Sampler.ALWAYS_SAMPLE)
                .build();
        return BraveTracer.create(braveTracer);
    }

    @Bean
    public OkHttpClient httpClient(@Qualifier("zipkinTracer") Tracer tracer) {
        TracingInterceptor tracingInterceptor = new TracingInterceptor(tracer, Arrays.asList());

        return new OkHttpClient.Builder()
                .addInterceptor(tracingInterceptor)
                .addNetworkInterceptor(tracingInterceptor)
                .build();
    }

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}
}
