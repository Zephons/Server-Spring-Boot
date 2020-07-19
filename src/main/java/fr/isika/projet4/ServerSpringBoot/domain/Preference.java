package fr.isika.projet4.ServerSpringBoot.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Preference {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false, updatable = false)
	private Long id;
	private String keyword;
	private String language;
	private Integer callTime;
	
	public Preference() {
	}
	
	public Preference(String keyword, String language, Integer callTime) {
		this.keyword = keyword;
		this.language = language;
		this.callTime = callTime;
	}

	public Long getId() {
		return id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Integer getCallTime() {
		return callTime;
	}

	public void setCallTime(Integer callTime) {
		this.callTime = callTime;
	}
	
}
