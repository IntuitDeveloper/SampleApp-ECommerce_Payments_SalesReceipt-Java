package com.intuit.developer.sampleapp.ecommerce.domain;

import com.intuit.developer.sampleapp.ecommerce.converters.MoneyConverter;
import org.joda.money.Money;


import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 8/20/14
 * Time: 4:08 PM
 */
@Entity
public class SalesItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String qboId;

    @Column(unique = true)
    private String name;

    private String description;

    @Convert(converter = MoneyConverter.class)
    private Money rate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_fk", referencedColumnName = "id")
    private Company company;

    public SalesItem() {

    }

    public SalesItem(String name, String description, Money rate) {
        this.name = name;
        this.description = description;
        this.rate = rate;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Money getRate() {
        return rate;
    }

    public void setRate(Money rate) {
        this.rate = rate;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Company getCompany() {
        return company;
    }
}
