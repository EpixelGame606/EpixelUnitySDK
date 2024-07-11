package com.epixel.sdk.unity.billing;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BillingBridgeHelper {
    @Nullable
    public static String productDetailToJson(List<ProductDetails> productDetails) {
        try {
            JSONObject arrayObject = new JSONObject();
            JSONArray array = new JSONArray();
            arrayObject.put("List", array);
            for (ProductDetails productDetail : productDetails) {
                String productId = productDetail.getProductId();
                String productType = productDetail.getProductType();
                int productTypeEnum;
                String title = productDetail.getTitle();
                String description = productDetail.getDescription();
                String formattedPrice = "", currencyType = "";
                if (BillingClient.ProductType.INAPP.equals(productType)) {
                    ProductDetails.OneTimePurchaseOfferDetails offerDetail = productDetail.getOneTimePurchaseOfferDetails();
                    if (offerDetail != null) {
                        formattedPrice = offerDetail.getFormattedPrice();
                        currencyType = offerDetail.getPriceCurrencyCode();
                    }
                    productTypeEnum = 0;
                } else {
                    List<ProductDetails.SubscriptionOfferDetails> offerDetails = productDetail.getSubscriptionOfferDetails();
                    if (offerDetails != null && offerDetails.size() > 0) {
                        List<ProductDetails.PricingPhase> pricingPhaseList = offerDetails.get(0).getPricingPhases().getPricingPhaseList();
                        if (pricingPhaseList.size() > 0) {
                            formattedPrice = pricingPhaseList.get(0).getFormattedPrice();
                            currencyType = pricingPhaseList.get(0).getPriceCurrencyCode();
                        }
                    }
                    productTypeEnum = 1;
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.putOpt("ProductId", productId);
                jsonObject.putOpt("ProductType", productTypeEnum);
                jsonObject.putOpt("Title", title);
                jsonObject.putOpt("Description", description);
                jsonObject.putOpt("FormattedPrice", formattedPrice);
                jsonObject.putOpt("CurrencyType", currencyType);
                array.put(jsonObject);
            }
            return arrayObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String purchaseToJson(String productId) {

        try {
            JSONObject arrayObject = new JSONObject();
            JSONArray array = new JSONArray();
            arrayObject.put("List", array);
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("ProductId", productId);
            jsonObject.putOpt("OrderId", "");
            jsonObject.putOpt("Quantity", 0);
            jsonObject.putOpt("Acknowledged", false);
            jsonObject.putOpt("PurchaseToken", "");
            array.put(jsonObject);
            return arrayObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String purchaseToJson(List<Purchase> purchases) {
        try {
            JSONObject arrayObject = new JSONObject();
            JSONArray array = new JSONArray();
            arrayObject.put("List", array);
            for (Purchase purchase : purchases) {
                String productId = purchase.getProducts().get(0);
                String orderId = purchase.getOrderId();
                boolean acknowledged = purchase.isAcknowledged();
                int quantity = purchase.getQuantity();
                String purchaseToken = purchase.getPurchaseToken();
                JSONObject jsonObject = new JSONObject();
                int purchaseState = purchase.getPurchaseState();
                jsonObject.putOpt("ProductId", productId);
                jsonObject.putOpt("OrderId", orderId);
                jsonObject.putOpt("Quantity", quantity);
                jsonObject.putOpt("Acknowledged", acknowledged);
                jsonObject.putOpt("PurchaseToken", purchaseToken);
                jsonObject.putOpt("PurchaseState", purchaseState);
                array.put(jsonObject);
            }
            return arrayObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }
}
