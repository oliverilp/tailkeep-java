package org.tailkeep.adapter.ytdlp.url;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tailkeep.domain.download.ProbeUrl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpProbeUrlTest {
    private final HttpProbeUrl probe = new HttpProbeUrl();
    private final List<String> methods = new ArrayList<>();
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/found", exchange -> {
            methods.add(exchange.getRequestMethod());
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/missing", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    @Test
    void headStatus_ShouldSendAHeadRequestAndReturnTheStatus() {
        assertThat(probe.headStatus(url("/found"))).isEqualTo(200);
        assertThat(methods).containsExactly("HEAD");
    }

    @Test
    void headStatus_ShouldReturnAnErrorStatusAsIs() {
        assertThat(probe.headStatus(url("/missing"))).isEqualTo(404);
    }

    @Test
    void headStatus_WhenNothingListens_ShouldFail() {
        int port = server.getAddress().getPort();
        server.stop(0);

        assertThatThrownBy(() -> probe.headStatus("http://127.0.0.1:" + port + "/found"))
                .isInstanceOf(ProbeUrl.UrlProbeException.class);
    }
}
