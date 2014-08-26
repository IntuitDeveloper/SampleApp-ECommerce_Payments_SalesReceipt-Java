package com.intuit.developer.sampleapp.ecommerce.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 6/17/14
 * Time: 5:41 PM
 */
@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private boolean connectedToQbo;
    private boolean employeesSynced;
    private boolean customersSynced;
    private boolean salesItemSynced;

    @Column(unique = true)
    private String qboId;

    private String requestToken;
    private String requestTokenSecret;
    private String accessToken;
    private String accessTokenSecret;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company")
    private final List<Customer> customers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company")
    private final List<SalesItem> salesItems = new ArrayList<>();

    public Company() {

    }

    public Company(String qdoId, String accessToken, String accessTokenSecret) {
        this.qboId = qdoId;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.connectedToQbo = true;
    }

    public Company(String name) {
        this.name = name;
        this.connectedToQbo = false;
    }

    public long getId() {
        return id;
    }

    public String getQboId() {
        return qboId;
    }

    public void setQboId(String qboId) {
        this.qboId = qboId;
    }

    public String getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    public String getRequestTokenSecret() {
        return requestTokenSecret;
    }

    public void setRequestTokenSecret(String requestTokenSecret) {
        this.requestTokenSecret = requestTokenSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConnectedToQbo() {
        return connectedToQbo;
    }

    public void setConnectedToQbo(boolean connectedToQbo) {
        this.connectedToQbo = connectedToQbo;
    }

    public boolean isEmployeesSynced() {
        return employeesSynced;
    }

    public void setEmployeesSynced(boolean employeesSynced) {
        this.employeesSynced = employeesSynced;
    }

    public boolean isCustomersSynced() {
        return customersSynced;
    }

    public void setCustomersSynced(boolean customersSynced) {
        this.customersSynced = customersSynced;
    }

    public boolean isSalesItemSynced() {
        return salesItemSynced;
    }

    public void setSalesItemSynced(boolean serviceItemsSynced) {
        this.salesItemSynced = serviceItemsSynced;
    }

	public List<Customer> getCustomers() {
		return customers;
	}

	public void addCustomer(Customer customer) {
        this.customers.add(customer);
        customer.setCompany(this);
    }

	public List<SalesItem> getSalesItems() {
		return salesItems;
	}

	public void addServiceItem(SalesItem salesItem) {
        this.salesItems.add(salesItem);
        salesItem.setCompany(this);
    }

}
