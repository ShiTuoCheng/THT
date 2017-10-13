package tht.topu.com.tht.modle;

/**
 * Created by shituocheng on 2017/8/8.
 */

public class Product {

    private String image;
    private String productTitle;
    private String productPrice;
    private boolean isHot;
    private boolean isRecommend;
    private String ogProductPrice;
    private String Mcid;

    private Product(Builder builder) {
        setImage(builder.image);
        setProductTitle(builder.productTitle);
        setProductPrice(builder.productPrice);
        setHot(builder.isHot);
        setRecommend(builder.isRecommend);
        setOgProductPrice(builder.ogProductPrice);
        Mcid = builder.Mcid;
    }

    public String getMcid() {
        return Mcid;
    }

    public void setMcid(String mcid) {
        Mcid = mcid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public boolean isHot() {
        return isHot;
    }

    public void setHot(boolean hot) {
        isHot = hot;
    }

    public boolean isRecommend() {
        return isRecommend;
    }

    public void setRecommend(boolean recommend) {
        isRecommend = recommend;
    }

    public String getOgProductPrice() {
        return ogProductPrice;
    }

    public void setOgProductPrice(String ogProductPrice) {
        this.ogProductPrice = ogProductPrice;
    }

    public static final class Builder {
        private String image;
        private String productTitle;
        private String productPrice;
        private boolean isHot;
        private boolean isRecommend;
        private String ogProductPrice;
        private String Mcid;

        public Builder() {
        }

        public Builder image(String val) {
            image = val;
            return this;
        }

        public Builder productTitle(String val) {
            productTitle = val;
            return this;
        }

        public Builder productPrice(String val) {
            productPrice = val;
            return this;
        }

        public Builder isHot(boolean val) {
            isHot = val;
            return this;
        }

        public Builder isRecommend(boolean val) {
            isRecommend = val;
            return this;
        }

        public Builder ogProductPrice(String val) {
            ogProductPrice = val;
            return this;
        }

        public Builder Mcid(String val) {
            Mcid = val;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
