package com.intuit.developer.sampleapp.ecommerce.oauth.controllers;

/**
 *
 */
public class SyncRequest {

	public enum EntityType {
		Customer,
		SalesItem
	}

	private EntityType type;
	private String companyId;
	private boolean successful;
	private String message;

	public SyncRequest() {

	}

	public EntityType getType() {
		return type;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}