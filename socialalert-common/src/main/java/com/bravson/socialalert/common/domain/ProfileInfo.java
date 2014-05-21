package com.bravson.socialalert.common.domain;

import java.net.URI;

import javax.validation.constraints.Size;

import org.joda.time.LocalDate;

public class ProfileInfo {

	@Size(max=30)
    private String lastname;
	@Size(max=30)
    private String firstname;
	private Gender gender;
	@Size(max=30)
	private String country;
	@Size(max=30)
	private String language;
	private LocalDate birthdate;
	private URI image;
	@Size(max=UserConstants.MAX_NICKNAME_LENGTH)
	private String nickname;
	@Size(max=200)
	private String biography;
	
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public LocalDate getBirthdate() {
		return birthdate;
	}
	public void setBirthdate(LocalDate birthdate) {
		this.birthdate = birthdate;
	}
	public URI getImage() {
		return image;
	}
	public void setImage(URI image) {
		this.image = image;
	}
	public String getBiography() {
		return biography;
	}
	public void setBiography(String biography) {
		this.biography = biography;
	}
	
}
