package nextstep.jwp.controller;

import nextstep.jwp.http.request.HttpRequest;
import nextstep.jwp.http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultController extends AbstractController {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultController.class);
    private static final DefaultController DEFAULT_CONTROLLER = new DefaultController();

    private DefaultController(){
    }

    public static DefaultController getInstance() {
        return DEFAULT_CONTROLLER;
    }

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        LOG.debug("HTTP GET Resource Request: {}", request.getPath());
        response.responseOk(request.getPath());
    }
}
