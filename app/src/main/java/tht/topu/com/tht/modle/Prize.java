package tht.topu.com.tht.modle;

/**
 * Created by shituocheng on 20/09/2017.
 */

public class Prize {

    private String name;
    private int Probability;
    private String pid;
    private String stockFinal;

    private Prize(Builder builder) {
        setName(builder.name);
        setProbability(builder.Probability);
        setPid(builder.pid);
        setStockFinal(builder.stockFinal);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProbability() {
        return Probability;
    }

    public void setProbability(int probability) {
        Probability = probability;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getStockFinal() {
        return stockFinal;
    }

    public void setStockFinal(String stockFinal) {
        this.stockFinal = stockFinal;
    }

    public static final class Builder {
        private String name;
        private int Probability;
        private String pid;
        private String stockFinal;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder Probability(int val) {
            Probability = val;
            return this;
        }

        public Builder pid(String val) {
            pid = val;
            return this;
        }

        public Builder stockFinal(String val) {
            stockFinal = val;
            return this;
        }

        public Prize build() {
            return new Prize(this);
        }
    }
}
