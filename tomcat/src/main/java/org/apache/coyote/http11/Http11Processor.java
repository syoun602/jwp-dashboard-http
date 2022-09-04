package org.apache.coyote.http11;

import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.exception.UncheckedServletException;
import nextstep.jwp.model.User;
import org.apache.coyote.Processor;
import org.apache.coyote.http11.domain.HttpStatus;
import org.apache.coyote.http11.domain.QueryParameters;
import org.apache.coyote.http11.exception.InvalidHttpRequestStartLineException;
import org.apache.coyote.http11.presentation.HttpRequest;
import org.apache.coyote.http11.presentation.HttpResponse;
import org.apache.coyote.http11.util.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);
    private static final String QUERY_STRING_PREFIX = "\\?";
    private static final String HTML_EXTENSION = ".html";
    private static final String ACCOUNT_KEY = "account";
    private static final String PASSWORD_KEY = "password";
    private static final int PATH_INDEX = 0;
    private static final int QUERY_STRING_INDEX = 1;

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream();
             final var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            final List<String> lines = readLines(bufferedReader);
            final HttpRequest httpRequest = HttpRequest.of(lines);
            final HttpResponse httpResponse = createHttpResponse(httpRequest, parseUri(httpRequest));
            sendResponse(outputStream, httpResponse);
        } catch (IOException | UncheckedServletException | InvalidHttpRequestStartLineException e) {
            log.error(e.getMessage(), e);
        }
    }

    private List<String> readLines(final BufferedReader bufferedReader) throws IOException {
        final ArrayList<String> lines = new ArrayList<>();
        String line;

        while (!(line = bufferedReader.readLine()).isBlank()) {
            lines.add(line);
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("요청 값이 존재하지 않습니다.");
        }

        return lines;
    }

    private String parseUri(final HttpRequest httpRequest) {
        final String uri = httpRequest.getUri();

        if (uri.contains(QUERY_STRING_PREFIX)) {
            final String[] splitUri = uri.split(QUERY_STRING_PREFIX);
            final String path = splitUri[PATH_INDEX];
            final String queryString = splitUri[QUERY_STRING_INDEX];
            final QueryParameters queryParameters = QueryParameters.from(queryString);
            logUser(queryParameters);

            return path + HTML_EXTENSION;
        }

        return uri;
    }

    private void logUser(final QueryParameters queryParameters) {
        final String account = queryParameters.getValueByKey(ACCOUNT_KEY);
        final Optional<User> user = InMemoryUserRepository.findByAccount(account);
        user.ifPresent(it -> validatePassword(queryParameters, it));
        log.info(user.toString());
    }

    private void validatePassword(final QueryParameters queryParameters, final User it) {
        if (it.checkPassword(queryParameters.getValueByKey(PASSWORD_KEY))) {
            throw new IllegalArgumentException("아이디나 비밀번호가 일치하지 않습니다.");
        }
    }

    private HttpResponse createHttpResponse(final HttpRequest httpRequest, final String uri) throws IOException {
        return new HttpResponse(HttpStatus.OK, httpRequest.getAcceptType(), ResourceLoader.getContent(uri));
    }

    private void sendResponse(final OutputStream outputStream, final HttpResponse httpResponse) throws IOException {
        final String formattedResponse = httpResponse.format();
        outputStream.write(formattedResponse.getBytes());
        outputStream.flush();
    }
}
