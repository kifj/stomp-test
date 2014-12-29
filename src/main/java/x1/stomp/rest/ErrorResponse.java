package x1.stomp.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "response")
public class ErrorResponse {
	public ErrorResponse() {
		errors = new ArrayList<ErrorMessage>();
	}

	public ErrorResponse(List<ErrorMessage> errors) {
		this.errors = errors;
	}

	@XmlElementRef(name = "error")
	@JsonProperty(value = "error")
	public List<ErrorMessage> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorMessage> errors) {
		this.errors = errors;
	}

	public void add(ErrorMessage errorMessage) {
		errors.add(errorMessage);
	}

	@Override
	public String toString() {
		return "<ErrorResponse [errors=" + errors + "]>";
	}

	private List<ErrorMessage> errors;

}
