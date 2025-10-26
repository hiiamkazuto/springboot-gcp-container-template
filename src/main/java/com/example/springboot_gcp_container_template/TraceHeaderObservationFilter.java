package com.example.springboot_gcp_container_template;

import io.micrometer.observation.Observation.Scope;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.filter.ServerHttpObservationFilter;

/**
 * HTTPレスポンスにトレースIDを付与するフィルター
 * 
 * TODO: 将来的にSpring BootまたはMicrometerに標準機能が搭載された場合は削除可能
 */
@Configuration
public class TraceHeaderObservationFilter extends ServerHttpObservationFilter {

	private static final String TRACE_ID_HEADER_NAME = "traceresponse";

	private final Tracer tracer;

	public TraceHeaderObservationFilter(Tracer tracer, ObservationRegistry observationRegistry) {
		super(observationRegistry);
		Assert.notNull(tracer, "'tracer' must not be null");
		this.tracer = tracer;
	}

	@Override
	protected void onScopeOpened(@NonNull Scope scope, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
		var currentSpan = this.tracer.currentSpan();
		if (currentSpan != null && !currentSpan.isNoop()) {
			response.setHeader(TRACE_ID_HEADER_NAME, currentSpan.context().traceId());
		}
	}

}
