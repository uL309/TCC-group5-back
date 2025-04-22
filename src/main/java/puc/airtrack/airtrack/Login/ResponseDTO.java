package puc.airtrack.airtrack.Login;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResponseDTO(@JsonProperty("username") String username, @JsonProperty("auth-token") String JWT) {
}
