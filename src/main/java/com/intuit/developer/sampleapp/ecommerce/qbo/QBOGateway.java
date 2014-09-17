package com.intuit.developer.sampleapp.ecommerce.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.CartItem;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.developer.sampleapp.ecommerce.domain.ShoppingCart;
import com.intuit.developer.sampleapp.ecommerce.mappers.CustomerMapper;
import com.intuit.developer.sampleapp.ecommerce.mappers.SalesItemMapper;
import com.intuit.developer.sampleapp.ecommerce.repository.CustomerRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.SalesItemRepository;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.BatchOperation;
import com.intuit.ipp.services.DataService;
import org.apache.commons.lang.math.IEEE754rUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class contains the needed code to interface with QBO via QBO v3 SDK.
 */
public class QBOGateway {

    @Autowired
    private QBOServiceFactory qboServiceFactory;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesItemRepository salesItemRepository;

    /**
     * Called when "Place Order" is clicked in the UI
     * Creates a sales receipt based upon the passed in cart and makes all needed references
     * @param cart - the cart that was "ordered" which a sales receipt must be created for
     * @return - returns the created sales receipt as sent back from QBO API.
     */
    public SalesReceipt createSalesReceiptInQBO(ShoppingCart cart) {
        Customer customer = cart.getCustomer();
        DataService dataService = qboServiceFactory.getDataService(customer.getCompany());
        // Make a sales receipt
        SalesReceipt receipt = new SalesReceipt();

        //Append the necessary lines items from the shopping cart
        appendLineItems(receipt, cart);

        /**
         * In a real application more judgement would be required to associate the correct tax code to a purchase.
         * Uncomment these lines to associate the receipt with a tax code in order to apply sales tax.
         * A Tax code must be correctly configured in the QBO Company, Sales Tax (As a feature) must be turned on,
         * and the sales item referenced must be have the taxable flag set.
         */
        TxnTaxDetail txnTaxDetail = new TxnTaxDetail();
        txnTaxDetail.setTxnTaxCodeRef(QBOQueryMethods.findTaxCodeReference(dataService, "California"));
        receipt.setApplyTaxAfterDiscount(true);
        receipt.setTxnTaxDetail(txnTaxDetail);

        // Set the reference to the customer
        ReferenceType customerRef = new ReferenceType();
        customerRef.setValue(customer.getQboId());
        receipt.setCustomerRef(customerRef);
        receipt.setPaymentMethodRef(QBOQueryMethods.findPaymentMethodReference(dataService, "Visa"));
        receipt = createObjectInQBO(dataService, receipt);

        // Construct an order confirmation from the
        return receipt;
    }

    /**
     * Called when "Sync" on the Admin->Settings page is invoked in the UI
     *
     * Customers are pushed to QBO as QBO Customers.
     *
     * NOTE: this example is simplified for the sample application; if you are syncing large sets of objects,
     *       please use the BatchOperation API
     */
	public void createCustomerInQBO(Customer customer) {
		DataService dataService = qboServiceFactory.getDataService(customer.getCompany());

        /**
         * In order to prevent syncing the same data into QBO more than once, query to see if the entity already exists.
         * This solution is only meant to provide a better sample app experience (e.g. if you wipe out your database,
         * we don't want the sample app to keep creating the same data over and over in QBO).
         *
         * In a production app keeping data in two systems in sync is a difficult problem to solve; this code is not
         * meant to demonstrate production quality sync functionality.
         */

		com.intuit.ipp.data.Customer returnedQBOObject = QBOQueryMethods.findCustomer(dataService, customer);
		if (returnedQBOObject == null) {
			final com.intuit.ipp.data.Customer qboObject = CustomerMapper.buildQBOObject(customer);
			returnedQBOObject = createObjectInQBO(dataService, qboObject);
		}

		// TODO: comment on best practice of not embedding QBO ID on app domain class
		customer.setQboId(returnedQBOObject.getId());
		customerRepository.save(customer);
	}


	/**
	 * Called when "Sync" on the Admin->Settings page is invoked in the UI
	 *
	 * SalesItems are pushed to QBO as QBO Items.
	 * QBO Items can be viewed on the "Products and Services" page within QBO.
	 *
	 * NOTE: this example is simplified for the sample application; if you are syncing large sets of objects,
	 *       please use the BatchOperation API
	 */
    public void createItemInQBO(SalesItem salesItem) {
	    DataService dataService = qboServiceFactory.getDataService(salesItem.getCompany());

        /**
         * In order to prevent syncing the same data into QBO more than once, query to see if the entity already exists.
         * This solution is only meant to provide a better sample app experience (e.g. if you wipe out your database,
         * we don't want the sample app to keep creating the same data over and over in QBO).
         *
         * In a production app keeping data in two systems in sync is a difficult problem to solve; this code is not
         * meant to demonstrate production quality sync functionality.
         */
	    com.intuit.ipp.data.Item returnedQBOObject = QBOQueryMethods.findItem(dataService, salesItem);
	    if (returnedQBOObject == null) {
		    // copy SalesItem to QBO Item
		    // This also populates some necessary default constant values
		    Item qboItem = SalesItemMapper.buildQBOObject(salesItem);
		    //
		    // We need to do lookups for accounts to associate the item to
		    //

		    // find an Income Account to associate with QBO item
		    ReferenceType accountRef = QBOQueryMethods.findAccountReference(dataService, AccountTypeEnum.INCOME, AccountSubTypeEnum.SALES_OF_PRODUCT_INCOME);
		    qboItem.setIncomeAccountRef(accountRef);

		    // find an Asset Account to associate with QBO item
		    ReferenceType assetAccountRef = QBOQueryMethods.findAccountReference(dataService, AccountTypeEnum.OTHER_CURRENT_ASSET, AccountSubTypeEnum.INVENTORY);
		    qboItem.setAssetAccountRef(assetAccountRef);

		    // find a Cost of Goods Sold account to use as the expense account reference on the QBO Item
		    ReferenceType cogAccountRef = QBOQueryMethods.findAccountReference(dataService, AccountTypeEnum.COST_OF_GOODS_SOLD, AccountSubTypeEnum.SUPPLIES_MATERIALS_COGS);
		    qboItem.setExpenseAccountRef(cogAccountRef);

		    // Set the inventory start date to be today
		    // This is not set in the mapper because this should only be set when the item is first created in QBO
		    qboItem.setInvStartDate(new Date());

		    // save the item in OBO
		    returnedQBOObject = createObjectInQBO(dataService, qboItem);
	    }

	    // update the SalesItem in app
	    salesItem.setQboId(returnedQBOObject.getId());
	    salesItemRepository.save(salesItem);
    }

    /**
     * Append Line Items to a sales receipt based on a shopping cart.
     * This is the bulk of the effort in creating a sales receipt.
     * @param salesReceipt - the receipt to append line items to
     * @param cart - the cart to use as a basis for the receipt (represents what was purchased)
     */
	private void appendLineItems(SalesReceipt salesReceipt, ShoppingCart cart) {
		// Make a list of lines
		List<Line> lineItems = new ArrayList<>();

		// For each cart item there will be a line item
		for (CartItem cartItem : cart.getCartItems()) {
			Line line = new Line();

            // This line corresponds to a the _sale_ of an _item_
            line.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);

			// Create a line detail
			SalesItemLineDetail lineDetail = new SalesItemLineDetail();

			// Make the line item reference the sales item from the cart item
			ReferenceType itemRef = new ReferenceType();
			itemRef.setValue(cartItem.getSalesItem().getQboId());
			lineDetail.setItemRef(itemRef);

            /**
             * These lines make the line item reference the TaxCode "TAX" so that
             * The sales receipt will include the amount of this line item in the tax calculation
             * "TAX" is a special reference value
             */
            ReferenceType taxRef = new ReferenceType();
            taxRef.setValue("TAX");
            lineDetail.setTaxCodeRef(taxRef);

			// Set the quantity
			lineDetail.setQty(BigDecimal.valueOf(cartItem.getQuantity()));

			// Set the unit price
			lineDetail.setUnitPrice(cartItem.getSalesItem().getUnitPrice().getAmount());
			line.setSalesItemLineDetail(lineDetail);
			line.setDescription(cartItem.getSalesItem().getName());

			// Set the line total
			line.setAmount(lineDetail.getUnitPrice().multiply(lineDetail.getQty()));

			// Add the line item to the list
			lineItems.add(line);
		}

		// Create a line to contain the discount
		Line discountLine = new Line();

        // This line corresponds to a _discout_
		discountLine.setDetailType(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL);

        // Creating an attaching a 'detail' object
        DiscountLineDetail discountLineDetail = new DiscountLineDetail();

        /**
         * There are two ways to set the value of the discount which are mutually exclusive
         * 1) Set an explicit amount
         * 2) Set a percentage of total.
         *
         * We will use the _percentage_ approach.
         */

        // Explicit amount approach
        // discountLine.setAmount(cart.getPromotionSavings().getAmount());

        // Percentage approach
        discountLineDetail.setDiscountPercent(new BigDecimal(ShoppingCart.PROMOTION_MULTIPLIER * 100));
        discountLineDetail.setPercentBased(true);

        // Add the discount details to the line
        discountLine.setDiscountLineDetail(discountLineDetail);

        // Add the discount line to the sales recept
		lineItems.add(discountLine);
    
        // Add the line items to the receipt
        salesReceipt.setLine(lineItems);
	}

    /**
     * Type generic wrapper for {@link com.intuit.ipp.services.DataService#add(com.intuit.ipp.core.IEntity)}
     * which translates a a somewhat confusing exception case.
     * @param dataService - the data service to use.
     * @param qboObject - the object to add to qbo
     * @param <T> - the type of the object to create
     * @return create object
     */
    private <T extends IEntity> T createObjectInQBO(DataService dataService, T qboObject) {
        try {
            final T createdObject = dataService.add(qboObject);
            return createdObject;
        } catch (FMSException e) {
            //NOTE: a StaleObjectException can be received while creating a new object; this error indicates that an
            //      object with that QBO ID already exists.
            throw new RuntimeException("Failed create an " + qboObject.getClass().getName() + " in QBO", e);
        }

    }
}
