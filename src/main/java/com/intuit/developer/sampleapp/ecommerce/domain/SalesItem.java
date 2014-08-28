package com.intuit.developer.sampleapp.ecommerce.domain;

import com.intuit.developer.sampleapp.ecommerce.converters.MoneyConverter;
import org.joda.money.Money;


import javax.persistence.*;
import java.math.BigDecimal;

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

    private String imageFile;

    @Convert(converter = MoneyConverter.class)
    private Money unitPrice;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_fk", referencedColumnName = "id")
    private Company company;

    // Zero Items is a sane default.
    private BigDecimal qtyOnHand;

    public SalesItem() {

    }

    public SalesItem(String name, String description, Money unitPrice, String imageFile) {
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.imageFile = imageFile;
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

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Company getCompany() {
        return company;
    }

    public void setQtyOnHand(BigDecimal qtyOnHand) {
        this.qtyOnHand = qtyOnHand;
    }

    public BigDecimal getQtyOnHand() {
        return this.qtyOnHand;
    }
}
