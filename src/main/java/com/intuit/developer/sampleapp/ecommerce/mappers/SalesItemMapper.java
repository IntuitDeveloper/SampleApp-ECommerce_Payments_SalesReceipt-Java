package com.intuit.developer.sampleapp.ecommerce.mappers;

import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemTypeEnum;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 8/21/14
 * Time: 8:58 AM
 */
public class SalesItemMapper {

    private static BoundMapperFacade<SalesItem, com.intuit.ipp.data.Item> domainToQBOMapper;

    static {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        mapperFactory.classMap(SalesItem.class, com.intuit.ipp.data.Item.class)
                .field("unitPrice.amount", "unitPrice")
		        .exclude("id")
                .byDefault()
                .register();

        domainToQBOMapper = mapperFactory.getMapperFacade(SalesItem.class, com.intuit.ipp.data.Item.class);
    }

    public static Item buildQBOObject(SalesItem salesItem) {

        if (salesItem == null) {
            return null;
        }

        Item qboServiceItem = domainToQBOMapper.map(salesItem);
        // These values must be set regardless of the sales item, they have meaning for QBO but not so much for this app
        qboServiceItem.setActive(true);
        qboServiceItem.setTaxable(true);
        qboServiceItem.setSalesTaxIncluded(false);
        qboServiceItem.setTrackQtyOnHand(true);
        qboServiceItem.setType(ItemTypeEnum.INVENTORY);
        return qboServiceItem;
    }
}
