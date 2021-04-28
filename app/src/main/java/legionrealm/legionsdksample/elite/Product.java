package legionrealm.legionsdksample.elite;

import java.util.List;

/**
 *
 */
public class Product {
    // {
    //      "product_id": "product id",
    //      "name": "product name",
    //      "description": "desc",
    //      "price": [
    //        {
    //          "currency": "USD",
    //          "amount": "1.00"
    //        }
    //      ],
    //      "one_off_limit": {
    //        "cycle": "ever",
    //        "length": 0
    //      },
    //      "consumable": true
    //    }
    private String product_id;
    private String name;
    private String description;
    private List<Price> price;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
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

    public List<Price> getPrice() {
        return price;
    }

    public void setPrice(List<Price> price) {
        this.price = price;
    }

    public static class Price {
        private String amount;
        private String currency;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}
