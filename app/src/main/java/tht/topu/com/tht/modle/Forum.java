package tht.topu.com.tht.modle;

/**
 * Created by shituocheng on 2017/8/11.
 */

public class Forum {

    private boolean isTop;
    private boolean isFavorite;
    private boolean isDel;
    private String ForumTitle;
    private String avatarIcon;
    private String userName;
    private String tagName;
    private String vip;
    private int likeNum;
    private int replyNum;

    private Forum(Builder builder) {
        setTop(builder.isTop);
        setFavorite(builder.isFavorite);
        setDel(builder.isDel);
        setForumTitle(builder.ForumTitle);
        setAvatarIcon(builder.avatarIcon);
        setUserName(builder.userName);
        setTagName(builder.tagName);
        setVip(builder.vip);
        setLikeNum(builder.likeNum);
        setReplyNum(builder.replyNum);
    }

    public boolean isDel() {
        return isDel;
    }

    public void setDel(boolean del) {
        isDel = del;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getForumTitle() {
        return ForumTitle;
    }

    public void setForumTitle(String forumTitle) {
        ForumTitle = forumTitle;
    }

    public String getAvatarIcon() {
        return avatarIcon;
    }

    public void setAvatarIcon(String avatarIcon) {
        this.avatarIcon = avatarIcon;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public int getReplyNum() {
        return replyNum;
    }

    public void setReplyNum(int replyNum) {
        this.replyNum = replyNum;
    }

    public static final class Builder {
        private String ForumTitle;
        private String avatarIcon;
        private String userName;
        private String tagName;
        private String vip;
        private int likeNum;
        private int replyNum;
        private boolean isTop;
        private boolean isFavorite;
        private boolean isDel;

        public Builder() {
        }

        public Builder ForumTitle(String val) {
            ForumTitle = val;
            return this;
        }

        public Builder avatarIcon(String val) {
            avatarIcon = val;
            return this;
        }

        public Builder userName(String val) {
            userName = val;
            return this;
        }

        public Builder tagName(String val) {
            tagName = val;
            return this;
        }

        public Builder vip(String val) {
            vip = val;
            return this;
        }

        public Builder likeNum(int val) {
            likeNum = val;
            return this;
        }

        public Builder replyNum(int val) {
            replyNum = val;
            return this;
        }

        public Forum build() {
            return new Forum(this);
        }

        public Builder isTop(boolean val) {
            isTop = val;
            return this;
        }

        public Builder isFavorite(boolean val) {
            isFavorite = val;
            return this;
        }

        public Builder isDel(boolean val) {
            isDel = val;
            return this;
        }
    }
}
