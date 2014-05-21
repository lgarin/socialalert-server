package com.bravson.socialalert.app.entities;

import java.net.URI;
import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.format.annotation.DateTimeFormat;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;
import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.Gender;
import com.bravson.socialalert.common.domain.ProfileInfo;
import com.bravson.socialalert.common.domain.PublicProfileInfo;

@SolrDocument(solrCoreName="UserProfile")
public class UserProfile extends VersionedEntity {

	@Id
	@Field
	private UUID uuid;
	
	@Field
    private String lastname;
	 
	@Field
    private String firstname;
	
	@Field
	private Gender gender;
	
	@Field
	private String country;
	
	@Field
	private String language;
	
	@Field
	@DateTimeFormat
	private LocalDateTime birthdate;
	
	@Field
	private URI image;
	
	@Field
	private String nickname;
	
	@Field
	private String biography;
	
	protected UserProfile() {
		
	}
	
	public UserProfile(String nickname) {
		this.uuid = UUID.randomUUID();
		this.nickname = nickname;
	}

	public UUID getId() {
		return uuid;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public String getLastname() {
		return lastname;
	}

	public Gender getGender() {
		return gender;
	}

	public String getCountry() {
		return country;
	}

	public String getLanguage() {
		return language;
	}

	public LocalDate getBirthdate() {
		return birthdate == null ? null : birthdate.toLocalDate();
	}

	public URI getImage() {
		return image;
	}

	public void complete(ExternalProfileInfo information) {
		if (firstname == null) {
			firstname = information.getFirstname();
		}
		if (lastname == null) {
			lastname = information.getLastname();
		}
		if (language == null) {
			language = information.getLanguage();
		}
		if (gender == null) {
			gender = information.getGender();
		}
		if (country == null) {
			country = information.getCountry();
		}
		if (birthdate == null && information.getBirthdate() != null) {
			birthdate = information.getBirthdate().toLocalDateTime(LocalTime.MIDNIGHT);
		}
		
		touch();
	}

	public ProfileInfo toProfileInfo() {
		ProfileInfo info = new ProfileInfo();
		info.setFirstname(firstname);
		info.setLastname(lastname);
		info.setLanguage(language);
		info.setGender(gender);
		info.setCountry(country);
		if (birthdate != null) {
			info.setBirthdate(birthdate.toLocalDate());
		}
		info.setImage(image);
		info.setNickname(nickname);
		info.setBiography(biography);
		return info;
	}
	
	public PublicProfileInfo toPublicInfo() {
		PublicProfileInfo info = new PublicProfileInfo();
		info.setImage(image);
		info.setNickname(nickname);
		info.setProfileId(uuid);
		info.setBiography(biography);
		return info;
	}

	public void update(ProfileInfo info) {
		nickname = info.getNickname();
		firstname = info.getFirstname();
		lastname = info.getLastname();
		language = info.getLanguage();
		gender = info.getGender();
		country = info.getCountry();
		if (info.getBirthdate() != null) {
			birthdate = info.getBirthdate().toLocalDateTime(LocalTime.MIDNIGHT);
		} else {
			birthdate = null;
		}
		biography = info.getBiography();
		touch();
	}

	public void updateImage(URI uri) {
		image = uri;
		touch();
	}
}
