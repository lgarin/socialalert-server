package com.bravson.socialalert.app.domain;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.brickred.socialauth.util.BirthDate;
import org.joda.time.LocalDate;

import com.bravson.socialalert.common.domain.Gender;

public class ExternalProfileInfo {

	private String identifier;
	private String email;
	private String nickname;
    private String lastname;
    private String firstname;
	private Gender gender;
	private String country;
	private String language;
	private LocalDate birthdate;
	private URL image;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
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
	public URL getImage() {
		return image;
	}
	public void setImage(URL image) {
		this.image = image;
	}
	
	public void setBirthdate(String birthdate) {
		if (StringUtils.isNotEmpty(birthdate)) {
			try {
				this.birthdate = LocalDate.parse(birthdate);
			} catch (IllegalArgumentException e) {
				// ignore
			}
		}
	}
	
	public void setBirthdate(BirthDate birthdate) {
		if (birthdate != null) {
			this.birthdate = new LocalDate(birthdate.getYear(), birthdate.getMonth(), birthdate.getDay());
		}
	}
	
	public void setGender(String gender) {
		if (StringUtils.isNotEmpty(gender)) {
			try {
				this.gender = Gender.valueOf(gender);
			} catch (IllegalArgumentException e) {
				// ignore
			}
		}
	}
	
	public void setImage(String image) {
		if (StringUtils.isNotEmpty(image)) {
			try {
				this.image = new URL(image);
			} catch (MalformedURLException e) {
				// ignore
			}
		}
	}

}
