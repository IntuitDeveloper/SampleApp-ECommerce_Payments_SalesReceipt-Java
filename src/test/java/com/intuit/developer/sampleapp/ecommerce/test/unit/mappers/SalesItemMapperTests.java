package com.intuit.developer.sampleapp.ecommerce.test.unit.mappers;

import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.developer.sampleapp.ecommerce.mappers.SalesItemMapper;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemTypeEnum;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.Assert.*;


public class SalesItemMapperTests {

    @Test
    public void testDomainToQBOMapping() throws Exception {
        // Create constant values
        final String name = "MagicTurnip";
        final String description = "It's magical";
        final Money unitPrice = Money.of(CurrencyUnit.USD, 5.00);
        final BigDecimal qtyOnHand = new BigDecimal(500);

        // Create a domain object and populate it
        SalesItem domainObject = new SalesItem();
        domainObject.setName(name);
        domainObject.setDescription(description);
        domainObject.setUnitPrice(unitPrice);
        domainObject.setQtyOnHand(qtyOnHand);

        // Performing Mapping
        Item qboEntity = SalesItemMapper.buildQBOObject(domainObject);

        // Compare Values of Domain Object and QBO entity
        assertEquals(domainObject.getName(), qboEntity.getName());
        assertEquals(domainObject.getDescription(), qboEntity.getDescription());
        assertEquals(domainObject.getQtyOnHand(), qboEntity.getQtyOnHand());

        // The type conversion may be a problem.
        assertEquals(domainObject.getUnitPrice().getAmount(), qboEntity.getUnitPrice());
        // Check for explicit / special fields on the _QBO ENTITY_ that are not part of domain object but should be set
        // as part of mapping
        assertTrue(qboEntity.isTaxable());
        assertTrue(qboEntity.isActive());
        assertTrue(qboEntity.isTrackQtyOnHand());
        assertFalse(qboEntity.isSalesTaxIncluded());
        assertEquals(ItemTypeEnum.INVENTORY, qboEntity.getType());
    }

    @Test
    public void testDomainToQBOMappingWithNull() throws Exception {
        // Test mapping with null input
        assertNull(SalesItemMapper.buildQBOObject(null));
    }
}