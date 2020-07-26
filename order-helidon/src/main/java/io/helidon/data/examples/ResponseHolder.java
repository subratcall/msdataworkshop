package io.helidon.data.examples;

import javax.json.bind.annotation.JsonbProperty;

public class ResponseHolder {
    private String message;
    @JsonbProperty(nillable = true)
    private String detail;


    public ResponseHolder(){};

    public ResponseHolder(String message, String detail) {
        this.message = message;
        this.detail = detail;
    }

}
