package nl.rijksoverheid.rdw.rde.client.lib;

public class HttpResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        if (isError())
            throw new IllegalStateException("No data in an error response.");

        return data;
    }

    public HttpResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public HttpResponse(T data) {
        this.code = 200;
        this.message = "OK";
        this.data = data;
    }

    public boolean isError() {
        return  200 > code && code >= 300;
    }
}
