package com.disorderlylabs.cart;


import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//tracing
import brave.Tracing;
import brave.sampler.Sampler;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;

import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;



@SpringBootApplication
public class CartApplication {

    @Bean
    public Tracing zipkinTracer() {
        OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v2/spans");
        AsyncReporter<zipkin2.Span> spanReporter = AsyncReporter.create(sender);

        Tracing tracing = Tracing.newBuilder()
                .localServiceName("application")
                .spanReporter(spanReporter)
                .traceId128Bit(true)
                .sampler(Sampler.ALWAYS_SAMPLE)
                .propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY,"InjectFault"))
                .build();

        return tracing;
    }


	public static void main(String[] args) {
		SpringApplication.run(CartApplication.class, args);
	}
}
