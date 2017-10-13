package tht.topu.com.tht.modle;

/**
 * Created by shituocheng on 2017/8/9.
 */

public class Rank {

    private String userIcon;
    private String userName;
    private String userPoint;
    private String userRank;
    private String userRankImage;
    private int memberLv;
    private int grade;

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    private String mid;

    private Rank(Builder builder) {
        setUserIcon(builder.userIcon);
        setUserName(builder.userName);
        setUserPoint(builder.userPoint);
        setUserRank(builder.userRank);
        setUserRankImage(builder.userRankImage);
        setMemberLv(builder.memberLv);
        setGrade(builder.grade);
        setMid(builder.mid);
    }

    public int getMemberLv() {
        return memberLv;
    }

    public void setMemberLv(int memberLv) {
        this.memberLv = memberLv;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPoint() {
        return userPoint;
    }

    public void setUserPoint(String userPoint) {
        this.userPoint = userPoint;
    }

    public String getUserRank() {
        return userRank;
    }

    public void setUserRank(String userRank) {
        this.userRank = userRank;
    }

    public String getUserRankImage() {
        return userRankImage;
    }

    public void setUserRankImage(String userRankImage) {
        this.userRankImage = userRankImage;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public static final class Builder {
        private String userIcon;
        private String userName;
        private String userPoint;
        private String userRank;
        private String userRankImage;
        private int memberLv;
        private int grade;
        private String mid;

        public Builder() {
        }

        public Builder userIcon(String val) {
            userIcon = val;
            return this;
        }

        public Builder userName(String val) {
            userName = val;
            return this;
        }

        public Builder userPoint(String val) {
            userPoint = val;
            return this;
        }

        public Builder userRank(String val) {
            userRank = val;
            return this;
        }

        public Builder userRankImage(String val) {
            userRankImage = val;
            return this;
        }

        public Builder memberLv(int val) {
            memberLv = val;
            return this;
        }

        public Builder grade(int val) {
            grade = val;
            return this;
        }

        public Builder mid(String val) {
            mid = val;
            return this;
        }

        public Rank build() {
            return new Rank(this);
        }
    }
}
