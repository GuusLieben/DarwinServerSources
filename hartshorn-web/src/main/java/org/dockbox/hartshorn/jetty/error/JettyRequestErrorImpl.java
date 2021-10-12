package org.dockbox.hartshorn.jetty.error;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.di.context.ApplicationContext;
import org.dockbox.hartshorn.di.context.DefaultCarrierContext;
import org.dockbox.hartshorn.web.error.RequestError;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
public class JettyRequestErrorImpl extends DefaultCarrierContext implements RequestError {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final int statusCode;
    private final PrintWriter writer;
    private final Exceptional<Throwable> cause;
    @Setter private String message;
    @Setter private boolean yieldDefaults = false;

    public JettyRequestErrorImpl(final ApplicationContext applicationContext, final HttpServletRequest request, final HttpServletResponse response, final int statusCode, final PrintWriter writer, final String message, final Throwable cause) {
        super(applicationContext);
        this.request = request;
        this.response = response;
        this.statusCode = statusCode;
        this.writer = writer;
        this.message = message;
        this.cause = Exceptional.of(cause, cause);
    }
}