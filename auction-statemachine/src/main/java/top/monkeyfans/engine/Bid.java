package top.monkeyfans.engine;

import java.io.Serializable;
import java.math.BigDecimal;

public class Bid implements Serializable {
    private String auctionId;
    private String userId;
    private BigDecimal price;

    public Bid() {
    }

    public Bid(String auctionId, String userId, BigDecimal price) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.price = price;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
