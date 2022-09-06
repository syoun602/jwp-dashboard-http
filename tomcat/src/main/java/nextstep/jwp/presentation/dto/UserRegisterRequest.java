package nextstep.jwp.presentation.dto;

import nextstep.jwp.model.User;
import org.apache.coyote.http11.web.QueryParameters;

public class UserRegisterRequest {

    private final String account;
    private final String email;
    private final String password;

    public UserRegisterRequest(final String account, final String email, final String password) {
        this.account = account;
        this.email = email;
        this.password = password;
    }

    public static UserRegisterRequest from(final QueryParameters queryParameters) {
        final String account = queryParameters.getValueByKey("account");
        final String email = queryParameters.getValueByKey("email");
        final String password = queryParameters.getValueByKey("password");

        return new UserRegisterRequest(account, email, password);
    }

    public User toEntity() {
        return new User(account, password, email);
    }

    public String getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "UserRegisterRequest{" +
                "account='" + account + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
