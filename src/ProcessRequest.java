public class ProcessRequest {
    /**
     * Constructs a ProcessRequest object with the specified status, message, and value.
     *
     * @param status  the status of the request processing (true if successful, false otherwise)
     * @param message the message associated with the request processing
     * @param value   the value associated with the request processing
     */
    public ProcessRequest(Boolean status, String message, String value) {
        this.status = status;
        this.message = message;
        this.value = value;
    }

    public Boolean status;
    public String message;
    public String value;
}
