package com.snscrawler.app.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "insta_account")
public class AccountEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private int seq;
	
	@Column(nullable = false, length = 11)
	private int num;
	
	@Column(nullable = false, unique = true, length = 100)
	private String id;
	
	@Column(nullable = false, length = 50)
	private String pw;
	
	@Column(nullable = false, length = 50)
	private String proxy;
	
	@Column(nullable = false, length = 4)
	private int block;
	
}
