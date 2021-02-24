package fr.abes.lnevent.security.payload.request;

import javax.validation.constraints.NotBlank;

public class LoginRequest {

	@NotBlank
	private String username; //siren

	@NotBlank
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}